package io.legado.shared.analyzeRule

/**
 * Native stub implementations for HarmonyOS.
 * These throw UnsupportedOperationException - actual implementations
 * require the custom Kotlin-OHOS toolchain with working harmonyOSArm64 target.
 * Once the .so is compiled, these will be replaced with real implementations.
 */

class NativeHtmlParser : HtmlParser {
    override fun parse(html: String): ParsedDocument = 
        throw UnsupportedOperationException("HarmonyOS native parser requires Kotlin-OHOS toolchain")
    override fun parseFragment(html: String): ParsedDocument = 
        throw UnsupportedOperationException("HarmonyOS native parser requires Kotlin-OHOS toolchain")
}

class NativeDoc : ParsedDocument {
    override fun selectCss(cssQuery: String): List<ParsedElement> = emptyList()
    override fun selectXpath(xpath: String): List<ParsedElement> = emptyList()
    override fun text(): String = ""
    override fun html(): String = ""
}

class NativeEl : ParsedElement {
    override fun selectCss(cssQuery: String): List<ParsedElement> = emptyList()
    override fun selectXpath(xpath: String): List<ParsedElement> = emptyList()
    override fun text(): String = ""
    override fun html(): String = ""
    override fun attr(name: String): String = ""
    override fun ownText(): String = ""
}

class NativeJsonPathEvaluator : JsonPathEvaluator {
    override fun evaluate(json: String, path: String): Any? = null
    override fun evaluateToString(json: String, path: String): String = ""
    override fun evaluateToList(json: String, path: String): List<String> = emptyList()
}

class NativeJsEvaluator : JsEvaluator {
    override fun evaluate(script: String, context: Map<String, Any?>): Any? = null
    override fun evaluateToString(script: String, context: Map<String, Any?>): String = ""
}

class NativeHttpRequester : HttpRequester {
    override suspend fun get(url: String, headers: Map<String, String>): String = ""
    override suspend fun post(url: String, body: String?, headers: Map<String, String>): String = ""
}

actual class PlatformProvider actual constructor() {
    actual val htmlParser: HtmlParser = NativeHtmlParser()
    actual val jsonPathEvaluator: JsonPathEvaluator = NativeJsonPathEvaluator()
    actual val jsEvaluator: JsEvaluator = NativeJsEvaluator()
    actual val httpRequester: HttpRequester = NativeHttpRequester()
}
