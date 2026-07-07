package io.legado.shared.utils


/**
 * URL/DNS utilities - migrated from Android NetworkUtils.
 * KMP-compatible: uses only commonMain APIs.
 */
object NetworkUtils {

    fun getAbsoluteURL(baseUrl: String, relativeUrl: String): String {
        if (relativeUrl.isBlank()) return baseUrl
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            return relativeUrl
        }
        if (baseUrl.isBlank()) return relativeUrl

        return when {
            relativeUrl.startsWith("//") -> {
                val scheme = baseUrl.substringBefore("://")
                "$scheme:$relativeUrl"
            }
            relativeUrl.startsWith("/") -> {
                val schemeEnd = baseUrl.indexOf("://")
                if (schemeEnd == -1) return relativeUrl
                val hostStart = schemeEnd + 3
                val pathStart = indexOfAny(baseUrl, charArrayOf('/', '?', '#'), hostStart)
                if (pathStart == -1) {
                    "$baseUrl$relativeUrl"
                } else {
                    baseUrl.substring(0, pathStart) + relativeUrl
                }
            }
            else -> {
                val schemeEnd = baseUrl.indexOf("://")
                if (schemeEnd == -1) return relativeUrl
                val hostStart = schemeEnd + 3
                val pathStart = indexOfAny(baseUrl, charArrayOf('/', '?', '#'), hostStart)
                if (pathStart == -1) {
                    "$baseUrl/$relativeUrl"
                } else {
                    baseUrl.substring(0, pathStart) + "/" + relativeUrl
                }
            }
        }
    }

    fun getBaseUrl(url: String): String? {
        if (url.isBlank()) return null
        val schemeEnd = url.indexOf("://")
        if (schemeEnd == -1) return null
        val hostStart = schemeEnd + 3
        val pathStart = indexOfAny(url, charArrayOf('/', '?', '#'), hostStart)
        return if (pathStart == -1) url else url.substring(0, pathStart)
    }

    fun getSubDomain(url: String): String? {
        val baseUrl = getBaseUrl(url) ?: return null
        val schemeEnd = baseUrl.indexOf("://")
        val host = if (schemeEnd != -1) baseUrl.substring(schemeEnd + 3) else baseUrl
        return host.substringBefore(':')
    }

    fun encodedQuery(query: String): Boolean {
        return query.contains(Regex("%[0-9A-Fa-f]{2}"))
    }

    fun encodedForm(value: String): Boolean {
        return value.contains(Regex("%[0-9A-Fa-f]{2}"))
    }

    private fun indexOfAny(s: String, chars: CharArray, startIndex: Int = 0): Int {
        for (i in startIndex until s.length) {
            if (chars.contains(s[i])) return i
        }
        return -1
    }
}

/**
 * URL and string encoding utilities.
 */
object EncoderUtils {

    fun escape(value: String): String {
        val sb = StringBuilder()
        for (c in value) {
            when (c) {
                ' ' -> sb.append("%20")
                '#' -> sb.append("%23")
                '$' -> sb.append("%24")
                '&' -> sb.append("%26")
                '+' -> sb.append("%2B")
                ',' -> sb.append("%2C")
                '/' -> sb.append("%2F")
                ':' -> sb.append("%3A")
                ';' -> sb.append("%3B")
                '=' -> sb.append("%3D")
                '?' -> sb.append("%3F")
                '@' -> sb.append("%40")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun urlEncode(value: String, charset: String = "UTF-8"): String {
        val sb = StringBuilder()
        val bytes = value.toByteArray(Charsets.UTF_8)
        for (b in bytes) {
            val c = b.toInt().toChar()
            when {
                c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' -> sb.append(c)
                c == '-' || c == '_' || c == '.' || c == '*' -> sb.append(c)
                c == ' ' -> sb.append('+')
                else -> {
                    sb.append('%')
                    sb.append(String.format("%02X", b.toInt() and 0xFF).uppercase())
                }
            }
        }
        return sb.toString()
    }

    fun queryEncode(value: String, charset: String = "UTF-8"): String {
        val sb = StringBuilder()
        val bytes = value.toByteArray(Charsets.UTF_8)
        for (b in bytes) {
            val c = b.toInt().toChar()
            when {
                c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' -> sb.append(c)
                c == '-' || c == '_' || c == '.' || c == '*' -> sb.append(c)
                c == ' ' -> sb.append("%20")
                else -> {
                    sb.append('%')
                    sb.append(String.format("%02X", b.toInt() and 0xFF).uppercase())
                }
            }
        }
        return sb.toString()
    }

    fun isJson(text: String): Boolean {
        val trimmed = text.trim()
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"))
    }

    fun isXml(text: String): Boolean {
        return text.trim().startsWith("<")
    }

    fun isJsonArray(text: String): Boolean {
        return text.trim().startsWith("[")
    }

    fun isJsonObject(text: String): Boolean {
        return text.trim().startsWith("{")
    }
}


