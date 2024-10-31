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
        Thread.sleep(2000)
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
        def l = createLink(controller: 'suggestion', action: 'feed', absolute: true)
        render(contentType: "application/rss+xml", encoding: "UTF-8") {
            rss(version: "2.0") {
                channel {
                    title("LanguageTool Word Suggestions")
                    link(l)
                    description("Words that maybe should be added to LanguageTool's spell dictionary, as suggested by users")                    
                    suggestions.each { suggestion ->
                        def word = suggestion.word
                        word = word.replaceAll(xml10pattern, "_")
                        if (word != suggestion.word) {
                            word += " [cleaned for XML]"
                        }
                        item {
                            title(suggestion.word)
                            //link(item.link)
                            description("Word: ${suggestion.word}")
                            pubDate(suggestion.date)
                            //guid(item.guid)
                        }
                    }
                }
            }
        }
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
