package io.legado.shared.analyzeRule

import io.legado.shared.constant.AppPattern
import io.legado.shared.utils.EncoderUtils
import io.legado.shared.utils.NetworkUtils

/**
 * URL template analyzer for book source rules.
 * Parses URLs with placeholders like {key}, {page}, {{js}}, and URL options.
 *
 * Migrated from Android AnalyzeUrl (978 lines) - core URL parsing logic only.
 * HTTP execution is delegated to HttpHandler.
 */
class AnalyzeUrl(
    private val mUrl: String,
    private val key: String? = null,
    private val page: Int? = null,
    private var baseUrl: String = "",
    private val source: BaseSource? = null,
    headerMapF: Map<String, String>? = null,
    private val infoMap: MutableMap<String, String>? = null,
    private val jsEngine: PlatformJsEngine = PlatformJsEngine()
) {
    var ruleUrl = ""
        private set
    var url = ""
        private set
    var type: String? = null
        private set
    val headerMap = LinkedHashMap<String, String>()
    private var body: String? = null
    var urlNoQuery: String = ""
        private set
    private var method = RequestMethod.GET
    private var charset: String? = null
    private var retry = 0
    private var useWebView = false
    private var webJs: String? = null
    private var bodyJs: String? = null

    init {
        headerMapF?.let { headerMap.putAll(it) }
        initUrl()
    }

    private fun initUrl() {
        ruleUrl = mUrl
        analyzeJs()
        replaceKeyPageJs()
        analyzeUrl()
    }

    private fun analyzeJs() {
        var start = 0
        val jsMatcher = AppPattern.JS_PATTERN.findAll(ruleUrl).iterator()
        var result = ruleUrl
        for (match in jsMatcher) {
            if (match.range.first > start) {
                ruleUrl.substring(start, match.range.first).let {
                    if (it.isNotEmpty()) {
                        result = it.replace("@result", result)
                    }
                }
            }
            val jsCode = match.groupValues.getOrNull(2) ?: match.groupValues.getOrNull(1) ?: ""
            result = evalJS(jsCode, result).let { (it as? JsValue) ?: JsValue.String(it.toString()) }.asString()
            start = match.range.last + 1
        }
        if (ruleUrl.length > start) {
            ruleUrl.substring(start).let {
                if (it.isNotEmpty()) {
                    result = it.replace("@result", result)
                }
            }
        }
        ruleUrl = result
    }

    private fun replaceKeyPageJs() {
        // Handle embedded {{js}} rules
        if (ruleUrl.contains("{{") && ruleUrl.contains("}}")) {
            val sb = StringBuilder()
            var pos = 0
            while (pos < ruleUrl.length) {
                val start = ruleUrl.indexOf("{{", pos)
                if (start == -1) {
                    sb.append(ruleUrl.substring(pos))
                    break
                }
                val end = ruleUrl.indexOf("}}", start + 2)
                if (end == -1) {
                    sb.append(ruleUrl.substring(pos))
                    break
                }
                sb.append(ruleUrl.substring(pos, start))
                val jsCode = ruleUrl.substring(start + 2, end)
                val jsResult = evalJS(jsCode)
                sb.append(when (jsResult) {
                    is JsValue.String -> jsResult.value
                    is JsValue.Number -> if (jsResult.value % 1.0 == 0.0) String.format("%.0f", jsResult.value) else jsResult.value.toString()
                    is JsValue -> jsResult.asString()
                    else -> jsResult.toString()
                })
                pos = end + 2
            }
            if (sb.isNotEmpty()) ruleUrl = sb.toString()
        }

        // Handle page parameter
        page?.let { pg ->
            val pagePattern = Regex("""\{(\d+(?:,\d+)*)}""")
            val matcher = pagePattern.find(ruleUrl)
            matcher?.let { match ->
                val pages = match.groupValues[1].split(",")
                ruleUrl = ruleUrl.replace(
                    match.value,
                    if (pg <= pages.size) pages[pg - 1].trim() else pages.last().trim()
                )
            }
        }
    }

    private fun analyzeUrl() {
        // Separate URL from options
        val paramPattern = Regex(""",\s*\{.*}$""")
        val match = paramPattern.find(ruleUrl)
        val urlNoOption = if (match != null) ruleUrl.substring(0, match.range.first) else ruleUrl
        url = NetworkUtils.getAbsoluteURL(baseUrl, urlNoOption)
        NetworkUtils.getBaseUrl(url)?.let { baseUrl = it }

        if (match != null) {
            val urlOptionStr = ruleUrl.substring(match.range.first + 1).trim()
            parseUrlOption(urlOptionStr)
        }

        urlNoQuery = url
        when (method) {
            RequestMethod.POST -> body?.let {
                if (!EncoderUtils.isJson(it) && !EncoderUtils.isXml(it) && headerMap["Content-Type"].isNullOrEmpty()) {
                    analyzeFields(it)
                }
            }
            else -> {
                val pos = url.indexOf('?')
                if (pos != -1) {
                    analyzeQuery(url.substring(pos + 1))
                    urlNoQuery = url.substring(0, pos)
                }
            }
        }
    }

    private fun parseUrlOption(jsonStr: String) {
        runCatching {
            UrlOption.parse(jsonStr)?.let { option ->
                option.getMethod()?.let { methodStr ->
                    method = when (methodStr.uppercase()) {
                        "POST" -> RequestMethod.POST
                        "HEAD" -> RequestMethod.HEAD
                        else -> RequestMethod.GET
                    }
                }
                option.getHeaderMap()?.forEach { (k, v) ->
                    headerMap[k.toString()] = v.toString()
                }
                option.getBody()?.let { body = it }
                option.getType()?.let { type = it }
                option.getCharset()?.let { charset = it }
                option.getRetry()?.let { retry = it }
                option.useWebView()?.let { useWebView = it }
                option.getWebJs()?.let { webJs = it }
                option.getBodyJs()?.let { bodyJs = it }
            }
        }
    }

    private fun analyzeFields(fieldsTxt: String) {
        encodedForm = encodeParams(fieldsTxt, charset, false)
    }

    private fun analyzeQuery(query: String) {
        encodedQuery = encodeParams(query, charset, true)
    }

    private var encodedForm: String? = null
    private var encodedQuery: String? = null

    fun getEncodedForm(): String? = encodedForm
    fun getEncodedQuery(): String? = encodedQuery
    fun getMethod(): String = method.name
    fun getBody(): String? = body
    fun getCharset(): String? = charset
    fun getRetry(): Int = retry
    fun getWebJs(): String? = webJs
    fun getBodyJs(): String? = bodyJs
    fun useWebView(): Boolean = useWebView

    private fun encodeParams(params: String, charset: String?, isQuery: Boolean): String {
        val checkEncoded = charset.isNullOrEmpty()
        val resolvedCharset = when {
            charset.isNullOrEmpty() -> "UTF-8"
            charset == "escape" -> null
            else -> charset
        }
        if (isQuery && resolvedCharset != null) {
            if (NetworkUtils.encodedQuery(params)) return params
            return EncoderUtils.queryEncode(params, resolvedCharset)
        }
        val len = params.length
        val sb = StringBuilder()
        var pos = 0
        while (pos <= len) {
            if (sb.isNotEmpty()) sb.append("&")
            var ampOffset = params.indexOf("&", pos)
            if (ampOffset == -1) ampOffset = len
            val eqOffset = params.indexOf("=", pos)
            val key: String
            val value: String?
            if (eqOffset == -1 || eqOffset > ampOffset) {
                key = params.substring(pos, ampOffset)
                value = null
            } else {
                key = params.substring(pos, eqOffset)
                value = params.substring(eqOffset + 1, ampOffset)
            }
            appendEncoded(sb, key, checkEncoded, resolvedCharset)
            if (value != null) {
                sb.append("=")
                appendEncoded(sb, value, checkEncoded, resolvedCharset)
            }
            pos = ampOffset + 1
        }
        return sb.toString()
    }

    private fun appendEncoded(sb: StringBuilder, value: String, checkEncoded: Boolean, charset: String?) {
        when {
            checkEncoded && NetworkUtils.encodedForm(value) -> sb.append(value)
            charset == null -> sb.append(EncoderUtils.escape(value))
            else -> sb.append(EncoderUtils.urlEncode(value, charset))
        }
    }

    private fun evalJS(jsStr: String, result: Any? = null): Any? {
        jsEngine.injectCompatShim()
        jsEngine.bindObject("baseUrl", baseUrl)
        jsEngine.bindObject("page", page)
        jsEngine.bindObject("key", key)
        jsEngine.bindObject("result", result)
        jsEngine.bindObject("java", this)
        jsEngine.bindObject("infoMap", infoMap)
        jsEngine.bindObject("source", source)
        return jsEngine.evaluate(jsStr)
    }
}

enum class RequestMethod { GET, POST, HEAD }

/**
 * Stub for source information - platform-specific
 */
interface BaseSource {
    fun getKey(): String?
    fun getHeaderMap(hasLogin: Boolean = true): Map<String, String>?
    fun getShareScope(context: Any?): Any?
}





