package io.legado.shared.analyzeRule

import io.legado.shared.utils.EncoderUtils

/**
 * URL options parsed from the JSON after a comma in rule URLs.
 * Example: searchUrl = "https://example.com/search?q={key},{"method":"POST","header":{"User-Agent":"test"}}"
 */
data class UrlOption(
    private val method: String? = null,
    private val header: Map<String, Any?>? = null,
    private val body: String? = null,
    private val type: String? = null,
    private val charset: String? = null,
    private val retry: Int? = null,
    private val useWebView: Boolean? = null,
    private val webJs: String? = null,
    private val bodyJs: String? = null,
    private val js: String? = null,
    private val dnsIp: String? = null,
    private val serverID: String? = null,
    private val webViewDelayTime: Int? = null
) {
    fun getMethod(): String? = method
    fun getHeaderMap(): Map<String, Any?>? = header
    fun getBody(): String? = body
    fun getType(): String? = type
    fun getCharset(): String? = charset
    fun getRetry(): Int? = (retry ?: 0)
    fun useWebView(): Boolean? = useWebView
    fun getWebJs(): String? = webJs
    fun getBodyJs(): String? = bodyJs
    fun getJs(): String? = js
    fun getDnsIp(): String? = dnsIp
    fun getServerID(): String? = serverID
    fun getWebViewDelayTime(): Int? = webViewDelayTime

    companion object {
        /**
         * Parse UrlOption from JSON string.
         * Uses simple JSON parsing without Gson dependency.
         */
        fun parse(jsonStr: String): UrlOption? {
            if (!EncoderUtils.isJson(jsonStr)) return null
            return parseJsonObject(jsonStr.trim())
        }

        private fun parseJsonObject(json: String): UrlOption? {
            val map = mutableMapOf<String, Any?>()
            parseJsonToMap(json, map)
            return UrlOption(
                method = map["method"] as? String,
                header = map["header"] as? Map<String, Any?>,
                body = map["body"] as? String,
                type = map["type"] as? String,
                charset = map["charset"] as? String,
                retry = (map["retry"] as? Double)?.toInt(),
                useWebView = map["webView"] as? Boolean,
                webJs = map["webJs"] as? String,
                bodyJs = map["bodyJs"] as? String,
                js = map["js"] as? String,
                dnsIp = map["dnsIp"] as? String,
                serverID = map["serverID"] as? String,
                webViewDelayTime = (map["webViewDelayTime"] as? Double)?.toInt()
            )
        }

        private fun parseJsonToMap(json: String, map: MutableMap<String, Any?>) {
            // Simple JSON parser for flat/nested objects
            var i = 0
            val len = json.length
            while (i < len) {
                // Find key
                val keyStart = indexOfQuote(json, i)
                if (keyStart == -1) break
                val keyEnd = indexOfQuote(json, keyStart + 1)
                if (keyEnd == -1) break
                val key = json.substring(keyStart + 1, keyEnd)

                // Find colon
                var colonPos = keyEnd + 1
                while (colonPos < len && json[colonPos] != ':') colonPos++
                if (colonPos == len) break

                // Find value
                colonPos++ // skip colon
                while (colonPos < len && json[colonPos].isWhitespace()) colonPos++
                if (colonPos == len) break

                when {
                    json[colonPos] == '{' -> {
                        // Nested object
                        val endIndex = findMatchingBrace(json, colonPos)
                        if (endIndex != -1) {
                            val nested = json.substring(colonPos, endIndex + 1)
                            val nestedMap = mutableMapOf<String, Any?>()
                            parseJsonToMap(nested, nestedMap)
                            map[key] = nestedMap
                            i = endIndex + 1
                        } else {
                            break
                        }
                    }
                    json[colonPos] == '[' -> {
                        // Array - simplified
                        val endIndex = findMatchingBracket(json, colonPos)
                        if (endIndex != -1) {
                            map[key] = json.substring(colonPos, endIndex + 1)
                            i = endIndex + 1
                        } else {
                            break
                        }
                    }
                    json[colonPos] == '"' -> {
                        val endQuote = indexOfQuote(json, colonPos + 1)
                        if (endQuote != -1) {
                            map[key] = json.substring(colonPos + 1, endQuote)
                            i = endQuote + 1
                        } else {
                            break
                        }
                    }
                    else -> {
                        // Number or boolean
                        val endPos = findValueEnd(json, colonPos)
                        val valueStr = json.substring(colonPos, endPos)
                        map[key] = parseValue(valueStr)
                        i = endPos
                    }
                }
            }
        }

        private fun indexOfQuote(s: String, start: Int): Int {
            for (i in start until s.length) {
                if (s[i] == '"') return i
            }
            return -1
        }

        private fun findMatchingBrace(s: String, start: Int): Int {
            var depth = 0
            for (i in start until s.length) {
                when (s[i]) {
                    '{' -> depth++
                    '}' -> { depth--; if (depth == 0) return i }
                }
            }
            return -1
        }

        private fun findMatchingBracket(s: String, start: Int): Int {
            var depth = 0
            for (i in start until s.length) {
                when (s[i]) {
                    '[' -> depth++
                    ']' -> { depth--; if (depth == 0) return i }
                }
            }
            return -1
        }

        private fun findValueEnd(s: String, start: Int): Int {
            var i = start
            while (i < s.length && s[i] != ',' && s[i] != '}' && s[i] != ']') i++
            return i
        }

        private fun parseValue(s: String): Any {
            val trimmed = s.trim()
            return when {
                trimmed == "true" -> true
                trimmed == "false" -> false
                trimmed.toDoubleOrNull() != null -> trimmed.toDouble()
                else -> trimmed
            }
        }
    }
}
