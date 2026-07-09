package io.legado.shared.readbook

import io.legado.shared.model.ReplaceRule

/**
 * Cross-platform reading engine for Legado.
 * Migrated from Android ReadBook.kt (1064 lines) - core reading logic only.
 *
 * Handles:
 * - Pagination (page splitting, next/prev page navigation)
 * - Reading progress calculation
 * - Chapter management
 * - Text processing for reading
 *
 * Platform-specific concerns (UI rendering, TTS, database) are delegated to platform layers.
 */
class ReadBookEngine(
    private var book: BookInfo? = null,
    private var config: ReadConfig = ReadConfig()
) {
    // ── Reading State ──────────────────────────────────────────────

    /** Current chapter index (0-based) */
    var currentChapterIndex: Int = 0
        private set

    /** Current position within chapter (character offset) */
    var currentPos: Int = 0
        private set

    /** Total chapters loaded */
    private val chapters = mutableListOf<ChapterContent>()

    /** Cached pages for current chapter */
    private var cachedPages: List<String> = emptyList()

    /** Current page index within chapter */
    private var currentPageIndex: Int = 0

    /** Total pages in current chapter */
    val totalPagesInChapter: Int get() = cachedPages.size

    /** Whether there is a next page */
    val hasNextPage: Boolean get() = currentPageIndex < cachedPages.size - 1 || currentChapterIndex < chapters.size - 1

    /** Whether there is a previous page */
    val hasPrevPage: Boolean get() = currentPageIndex > 0 || currentChapterIndex > 0

    // ── Public API ─────────────────────────────────────────────────

    /**
     * Load book data and initialize reading state.
     */
    fun loadBook(book: BookInfo, chapters: List<ChapterContent>) {
        this.book = book
        this.chapters.clear()
        this.chapters.addAll(chapters)
        this.currentChapterIndex = 0
        this.currentPos = 0
        this.currentPageIndex = 0
        if (chapters.isNotEmpty()) {
            loadChapterPages(0)
        }
    }

    /**
     * Get the current page content.
     */
    fun getCurrentPage(): String {
        return cachedPages.getOrElse(currentPageIndex) { "" }
    }

    /**
     * Move to the next page. Returns false if at the end.
     */
    fun nextPage(): Boolean {
        if (currentPageIndex < cachedPages.size - 1) {
            currentPageIndex++
            updatePosFromPage()
            return true
        }
        return nextChapter(atStart = true)
    }

    /**
     * Move to the previous page. Returns false if at the beginning.
     */
    fun prevPage(): Boolean {
        if (currentPageIndex > 0) {
            currentPageIndex--
            updatePosFromPage()
            return true
        }
        return prevChapter(atEnd = true)
    }

    /**
     * Move to a specific page in the current chapter.
     */
    fun goToPage(pageIndex: Int): Boolean {
        if (pageIndex in cachedPages.indices) {
            currentPageIndex = pageIndex
            updatePosFromPage()
            return true
        }
        return false
    }

    /**
     * Move to the next chapter.
     */
    fun nextChapter(atStart: Boolean = true): Boolean {
        if (currentChapterIndex < chapters.size - 1) {
            currentChapterIndex++
            currentPageIndex = if (atStart) 0 else (cachedPages.size - 1).coerceAtLeast(0)
            loadChapterPages(currentChapterIndex)
            if (!atStart) currentPageIndex = (cachedPages.size - 1).coerceAtLeast(0)
            updatePosFromPage()
            return true
        }
        return false
    }

    /**
     * Move to the previous chapter.
     */
    fun prevChapter(atEnd: Boolean = false): Boolean {
        if (currentChapterIndex > 0) {
            currentChapterIndex--
            loadChapterPages(currentChapterIndex)
            currentPageIndex = if (atEnd) (cachedPages.size - 1).coerceAtLeast(0) else 0
            updatePosFromPage()
            return true
        }
        return false
    }

    /**
     * Jump to a specific chapter by index.
     */
    fun goToChapter(chapterIndex: Int): Boolean {
        if (chapterIndex in chapters.indices) {
            currentChapterIndex = chapterIndex
            currentPageIndex = 0
            loadChapterPages(chapterIndex)
            updatePosFromPage()
            return true
        }
        return false
    }

    /**
     * Get reading progress as a percentage (0.0 to 1.0).
     */
    fun getProgress(): Double {
        if (chapters.isEmpty()) return 0.0
        val totalChars = chapters.sumOf { it.content.length }.toDouble()
        if (totalChars <= 0) return 0.0
        var readChars = 0.0
        for (i in 0 until currentChapterIndex) {
            readChars += chapters[i].content.length
        }
        readChars += currentPos
        return (readChars / totalChars).coerceIn(0.0, 1.0)
    }

    /**
     * Get current reading position info.
     */
    fun getPositionInfo(): PositionInfo {
        return PositionInfo(
            chapterIndex = currentChapterIndex,
            chapterTitle = chapters.getOrNull(currentChapterIndex)?.title ?: "",
            pageIndex = currentPageIndex,
            totalPages = cachedPages.size,
            posInChapter = currentPos,
            chapterLength = chapters.getOrNull(currentChapterIndex)?.content?.length ?: 0,
            progress = getProgress()
        )
    }

    /**
     * Update reading configuration.
     */
    fun updateConfig(config: ReadConfig) {
        this.config = config
        // Re-paginate current chapter with new config
        loadChapterPages(currentChapterIndex)
    }

    // ── Pagination Engine ─────────────────────────────────────────

    private fun loadChapterPages(chapterIndex: Int) {
        val chapter = chapters.getOrNull(chapterIndex) ?: run {
            cachedPages = emptyList()
            return
        }
        cachedPages = paginate(chapter.content, config)
        currentPageIndex = currentPageIndex.coerceIn(0, (cachedPages.size - 1).coerceAtLeast(0))
    }

    /**
     * Split chapter content into pages based on config.
     * Pure algorithm - no platform dependencies.
     */
    private fun paginate(content: String, config: ReadConfig): List<String> {
        if (content.isEmpty()) return listOf("")

        val pageSize = config.pageSize.coerceAtLeast(100)
        val pages = mutableListOf<String>()
        var start = 0
        val length = content.length

        while (start < length) {
            val end = findPageBreak(content, start, pageSize, config)
            pages.add(content.substring(start, end))
            start = end
        }

        return pages.ifEmpty { listOf("") }
    }

    /**
     * Find the optimal page break position.
     * Tries to break at paragraph boundaries, then sentence boundaries.
     */
    private fun findPageBreak(content: String, start: Int, targetSize: Int, config: ReadConfig): Int {
        val length = content.length
        val end = (start + targetSize).coerceAtMost(length)
        if (end >= length) return end

        // Try to break at paragraph (double newline)
        val paragraphBreak = content.lastIndexOf("\n\n", end)
        if (paragraphBreak > start && paragraphBreak >= start + targetSize / 2) {
            return paragraphBreak + 2
        }

        // Try to break at single newline
        val newlineBreak = content.lastIndexOf('\n', end)
        if (newlineBreak > start && newlineBreak >= start + targetSize / 2) {
            return newlineBreak + 1
        }

        // Try to break at sentence ending (Chinese or Western punctuation)
        val sentenceEndings = charArrayOf('。', '！', '？', '.', '!', '?', '」', '』', '"', '"', '\'', '\'')
        for (i in end downTo (start + targetSize / 2)) {
            if (content[i] in sentenceEndings) {
                return i + 1
            }
        }

        // Fallback: break at target size
        return end
    }

    private fun updatePosFromPage() {
        var pos = 0
        for (i in 0 until currentPageIndex) {
            pos += cachedPages.getOrElse(i) { "" }.length
        }
        currentPos = pos
    }

    // ── Text Processing ────────────────────────────────────────────

    /**
     * Apply content cleanup rules to raw text.
     */
    fun processContent(raw: String, rules: List<ReplaceRule>): String {
        var result = raw
        for (rule in rules) {
            if (!rule.isEnabled) continue
            result = runCatching {
                if (rule.isRegex) {
                    val regex = Regex(rule.pattern, RegexOption.MULTILINE)
                    regex.replace(result, rule.replacement)
                } else {
                    result.replace(rule.pattern, rule.replacement)
                }
            }.getOrDefault(result)
        }
        return result
    }

    /**
     * Strip HTML tags from content.
     */
    fun stripHtml(html: String): String {
        return html
            .replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<[^>]+>"), "")
            .replace(Regex("&\\w+;")) { match ->
                when (match.value) {
                    "&amp;" -> "&"
                    "&lt;" -> "<"
                    "&gt;" -> ">"
                    "&quot;" -> "\""
                    "&apos;" -> "'"
                    "&#39;" -> "'"
                    "&nbsp;" -> " "
                    else -> ""
                }
            }
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

// ── Data Models ───────────────────────────────────────────────────

data class BookInfo(
    val url: String = "",
    val name: String = "",
    val author: String = "",
    val tocUrl: String = "",
    val variableMap: MutableMap<String, String> = mutableMapOf()
)

data class ChapterContent(
    val title: String = "",
    val url: String = "",
    val content: String = "",
    val index: Int = 0,
    val tag: String? = null
)

data class ReadConfig(
    val pageSize: Int = 500,
    val textSize: Int = 18,
    val lineHeight: Int = 1
)

data class PositionInfo(
    val chapterIndex: Int,
    val chapterTitle: String,
    val pageIndex: Int,
    val totalPages: Int,
    val posInChapter: Int,
    val chapterLength: Int,
    val progress: Double
)
