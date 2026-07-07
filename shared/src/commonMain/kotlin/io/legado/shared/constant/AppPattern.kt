package io.legado.shared.constant

object AppPattern {
    val JS_PATTERN = Regex("<js>([\\w\\W]*?)</js>|@js:([\\w\\W]*)", RegexOption.IGNORE_CASE)
    val WebJS_PATTERN = Regex("@webjs:([\\w\\W]{5,})", RegexOption.IGNORE_CASE)
    val EXP_PATTERN = Regex("\\{\\{([\\w\\W]*?)\\}\\}")
    val imgPattern = Regex("<img[^>]*src=\"([^\"]*(?:\"[^>]+\\})?)\"[^>]*>")
    val useHtmlRegex = Regex("<usehtml>.*?</usehtml>", RegexOption.DOT_MATCHES_ALL)
    val dataUriRegex = Regex("^data:.*?;base64,(.*)")
    val imgRegex = Regex("(.*)((?:data|https?):[\\s\\S]+)$")
    val wordCountRegex = Regex("(?:^|字数[：:、]?|\\s+)([0-9万千百\\.]{1,6}字)")
    val noWordCountRegex = Regex("[\\s\\u200B-\\u200F\\uFEFF]")
    val domainRegex = Regex("^https?://([^:/]+)", RegexOption.IGNORE_CASE)
    val nameRegex = Regex("\\s+作\\s*者.*|\\s+\\S+\\s+著")
    val authorRegex = Regex("^\\s*作\\s*者[:：\\s]+|\\s+著")
    val fileNameRegex = Regex("[\\\\/:*?\"<>|.]")
    val fileNameRegex2 = Regex("[\\\\/:*?\"<>|]")
    val splitGroupRegex = Regex("[,;，；]")
    val titleNumPattern = Regex("(第)(.+?)(章)")
    val debugMessageSymbolRegex = Regex("[⇒◇┌└≡]")
    val bookFileRegex = Regex(".*\\.(txt|epub|umd|pdf|mobi|azw3|azw)", RegexOption.IGNORE_CASE)
    val archiveFileRegex = Regex(".*\\.(zip|rar|7z)$", RegexOption.IGNORE_CASE)
    val bdRegex = Regex("(\\p{P})+")
    val rnRegex = Regex("[\\r\\n]")
    val notReadAloudRegex = Regex("^(\\s|\\p{C}|\\p{P}|\\p{Z}|\\p{S})+$")
    val xmlContentTypeRegex = "(application|text)/\\w*\\+?xml.*".toRegex()
    val semicolonRegex = ";".toRegex()
    val equalsRegex = "=".toRegex()
    val spaceRegex = "\\s+".toRegex()
    val regexCharRegex = "[{}()\\[\\].+*?^$\\\\|]".toRegex()
    val LFRegex = "\n".toRegex()

    /**
     * Chinese numeral map.
     */
    val chnMap: Map<Char, Int> by lazy {
        val map = mutableMapOf<Char, Int>()
        val units = mapOf('百' to 100, '佰' to 100, '千' to 1000, '仟' to 1000, '万' to 10000, '亿' to 100000000)
        for ((char, value) in units) {
            map[char] = value
        }
        map['两'] = 2
        var cnStr = "零一二三四五六七八九十"
        for (i in cnStr.indices) {
            map[cnStr[i]] = i
        }
        cnStr = "〇壹贰叁肆伍陆柒捌玖拾"
        for (i in cnStr.indices) {
            map[cnStr[i]] = i
        }
        map.toMap()
    }
}
