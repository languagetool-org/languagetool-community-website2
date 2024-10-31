/* LanguageTool Community 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package languagetool.community.website2

import ltcommunity.Suggestion
import org.languagetool.languagemodel.LuceneLanguageModel
import org.languagetool.rules.Rule
import org.languagetool.*
import org.languagetool.rules.spelling.SpellingCheckRule

/**
 * Get user suggestion for words that might be added to the dictionary.
 */
class SuggestionController {
    
    def index() {
        if (!params.lang) {
            throw new Exception("Parameter 'lang' needs to be set")
        }
        if (!params.word) {
            throw new Exception("Parameter 'word' needs to be set")
        }
        Language lang = Languages.getLanguageForShortCode(params.lang)
        def bundle = JLanguageTool.getMessageBundle(lang)
        String i18nLanguage = bundle.getString(lang.getShortCode())
        [language: lang, i18nLanguage: i18nLanguage, email: params.email != 'off']
    }

    def suggestWord() {
        log.info("Saving word suggestion: ${params.word}, language ${params.languageCode}, email ${params.email}")
        if (params.word == "detecd") {
            log.info("Not saving 'detecd'")
            render "This is a test word and has not been sent to the reviewers."
            return
        }
        List existingWords = Suggestion.findAllByWordAndLanguageCode(params.word, params.languageCode)
        int ignoreCount = 0
        for (w in existingWords) {
            if (w.ignoreWord) {
                ignoreCount++
            }
        }
        if (ignoreCount > 0 && ignoreCount == existingWords.size()) {
            log.info("Not saving suggestion: ${params.word}, all ${ignoreCount} previous suggestions have been ignored")
            render "Previously, this word was rejected. If you think that the spelling is correct, open an issue on " +
                    "https://github.com/languagetool-org/languagetool/issues/. Give references that show that the " +
                    "spelling is correct. (Make sure that the spelling is correct for the language variant: British English, American English and so on.)"
            return
        }
        Suggestion s = new Suggestion()
        s.date = new Date()
        s.word = params.word
        s.languageCode = params.languageCode
        s.email = params.email
        s.ignoreWord = false
        s.save(failOnError: true)
        render(view: 'suggestWord')
    }
    
    def feed() {
        validatePassword()
        render(feedType:"rss", feedVersion:"2.0") {
            title = "LanguageTool Word Suggestions"
            description = "Words that should be added to LanguageTool's spell dictionary, as suggested by users"
            link = createLink(controller: 'suggestion', action: 'feed', absolute: true)
            List suggestions
            if (params.lang) {
                suggestions = Suggestion.findAllByLanguageCode(params.lang, [max: 100, sort:'date', order:'desc'])
            } else {
                suggestions = Suggestion.findAll([max: 100, sort:'date', order:'desc'])
            }
            String xml10pattern = "[^" +
                "\u0009\r\n" +
                "\u0020-\uD7FF" +
                "\uE000-\uFFFD" +
                "\ud800\udc00-\udbff\udfff" +
                "]"  // source: http://stackoverflow.com/questions/4237625/removing-invalid-xml-characters-from-a-string-in-java/4237934#4237934
            suggestions.each { suggestion ->
                def word = suggestion.word
                word = word.replaceAll(xml10pattern, "_")
                if (word != suggestion.word) {
                    word += " [cleaned for XML]"
                }
                entry("${word} - language: ${suggestion.languageCode}, email: ${suggestion.email}") {
                    publishedDate = suggestion.date
                    "Language: ${suggestions.languageCode}\n" +
                        "Word: ${suggestions.word}\n" +
                        "Email: ${suggestions.email}\n" +
                        "Date: ${suggestions.date}\n"
                }
            }
        }
    }
    
    def edit() {
        if (!params.lang) {
            throw new Exception("Param 'lang' not set")
        }
        if (!params.lang.matches("..")) {
            throw new Exception("Param valid 'lang' parameter: ${params.lang}")
        }
        validatePassword()

        // potential speed up?
        //def res = Suggestion.executeQuery("select word, count(word) as ct from ltcommunity.Suggestion where language_code = 'de' and ignore_word = false group by word order by ct desc")
        //word: print res.get(0)[0]
        //count: print res.get(0)[1]
        
        int allSuggestionCount = Suggestion.countByLanguageCodeAndIgnoreWord(params.lang, false)
        List suggestions = Suggestion.findAllByLanguageCodeAndIgnoreWord(params.lang, false, [sort:'date', order:'desc'])
        List suggestionIds = []
        suggestions.each { suggestionIds.add(it.id) }
        File ngramDir = new File(grailsApplication.config.ngramindex, params.lang)
        Map<String, Long> suggestionCounts = new HashMap()
        long t1 = System.currentTimeMillis()
        if (ngramDir.exists()) {
            LuceneLanguageModel lm = new LuceneLanguageModel(ngramDir)
            for (Suggestion s : suggestions) {
                suggestionCounts.put(s.word, lm.getCount(s.word.replaceFirst("\\.\$", "")))
            }
        }
        print "lm.getCount took " + (System.currentTimeMillis()-t1) + "ms"
        Map<String, List<String>> ltSuggestions = new HashMap()
        String langCode = params.lang
        if (langCode == "de") {
            langCode = "de-DE"
        } else if (langCode == "pt") {
            langCode = "pt-PT"
        } else if (langCode == "en") {
            langCode = "en-US"
        }
        def lt = new JLanguageTool(Languages.getLanguageForShortCode(langCode))
        def spellRule = null
        for (Rule rule : lt.getAllRules()) {
            if (!rule.isDictionaryBasedSpellingRule()) {
                lt.disableRule(rule.getId());
            }
            if (rule instanceof SpellingCheckRule) {
                spellRule = (SpellingCheckRule)rule
            }
        }
        Map word2Count = new HashMap()
        List filteredSuggestions = []
        Set listedWords = new HashSet()
        int minOcc = params.minOcc ? Integer.parseInt(params.minOcc) : 2
        t1 = System.currentTimeMillis()
        int limit = 50
        for (Suggestion s : suggestions) {
            int count = Suggestion.countByLanguageCodeAndWordAndIgnoreWord(params.lang, s.word, false)
            if (count < minOcc) {
                continue
            }
            word2Count.put(s.word, count)
            def matches = lt.check(s.word)
            // spellRule.isMisspelled(s.word) <- would be faster, but we want the suggestions...
            if (matches.size() > 0) {
                ltSuggestions.put(s.word, matches.get(0).getSuggestedReplacements())
            } else {
                // ignore - word probably already added (maybe suggested by user using an older version of LT)
                continue
            }
            if (!listedWords.contains(s.word)) {
                filteredSuggestions.add(s)
            }
            listedWords.add(s.word)
            if (filteredSuggestions.size() > limit) {
                break
            }
        }
        print "loop for " + suggestions.size() + " suggestions took " + (System.currentTimeMillis()-t1) + "ms"
        [ltSuggestions: ltSuggestions, suggestions: filteredSuggestions, suggestionIds: suggestionIds, suggestionCounts: suggestionCounts,
         allSuggestionCount: allSuggestionCount, word2Count: word2Count, minOcc: minOcc, limit: limit]
    }
    
    def hide() {
        validatePassword()
        int count = 0
        def suggestions = Suggestion.findAllByWord(params.word)
        for (Suggestion s  : suggestions) {
            s.ignoreWord = true
            s.save(failOnError: true)
            count++
        }
        print "Ignored " + count + " suggestions of word " + params.word
        render "OK"
    }

    def editDone() {
        validatePassword()
        String result = ""
        List ids = params.ids.split(",")
        int count = 0
        for (String id : ids) {
            Suggestion s = Suggestion.get(id)
            s.ignoreWord = true
            s.save(failOnError: true)
            if (params[id + "_use"]) {
                List suffixes = []
                // this is currently specific to German:
                if (params[id + "_N"]) {
                    suffixes.add("N")
                }
                if (params[id + "_S"]) {
                    suffixes.add("S")
                }
                if (params[id + "_A"]) {
                    suffixes.add("A")
                }
                if (params[id + "_E"]) {
                    suffixes.add("E")
                }
                if (suffixes.isEmpty()) {
                    result += params[id + "_word"] + "\n"
                } else {
                    result += params[id + "_word"] + "/" + suffixes.join('') + "\n"
                }
                count++
            }
        }
        log.info("Showing ${count} selected spell suggestions")
        [result: result]
    }

    private void validatePassword() {
        String password = grailsApplication.config.suggestion.password
        if (!password || password.trim().isEmpty()) {
            throw new Exception("'suggestion.password' needs to be set in config")
        }
        if (params.password != password) {
            throw new Exception("Invalid password")
        }
    }
}
