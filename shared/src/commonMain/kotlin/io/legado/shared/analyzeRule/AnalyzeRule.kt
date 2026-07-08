package io.legado.shared.analyzeRule

import io.legado.shared.constant.AppPattern
import io.legado.shared.utils.NetworkUtils
import io.legado.shared.utils.EncoderUtils

/**
 * Cross-platform AnalyzeRule - core rule parsing engine for book source rules.
 *
 * Migrated from Android AnalyzeRule (978 lines) while maintaining the same API contract.
 * Platform-specific operations (JS execution, HTML parsing, JSON path) are delegated
 * to the PlatformProvider abstraction.
 *
 * @param content The raw content to process (HTML string, JSON string, etc.)
 * @param baseUrl Base URL for resolving relative links
 * @param source Optional book source reference
 */
class AnalyzeRule(
    private var content: Any? = null,
    private var baseUrl: String = "",
    private val source: SourceInfo? = null
) {
    // ── State ──────────────────────────────────────────────────────
    
    /** Variable storage for rule evaluation context */
    private val variableMap = mutableMapOf<String, String>()
    
    /** Cached rule decompositions */
    private val ruleCache = mutableMapOf<String, List<SourceRule>>()
    
    /** Whether this rule chain uses regex mode */
    var isRegex: Boolean = false
        private set
    
    /** Redirect URL tracking for nested rule evaluation */
    var redirectUrl: String? = null

    // ── Public API ─────────────────────────────────────────────────

    /**
     * Set the content to be processed.
     */
    fun setContent(content: Any?, baseUrl: String? = null): AnalyzeRule {
        this.content = content
        if (baseUrl != null) this.baseUrl = baseUrl
        return this
    }

    /**
     * Set base URL for relative link resolution.
     */
    fun setBaseUrl(baseUrl: String?): AnalyzeRule {
        if (baseUrl != null) this.baseUrl = baseUrl
        return this
    }

    /**
     * Extract a single string result from processed content.
     */
    fun getString(ruleStr: String?, mContent: Any? = null, isUrl: Boolean = false): String {
        if (ruleStr.isNullOrBlank()) return ""
        val ruleList = splitSourceRuleCached(ruleStr)
        return getStringInternal(ruleList, mContent, isUrl, unescape = true)
    }

    /**
     * Extract a single string with optional unescaping.
     */
    fun getString(ruleStr: String?, unescape: Boolean): String {
        if (ruleStr.isNullOrBlank()) return ""
        val ruleList = splitSourceRuleCached(ruleStr)
        return getStringInternal(ruleList, null, false, unescape)
    }

    /**
     * Extract multiple string results.
     */
    fun getStringList(ruleStr: String?, mContent: Any? = null, isUrl: Boolean = false): List<String>? {
        if (ruleStr.isNullOrBlank()) return emptyList()
        val ruleList = splitSourceRuleCached(ruleStr)
        val result = extractElements(ruleList, mContent, isUrl) ?: return emptyList()
        return when (result) {
            is List<*> -> result.mapNotNull { it?.toString() }
            else -> listOf(result.toString())
        }
    }

    /**
     * Extract content element(s) with full type preservation.
     */
    fun getElement(ruleStr: String): Any? {
        if (ruleStr.isBlank()) return null
        val ruleList = splitSourceRule(ruleStr, allInOne = true)
        return extractElement(ruleList, isElements = false)
    }

    /**
     * Extract list of content elements.
     */
    fun getElements(ruleStr: String): List<Any> {
        if (ruleStr.isBlank()) return emptyList()
        val ruleList = splitSourceRule(ruleStr, allInOne = true)
        val result = extractElement(ruleList, isElements = true)
        return when (result) {
            is List<*> -> result.filterNotNull()
            else -> if (result != null) listOf(result) else emptyList()
        }
    }

    /**
     * Store a variable in the evaluation context.
     */
    fun put(key: String, value: String): String {
        variableMap[key] = value
        return value
    }

    /**
     * Retrieve a variable from the evaluation context.
     */
    fun get(key: String): String {
        return variableMap[key] ?: ""
    }

    /**
     * Execute JavaScript code within the current context.
     */
    fun evalJS(jsStr: String, result: Any? = null): Any? {
        val evaluator = AnalyzeRulePlatform.provider.jsEvaluator

        // Build JS context
        val jsContext = mutableMapOf<String, Any?>().apply {
            putAll(variableMap)
            put("java", JsBridge(this@AnalyzeRule))
            put("baseUrl", baseUrl)
            put("content", content)
            put("result", result)
            put("source", source)
        }

        return evaluator.evaluate(jsStr, jsContext)
    }

    // ── Rule Decomposition ────────────────────────────────────────

    /**
     * Split a rule string into processable SourceRule components.
     * This is the core method that parses: CSS selectors, XPath, Regex, JS blocks.
     */
    fun splitSourceRule(ruleStr: String?, allInOne: Boolean = false): List<SourceRule> {
        if (ruleStr.isNullOrEmpty()) return emptyList()

        val ruleList = ArrayList<SourceRule>()
        var mode: Mode = Mode.Default
        var start = 0

        // Handle allInOne mode (starts with ':')
        if (allInOne && ruleStr.startsWith(":")) {
            mode = Mode.Regex
            isRegex = true
            start = 1
        } else if (isRegex) {
            mode = Mode.Regex
        }

        var tmp: String

        // Process @js: and <js></js> blocks
        val jsMatcher = AppPattern.JS_PATTERN.findAll(ruleStr)
        for (match in jsMatcher) {
            if (match.range.first > start) {
                tmp = ruleStr.substring(start, match.range.first).trim()
                if (tmp.isNotEmpty()) {
                    ruleList.add(SourceRule(tmp, mode))
                }
            }
            val jsCode = match.groupValues.getOrNull(2) ?: match.groupValues.getOrNull(1) ?: ""
            ruleList.add(SourceRule(jsCode, Mode.Js))
            start = match.range.last + 1
        }

        // Process @webjs: blocks
        val webJsMatcher = AppPattern.WebJS_PATTERN.findAll(ruleStr)
        for (match in webJsMatcher) {
            if (match.range.first > start) {
                tmp = ruleStr.substring(start, match.range.first).trim()
                if (tmp.isNotEmpty()) {
                    ruleList.add(SourceRule(tmp, mode))
                }
            }
            ruleList.add(SourceRule(match.groupValues.getOrElse(1) { "" }, Mode.WebJs))
            start = match.range.last + 1
        }

        // Remaining content
        if (ruleStr.length > start) {
            tmp = ruleStr.substring(start).trim()
            if (tmp.isNotEmpty()) {
                ruleList.add(SourceRule(tmp, mode))
            }
        }

        return ruleList
    }

    // ── Internal Processing ────────────────────────────────────────

    private fun splitSourceRuleCached(ruleStr: String): List<SourceRule> {
        return ruleCache.getOrPut(ruleStr) { splitSourceRule(ruleStr) }
    }

    private fun getStringInternal(
        ruleList: List<SourceRule>,
        mContent: Any?,
        isUrl: Boolean,
        unescape: Boolean
    ): String {
        var result: Any? = extractElement(ruleList, isElements = false, mContent)
        if (result == null) result = ""

        var resultStr = result.toString()

        // HTML unescape if needed
        if (unescape && resultStr.contains('&')) {
            resultStr = unescapeHtml(resultStr)
        }

        // URL resolution
        if (isUrl) {
            return if (resultStr.isBlank()) {
                baseUrl
            } else {
                NetworkUtils.getAbsoluteURL(redirectUrl ?: baseUrl, resultStr)
            }
        }

        return resultStr
    }

    private fun extractElement(
        ruleList: List<SourceRule>,
        isElements: Boolean,
        mContent: Any? = null
    ): Any? {
        var result: Any? = mContent ?: content
        if (result == null || ruleList.isEmpty()) return null

        val provider = AnalyzeRulePlatform.provider

        for (sourceRule in ruleList) {
            result ?: continue

            when (sourceRule.mode) {
                Mode.WebJs -> {
                    result = evalJS(sourceRule.rule, result)
                }

                Mode.Js -> {
                    result = evalJS(sourceRule.rule, result)
                }

                Mode.JsonPath -> {
                    val jsonStr = when (result) {
                        is String -> result
                        else -> result.toString()
                    }
                    result = if (isElements) {
                        provider.jsonPathEvaluator.evaluateToList(jsonStr, sourceRule.rule)
                    } else {
                        provider.jsonPathEvaluator.evaluateToString(jsonStr, sourceRule.rule)
                    }
                }

                Mode.XPath -> {
                    val doc = provider.htmlParser.parse(result.toString())
                    result = if (isElements) {
                        doc.selectXpath(sourceRule.rule)
                    } else {
                        doc.selectXpath(sourceRule.rule).firstOrNull()
                    }
                }

                Mode.Regex -> {
                    val text = result.toString()
                    result = if (isElements) {
                        AnalyzeByRegex.getElements(text, sourceRule.rule.split("&&"))
                    } else {
                        AnalyzeByRegex.getElement(text, sourceRule.rule.split("&&"))
                    }
                }

                Mode.Default -> {
                    val doc = provider.htmlParser.parse(result.toString())
                    result = if (isElements) {
                        doc.selectCss(sourceRule.rule)
                    } else {
                        doc.selectCss(sourceRule.rule).firstOrNull()?.text() ?: ""
                    }
                }

                Mode.AllInOne -> {
                    result = AnalyzeByRegex.getElement(result.toString(), listOf(sourceRule.rule))
                }
            }

            // Apply post-processing regex
            if (result != null && sourceRule.replaceRegex.isNotEmpty()) {
                result = applyReplaceRegex(result.toString(), sourceRule.replaceRegex)
            }
        }

        return result
    }

    private fun extractElements(
        ruleList: List<SourceRule>,
        mContent: Any?,
        isUrl: Boolean
    ): Any? {
        return extractElement(ruleList, isElements = true, mContent)
    }

    // ── Utilities ──────────────────────────────────────────────────

    private fun unescapeHtml(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&#x27;", "'")
            .replace("&#x2F;", "/")
            .replace("<br>", "\n")
            .replace("<br/>", "\n")
            .replace("<br />", "\n")
            .replace("</p>", "\n")
            .replace("</div>", "\n")
    }

    private fun applyReplaceRegex(text: String, regex: String): String {
        return runCatching {
            val parts = regex.split("##")
            if (parts.size == 2) {
                val pattern = Regex(parts[0])
                pattern.replace(text, parts[1])
            } else {
                text
            }
        }.getOrDefault(text)
    }

    // ── Inner Classes ──────────────────────────────────────────────

    /**
     * Bridge object exposed to JS context for Java interoperability.
     */
    class JsBridge(private val analyzeRule: AnalyzeRule) {
        fun put(key: String, value: String) = analyzeRule.put(key, value)
        fun get(key: String) = analyzeRule.get(key)
        fun getString(rule: String) = analyzeRule.getString(rule)
        fun getBaseUrl() = analyzeRule.baseUrl
    }
}

/**
 * Lightweight source info for cross-platform use.
 */
data class SourceInfo(
    val url: String = "",
    val name: String = "",
    val rule: String = "",
    val variables: MutableMap<String, String> = mutableMapOf()
)

// ── Regex-based analysis (pure Kotlin, no platform deps) ──────────

object AnalyzeByRegex {
    fun getElement(text: String, rules: List<String>): String {
        var result = text
        for (rule in rules) {
            if (rule.isBlank()) continue
            result = runCatching {
                val regex = Regex(rule)
                regex.find(result)?.value ?: ""
            }.getOrDefault("")
        }
        return result
    }

    fun getElements(text: String, rules: List<String>): List<String> {
        var result: Any = text
        for (rule in rules) {
            if (rule.isBlank()) continue
            result = runCatching {
                val regex = Regex(rule)
                if (rule.contains("(")) {
                    regex.findAll(result as String).map { it.value }.toList()
                } else {
                    regex.findAll(result as String).map { it.value }.toList()
                }
            }.getOrDefault(emptyList<String>())
        }
        return result as? List<String> ?: emptyList()
    }
}
