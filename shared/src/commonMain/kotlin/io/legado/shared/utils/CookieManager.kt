package io.legado.shared.utils

class CookieManager {
    private val cookies = mutableMapOf<String, String>()

    fun getCookie(url: String): String? = cookies[url]

    fun setCookie(url: String, cookie: String?) {
        if (cookie.isNullOrEmpty()) {
            cookies.remove(url)
        } else {
            cookies[url] = cookie
        }
    }

    fun clearAll() { cookies.clear() }

    fun getCookieHeader(url: String): String? {
        val cookie = getCookie(url) ?: return null
        return cookie.split(";").joinToString("; ") { it.trim() }
    }

    fun saveCookiesFromHeader(url: String, setCookieHeader: String) {
        val cookies = setCookieHeader.split(",")
            .mapNotNull { entry ->
                val trimmed = entry.trim()
                val idx = trimmed.indexOf('=')
                if (idx > 0) trimmed.substring(0, idx + 1) else null
            }
            .joinToString("; ")
        if (cookies.isNotEmpty()) setCookie(url, cookies)
    }
}
