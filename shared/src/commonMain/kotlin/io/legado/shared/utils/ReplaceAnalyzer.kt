package io.legado.shared.utils

import io.legado.shared.model.ReplaceRule

object ReplaceAnalyzer {

    fun applyReplacements(text: String, rules: List<ReplaceRule>): String {
        var result = text
        for (rule in rules) {
            if (!rule.isEnabled) continue
            result = applyRule(result, rule)
        }
        return result
    }

    fun applyRule(text: String, rule: ReplaceRule): String {
        if (!rule.isEnabled) {
            return text
        }
        return runCatching {
            if (rule.isRegex) {
                val regex = Regex(rule.pattern, RegexOption.MULTILINE)
                regex.replace(text, rule.replacement)
            } else {
                text.replace(rule.pattern, rule.replacement)
            }
        }.getOrDefault(text)
    }

    fun isValidRule(rule: ReplaceRule): Boolean {
        if (rule.pattern.isEmpty()) return false
        if (rule.isRegex) {
            return runCatching { Regex(rule.pattern) }.isSuccess
        }
        return true
    }
}
