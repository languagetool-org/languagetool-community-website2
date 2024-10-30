hide.languages = ["cs", "ml", "be", "sk", "zh", "ast", "km", "en-GB", "en-US", "en-CA", "en-ZA", "en-NZ", "en-AU",
        "de-DE", "de-AT", "de-CH", "pt-BR", "pt-PT", "pt-AO", "pt-MZ", "ca-ES-valencia", "de-DE-x-simple-language", "sr-BA", "sr-RS", "sr-ME", "sr-HR", "nl-BE",
        "ca-ES-balear", "es-AR", "fr-BE", "fr-CA", "fr-CH"]
expose.languages = ["en", "fr", "de", "es", "pl", "ca", "nl", "pt", "es", "uk"]

maxPatternElements = 5
// the feed of user-suggested words is password protected:
suggestion.password = "fixme"

// Lucene index directories for fast rule matching - "LANG" will be replaced with the language code.
// Use SentenceSourceIndexer to create these indexes (including POS tagging):
fastSearchIndex = "/home/languagetool/corpus/LANG"
fastSearchTimeoutMillis = 15000

// path to grammar.xml with 'XX' as placeholders for language code
//grammarPathTemplate = "/home/dnaber/lt/git/languagetool/languagetool-language-modules/XX/src/main/resources/org/languagetool/rules/XX/grammar.xml"
grammarPathTemplate = "/home/languagetool/languagetool/languagetool-language-modules/XX/src/main/resources/org/languagetool/rules/XX/grammar.xml"
