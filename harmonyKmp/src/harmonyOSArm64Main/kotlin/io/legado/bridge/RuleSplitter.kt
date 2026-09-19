package io.legado.bridge

/**
 * Legado source-rule string summarizer — pure Kotlin, no platform deps.
 * Demonstrates parsing logic that mirrors shared/analyzeRule behavior.
 */
internal object RuleSplitter {

    fun summarize(ruleStr: String): String {
        if (ruleStr.isBlank()) return ""
        val parts = ruleStr.split("##").map { it.trim() }
        val rule = parts[0]
        val mode = when {
            rule.startsWith("<js>") -> "JS"
            rule.startsWith("{") -> "XPath"
            rule.startsWith("$") -> "JSON"
            else -> "CSS"
        }
        val nav = parts.drop(1).joinToString("|", prefix = " -> ")
        return "$mode: $rule$nav"
    }
}