package io.legado.shared.utils

import io.legado.shared.constant.AppPattern

/**
 * JavaScript encoding utilities for Legado.
 * Handles JS injection, encoding/decoding for book source rules.
 */
object JsEncodeUtils {

    /**
     * Execute a simple JS expression and return the result.
     */
    fun evalJs(jsCode: String, context: Map<String, Any?> = emptyMap()): String {
        // This is a stub — actual implementation delegated to JS engine
        return ""
    }

    /**
     * Check if a string contains JS patterns.
     */
    fun containsJs(rule: String): Boolean {
        return AppPattern.JS_PATTERN.containsMatchIn(rule) ||
               AppPattern.WebJS_PATTERN.containsMatchIn(rule)
    }

    /**
     * Extract JS content from a rule.
     */
    fun extractJs(rule: String): List<String> {
        val result = mutableListOf<String>()
        AppPattern.JS_PATTERN.findAll(rule).forEach { match ->
            match.groupValues.getOrNull(1)?.let { if (it.isNotEmpty()) result.add(it) }
            match.groupValues.getOrNull(2)?.let { if (it.isNotEmpty()) result.add(it) }
        }
        return result
    }

    /**
     * Wrap content in a JS execution block.
     */
    fun wrapJs(js: String): String {
        return "<js>$js</js>"
    }

    /**
     * Quote a string for JS string literal.
     */
    fun quoteJs(text: String): String {
        return "'" + text
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r") + "'"
    }

    /**
     * Replace @js: expression in rule with execution result.
     */
    fun replaceJsExpressions(
        rule: String,
        evaluator: (String) -> String
    ): String {
        return AppPattern.JS_PATTERN.replace(rule) { matchResult ->
            val jsCode = matchResult.groupValues.getOrNull(1)?.takeIf { it.isNotEmpty() }
                ?: matchResult.groupValues.getOrNull(2) ?: return@replace matchResult.value
            runCatching { evaluator(jsCode) }.getOrDefault(matchResult.value)
        }
    }
}
