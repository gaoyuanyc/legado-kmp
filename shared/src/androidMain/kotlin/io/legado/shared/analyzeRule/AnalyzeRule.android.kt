package io.legado.shared.analyzeRule

import io.legado.shared.constant.AppPattern
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.mozilla.javascript.Context as RhinoContext
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ImporterTopLevel
import java.net.URL

/**
 * Android implementations of platform abstractions.
 * Uses Rhino for JS, Jsoup for HTML, and HttpURLConnection for HTTP.
 */

// ── HTML Parser ──────────────────────────────────────────────────

class AndroidHtmlParser : HtmlParser {
    override fun parse(html: String): ParsedDocument = AndroidParsedDoc(Jsoup.parse(html))
    override fun parseFragment(html: String): ParsedDocument = AndroidParsedDoc(Jsoup.parseBodyFragment(html))
}

class AndroidParsedDoc(private val doc: Document) : ParsedDocument {
    override fun selectCss(cssQuery: String): List<ParsedElement> {
        return try { doc.select(cssQuery).map { AndroidParsedEl(it) } } catch (e: Exception) { emptyList() }
    }
    override fun selectXpath(xpath: String): List<ParsedElement> {
        return try {
            val nodes = evaluateXpath(doc, xpath)
            nodes.map { AndroidParsedEl(it) }
        } catch (e: Exception) { emptyList() }
    }
    override fun text(): String = doc.text()
    override fun html(): String = doc.html()

    private fun evaluateXpath(context: Any, xpath: String): List<Element> {
        // Basic XPath to CSS conversion for common cases
        val css = xpathToCss(xpath)
        return if (css != null) try { doc.select(css) } catch (e: Exception) { emptyList() }
        else emptyList()
    }

    private fun xpathToCss(xpath: String): String? {
        // Handle common XPath patterns
        return when {
            xpath.startsWith("//") -> xpath.removePrefix("//").replace("/", " > ")
            xpath.startsWith("/") -> xpath.removePrefix("/").replace("/", " > ")
            xpath.contains("[@") -> {
                // Extract attribute selectors
                val regex = Regex("""([\w-]+)\[@([\w-]+)=['"]([^'"]+)['"]\]""")
                regex.replace(xpath) { "${it.groupValues[1]}[${it.groupValues[2]}=${it.groupValues[3]}]" }
            }
            else -> null
        }
    }
}

class AndroidParsedEl(private val el: Element) : ParsedElement {
    override fun selectCss(cssQuery: String): List<ParsedElement> = el.select(cssQuery).map { AndroidParsedEl(it) }
    override fun selectXpath(xpath: String): List<ParsedElement> = emptyList()
    override fun text(): String = el.text()
    override fun html(): String = el.html()
    override fun attr(name: String): String = el.attr(name)
    override fun ownText(): String = el.ownText()
}

// ── JSON Path Evaluator ──────────────────────────────────────────

class AndroidJsonPathEvaluator : JsonPathEvaluator {
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

// ── JS Evaluator (Android Rhino) ────────────────────────────────

class AndroidJsEvaluator : JsEvaluator {
    override fun evaluate(script: String, context: Map<String, Any?>): Any? {
        val rhino = RhinoContext.enter()
        rhino.optimizationLevel = -1  // Interpreter mode for Android
        rhino.languageVersion = RhinoContext.VERSION_ES6
        
        return try {
            val scope: Scriptable = ImporterTopLevel(rhino)
            
            // Inject context variables
            for ((key, value) in context) {
                val jsValue = RhinoContext.javaToJS(value, scope)
                scope.put(key, scope, jsValue)
            }
            
            // Execute
            val result = rhino.evaluateString(scope, script, "rule", 1, null)
            RhinoContext.jsToJava(result, Any::class.java)
        } catch (e: Exception) {
            // Log but don't crash
            null
        } finally {
            RhinoContext.exit()
        }
    }

    override fun evaluateToString(script: String, context: Map<String, Any?>): String {
        return evaluate(script, context)?.toString() ?: ""
    }
}

// ── HTTP Requester ────────────────────────────────────────────────

class AndroidHttpRequester : HttpRequester {
    override suspend fun get(url: String, headers: Map<String, String>): String {
        return runCatching {
            val conn = (URL(url).openConnection() as java.net.HttpURLConnection).apply {
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
            val conn = (URL(url).openConnection() as java.net.HttpURLConnection).apply {
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

actual class PlatformProvider {
    actual val htmlParser: HtmlParser = AndroidHtmlParser()
    actual val jsonPathEvaluator: JsonPathEvaluator = AndroidJsonPathEvaluator()
    actual val jsEvaluator: JsEvaluator = AndroidJsEvaluator()
    actual val httpRequester: HttpRequester = AndroidHttpRequester()
}
