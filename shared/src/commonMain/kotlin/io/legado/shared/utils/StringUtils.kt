package io.legado.shared.utils

import io.legado.shared.constant.AppPattern
import okio.Buffer
import okio.GzipSink
import okio.GzipSource

object StringUtils {

    fun encodeBase64(data: ByteArray): String = encodeBase64Impl(data)

    fun decodeBase64(str: String): ByteArray = decodeBase64Impl(str)

    fun compressString(data: String): ByteArray {
        val buffer = Buffer()
        GzipSink(buffer).use { gzip ->
            gzip.write(Buffer().writeUtf8(data), data.toByteArray().size.toLong())
        }
        return buffer.readByteArray()
    }

    fun decompressString(data: ByteArray): String {
        val source = GzipSource(Buffer().write(data))
        return Buffer().use { result ->
            result.writeAll(source)
            result.readUtf8()
        }
    }

    fun trim(s: String?): String = s?.trim() ?: ""

    fun splitNotBlank(text: String, regex: Regex): List<String> {
        return text.split(regex).filter { it.isNotBlank() }
    }

    fun formatFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = size.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        return String.format("%.1f%s", value, units[unitIndex])
    }

    fun getDomain(url: String): String? {
        return AppPattern.domainRegex.find(url)?.groupValues?.getOrNull(1)
    }

    fun splitRule(rule: String, separator: String = ";"): List<String> {
        return rule.split(separator).map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun stripHtmlTags(html: String): String {
        return html.replace(Regex("<[^>]*>"), "")
    }

    fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }
}

expect fun encodeBase64Impl(data: ByteArray): String

expect fun decodeBase64Impl(str: String): ByteArray
