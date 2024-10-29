package languagetool.community.website2

import org.languagetool.remote.CheckConfiguration
import org.languagetool.remote.CheckConfigurationBuilder
import org.languagetool.remote.RemoteLanguageTool
import org.languagetool.remote.RemoteResult
import org.languagetool.RuleComparator
import org.languagetool.*
import org.languagetool.Languages
import org.languagetool.rules.*
import org.languagetool.rules.patterns.*

class RuleController {

    def index() {
        redirect(action:'list', params:params)
    }

    def list() {
        int max = 10
        int offset = 0
        if (!params.lang) params.lang = "en"
        if (params.offset) offset = Integer.parseInt(params.offset)
        if (params.max) max = Integer.parseInt(params.max)
        String langCode = getLanguageCode()
        Language langObj = Languages.getLanguageForShortCode(langCode)
        JLanguageTool lt = new JLanguageTool(langObj)
        List<Rule> rules = lt.getAllRules()
        List<String> categories = getCategories(rules)
        if (params.filter || params.categoryFilter) {
            rules = filterRules(rules, params.filter, params.categoryFilter)
        }
        if (params.sort) {
            def sortF = SortField.pattern
            if (params.sort == 'description') sortF = SortField.description
            if (params.sort == 'category') sortF = SortField.category
            Collections.sort(rules, new RuleComparator(sortF,
                    params.order == 'desc' ? SortDirection.desc : SortDirection.asc));
        }
        int ruleCount = rules.size()
        if (ruleCount == 0) {
            rules = []
        } else {
            rules = rules[offset..Math.min(rules.size()-1, offset+max-1)]
        }
        render(view: 'list', model: [ruleList: rules, ruleCount: ruleCount,
                languages: SortedLanguages.get(), language: langObj,
                categories: categories, categoryFilter: params.categoryFilter])
    }

    private String getLanguageCode() {
        String lang = "en"
        if (params.lang) {
            lang = params.lang
        }
        assert(lang)
        return lang
    }

    private List<String> getCategories(List<Rule> rules) {
        Set<String> categorySet = new HashSet()
        for (rule in rules) {
            categorySet.add(rule.getCategory().getName())
        }
        List<String> categories = new ArrayList(categorySet)
        Collections.sort(categories)
        return categories
    }


}