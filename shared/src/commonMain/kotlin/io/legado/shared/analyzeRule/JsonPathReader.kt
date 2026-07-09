package io.legado.shared.analyzeRule

/**
 * Minimal JSON path reader - handles common JSONPath expressions.
 * Platform-agnostic: works on Android and JVM.
 */
object JsonPathReader {
    fun read(json: String, path: String): Any? {
        val jsonValue = parseJson(json)
        if (path == "$" || path.isEmpty()) return jsonValue

        val segments = parsePath(path)
        var current: Any? = jsonValue
        for (seg in segments) {
            current = when {
                seg == "*" -> current  // wildcard
                seg.startsWith("[") && seg.endsWith("]") -> {
                    val inner = seg.substring(1, seg.length - 1)
                    when {
                        inner == "*" -> current
                        inner.toIntOrNull() != null -> {
                            val idx = inner.toInt()
                            (current as? List<*>)?.getOrNull(idx)
                        }
                        inner.startsWith("'") && inner.endsWith("'") -> {
                            val key = inner.substring(1, inner.length - 1)
                            (current as? Map<*, *>)?.get(key)
                        }
                        else -> (current as? Map<*, *>)?.get(inner)
                    }
                }
                else -> (current as? Map<*, *>)?.get(seg)
            }
        }
        return current
    }

    private fun parsePath(path: String): List<String> {
        val segments = mutableListOf<String>()
        var buffer = StringBuilder()
        var bracketDepth = 0

        for (c in path) {
            when {
                c == '[' && bracketDepth == 0 -> {
                    if (buffer.isNotEmpty()) {
                        var seg = buffer.toString().trimStart('$', '.')
                        if (seg.isNotEmpty()) segments.add(seg)
                        buffer.clear()
                    }
                    bracketDepth++
                    buffer.append(c)
                }
                c == '[' -> { bracketDepth++; buffer.append(c) }
                c == ']' -> {
                    bracketDepth--
                    buffer.append(c)
                    if (bracketDepth == 0) {
                        segments.add(buffer.toString())
                        buffer.clear()
                    }
                }
                c == '.' && bracketDepth == 0 -> {
                    if (buffer.isNotEmpty()) {
                        var seg = buffer.toString().trimStart('$', '.')
                        if (seg.isNotEmpty()) segments.add(seg)
                        buffer.clear()
                    }
                }
                else -> buffer.append(c)
            }
        }
        if (buffer.isNotEmpty()) {
            var seg = buffer.toString().trimStart('$', '.')
            if (seg.isNotEmpty()) segments.add(seg)
        }
        return segments.filter { it.isNotEmpty() }
    }

    private fun parseJson(text: String): Any? {
        val trimmed = text.trim()
        return when {
            trimmed.startsWith("{") -> parseObject(trimmed)
            trimmed.startsWith("[") -> parseArray(trimmed)
            trimmed.startsWith("\"") -> parseString(trimmed)
            trimmed == "null" -> null
            trimmed == "true" -> true
            trimmed == "false" -> false
            trimmed.toDoubleOrNull() != null -> trimmed.toDouble()
            trimmed.toLongOrNull() != null -> trimmed.toLong()
            else -> trimmed
        }
    }

    private fun parseObject(text: String): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val regex = Regex("""["']([^"']+)["']\s*:\s*([^,}]+)""")
        regex.findAll(text).forEach { match ->
            val key = match.groupValues[1]
            val value = parseJson(match.groupValues[2].trim().trimEnd('}'))
            map[key] = value
        }
        return map
    }

    private fun parseArray(text: String): List<Any?> {
        val list = mutableListOf<Any?>()
        val content = text.substring(1, text.lastIndexOf(']').coerceAtLeast(1))
        if (content.isBlank()) return list

        var depth = 0
        var start = 0
        for (i in content.indices) {
            when (content[i]) {
                '{', '[' -> depth++
                '}', ']' -> depth--
                ',' -> {
                    if (depth == 0) {
                        list.add(parseJson(content.substring(start, i).trim()))
                        start = i + 1
                    }
                }
            }
        }
        if (start < content.length) {
            list.add(parseJson(content.substring(start).trim()))
        }
        return list
    }

    private fun parseString(text: String): String {
        val match = Regex("""^["'](.*)["']$""").find(text)
        return match?.groupValues?.get(1) ?: text
    }
}
