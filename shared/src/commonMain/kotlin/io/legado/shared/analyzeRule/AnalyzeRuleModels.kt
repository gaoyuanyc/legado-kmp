package io.legado.shared.analyzeRule

/**
 * Parsed rule component with its processing mode.
 * Platform-agnostic data model for book source rules.
 */
data class SourceRule(
    val rule: String,
    val mode: Mode = Mode.Default,
    val replaceRegex: String = "",
    val putMap: Map<String, String> = emptyMap(),
    val variableMap: MutableMap<String, String> = mutableMapOf()
) {
    fun getParamSize(): Int = 0
}

/**
 * Processing modes for rule evaluation.
 */
enum class Mode {
    Default,    // CSS selector / XPath
    Regex,      // Regular expression
    Js,         // JavaScript (@js: or <js>)
    WebJs,      // Web JavaScript (@webjs:)
    JsonPath,   // JSON path expression
    XPath,      // XPath expression
    AllInOne    // All-in-one mode (legacy)
}

/**
 * Result container for rule evaluation.
 */
data class RuleResult(
    val strings: List<String> = emptyList(),
    val elements: List<Any> = emptyList()
)

/**
 * Abstraction for HTML/XML parsing across platforms.
 * Android: Jsoup implementation
 * HarmonyOS: Native HTML parsing
 */
interface HtmlParser {
    fun parse(html: String): ParsedDocument
    fun parseFragment(html: String): ParsedDocument
}

interface ParsedDocument {
    fun selectCss(cssQuery: String): List<ParsedElement>
    fun selectXpath(xpath: String): List<ParsedElement>
    fun text(): String
    fun html(): String
}

interface ParsedElement {
    fun selectCss(cssQuery: String): List<ParsedElement>
    fun selectXpath(xpath: String): List<ParsedElement>
    fun text(): String
    fun html(): String
    fun attr(name: String): String
    fun ownText(): String
}

/**
 * Abstraction for JSON path evaluation.
 */
interface JsonPathEvaluator {
    fun evaluate(json: String, path: String): Any?
    fun evaluateToString(json: String, path: String): String
    fun evaluateToList(json: String, path: String): List<String>
}

/**
 * Abstraction for JavaScript execution.
 */
interface JsEvaluator {
    fun evaluate(script: String, context: Map<String, Any?> = emptyMap()): Any?
    fun evaluateToString(script: String, context: Map<String, Any?> = emptyMap()): String
}

/**
 * Abstraction for HTTP requests.
 */
interface HttpRequester {
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): String
    suspend fun post(url: String, body: String? = null, headers: Map<String, String> = emptyMap()): String
}

/**
 * Platform provider that wires up all abstractions.
 */
expect class PlatformProvider() {
    val htmlParser: HtmlParser
    val jsonPathEvaluator: JsonPathEvaluator
    val jsEvaluator: JsEvaluator
    val httpRequester: HttpRequester
}

object AnalyzeRulePlatform {
    lateinit var provider: PlatformProvider
    
    fun init(provider: PlatformProvider) {
        this.provider = provider
    }
}
