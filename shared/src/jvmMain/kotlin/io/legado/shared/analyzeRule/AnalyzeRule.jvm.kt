package io.legado.shared.analyzeRule

import io.legado.shared.constant.AppPattern
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * JVM implementations of platform abstractions.
 * Uses Jsoup for HTML, Rhino for JS, HttpURLConnection for HTTP.
 * Same as Android but without Android-specific APIs.
 */

// ── HTML Parser ──────────────────────────────────────────────────

class JvmHtmlParser : HtmlParser {
    override fun parse(html: String): ParsedDocument = JvmParsedDoc(Jsoup.parse(html))
    override fun parseFragment(html: String): ParsedDocument = JvmParsedDoc(Jsoup.parseBodyFragment(html))
}

class JvmParsedDoc(private val doc: Document) : ParsedDocument {
    override fun selectCss(cssQuery: String): List<ParsedElement> {
        return try { doc.select(cssQuery).map { JvmParsedEl(it) } } catch (e: Exception) { emptyList() }
    }
    override fun selectXpath(xpath: String): List<ParsedElement> {
        return try {
            val css = xpathToCss(xpath)
            if (css != null) doc.select(css).map { JvmParsedEl(it) } else emptyList()
        } catch (e: Exception) { emptyList() }
    }
    override fun text(): String = doc.text()
    override fun html(): String = doc.html()

    private fun xpathToCss(xpath: String): String? = when {
        xpath.startsWith("//") -> xpath.removePrefix("//").replace("/", " > ")
        xpath.startsWith("/") -> xpath.removePrefix("/").replace("/", " > ")
        else -> null
    }
}

class JvmParsedEl(private val el: Element) : ParsedElement {
    override fun selectCss(cssQuery: String): List<ParsedElement> = el.select(cssQuery).map { JvmParsedEl(it) }
    override fun selectXpath(xpath: String): List<ParsedElement> = emptyList()
    override fun text(): String = el.text()
    override fun html(): String = el.html()
    override fun attr(name: String): String = el.attr(name)
    override fun ownText(): String = el.ownText()
}

// ── JSON Path Evaluator ──────────────────────────────────────────

class JvmJsonPathEvaluator : JsonPathEvaluator {
    override fun evaluate(json: String, path: String): Any? {
        return runCatching { JsonPathReader.read(json, path) }.getOrNull()
    }
    override fun evaluateToString(json: String, path: String): String {
        return evaluate(json, path)?.toString() ?: ""
    }
    override fun evaluateToList(json: String, path: String): List<String> {
        val result = evaluate(json, path)
        return when (result) {
            is List<*> -> result.mapNotNull { it?.toString() }
            else -> listOfNotNull(result?.toString())
        }
    }
}

// ── JS Evaluator (JVM Rhino) ────────────────────────────────────

class JvmJsEvaluator : JsEvaluator {
    override fun evaluate(script: String, context: Map<String, Any?>): Any? {
        val rhino = org.mozilla.javascript.Context.enter()
        rhino.optimizationLevel = -1
        rhino.languageVersion = org.mozilla.javascript.Context.VERSION_ES6
        
        return try {
            val scope: org.mozilla.javascript.Scriptable = org.mozilla.javascript.ImporterTopLevel(rhino)
            for ((key, value) in context) {
                val jsValue = org.mozilla.javascript.Context.javaToJS(value, scope)
                scope.put(key, scope, jsValue)
            }
            val result = rhino.evaluateString(scope, script, "rule", 1, null)
            org.mozilla.javascript.Context.jsToJava(result, Any::class.java)
        } catch (e: Exception) {
            null
        } finally {
            org.mozilla.javascript.Context.exit()
        }
    }

    override fun evaluateToString(script: String, context: Map<String, Any?>): String {
        return evaluate(script, context)?.toString() ?: ""
    }
}

// ── HTTP Requester ────────────────────────────────────────────────

class JvmHttpRequester : HttpRequester {
    override suspend fun get(url: String, headers: Map<String, String>): String {
        return runCatching {
            val conn = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 30000
                readTimeout = 30000
                headers.forEach { (k, v) -> setRequestProperty(k, v) }
            }
            conn.inputStream.bufferedReader().use { it.readText() }
        }.getOrDefault("")
    }

    override suspend fun post(url: String, body: String?, headers: Map<String, String>): String {
        return runCatching {
            val conn = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 30000
                readTimeout = 30000
                headers.forEach { (k, v) -> setRequestProperty(k, v) }
            }
            if (body != null) {
                conn.outputStream.use { it.write(body.toByteArray()) }
            }
            conn.inputStream.bufferedReader().use { it.readText() }
        }.getOrDefault("")
    }
}

// ── Platform Provider ────────────────────────────────────────────

actual class PlatformProvider actual constructor() {
    actual val htmlParser: HtmlParser = JvmHtmlParser()
    actual val jsonPathEvaluator: JsonPathEvaluator = JvmJsonPathEvaluator()
    actual val jsEvaluator: JsEvaluator = JvmJsEvaluator()
    actual val httpRequester: HttpRequester = JvmHttpRequester()
}
