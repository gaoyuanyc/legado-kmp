package io.legado.shared.webbook

import io.legado.shared.analyzeRule.AnalyzeRule
import io.legado.shared.analyzeRule.AnalyzeUrl
import io.legado.shared.analyzeRule.SourceInfo
import io.legado.shared.model.Book
import io.legado.shared.model.BookSource
import io.legado.shared.network.HttpHandler

// == Data Models ====================================================

data class SearchResult(
    val name: String = "",
    val author: String = "",
    val kind: String? = null,
    val coverUrl: String? = null,
    val intro: String? = null,
    val lastChapter: String? = null,
    val url: String? = null
)

data class BookChapter(
    val title: String = "",
    val url: String = "",
    val index: Int = 0,
    val tag: String? = null,
    var nextUrl: String? = null
)

class RuleData {
    val variables: MutableMap<String, String> = mutableMapOf()
    fun put(key: String, value: String) { variables[key] = value }
    fun get(key: String): String = variables[key] ?: ""
}

// == WebBook Engine =================================================

class WebBook(
    private val httpHandler: HttpHandler
) {

    suspend fun searchBook(
        bookSource: BookSource,
        key: String,
        page: Int? = 1,
        filter: ((name: String, author: String, kind: String?) -> Boolean)? = null,
        shouldBreak: ((size: Int) -> Boolean)? = null
    ): List<SearchResult> {
        val searchUrl = bookSource.searchUrl
        if (searchUrl.isNullOrBlank()) return emptyList()

        val ruleData = RuleData()
        val url = buildSearchUrl(searchUrl, key, page)
        val response = httpHandler.getString(url, emptyMap())

        return analyzeBookList(
            bookSource = bookSource,
            baseUrl = url,
            body = response.body,
            isSearch = true,
            filter = filter,
            shouldBreak = shouldBreak
        )
    }

    suspend fun exploreBook(
        bookSource: BookSource,
        url: String,
        page: Int? = 1
    ): List<SearchResult> {
        val fullUrl = buildSearchUrl(url, null, page)
        val response = httpHandler.getString(fullUrl, emptyMap())
        return analyzeBookList(bookSource, fullUrl, response.body, false)
    }

    suspend fun getBookInfo(
        bookSource: BookSource,
        book: Book,
        canReName: Boolean = true
    ): Book {
        val ruleData = RuleData()
        val analyzeUrl = AnalyzeUrl(
            mUrl = book.bookUrl,
            baseUrl = bookSource.bookSourceUrl,
            infoMap = ruleData.variables
        )
        val response = httpHandler.getString(analyzeUrl.url, analyzeUrl.headerMap)
        return analyzeBookInfo(bookSource, book, book.bookUrl, analyzeUrl.url, response.body, canReName)
    }

    suspend fun getChapterList(
        bookSource: BookSource,
        book: Book
    ): List<BookChapter> {
        val ruleData = RuleData()
        val tocUrl = book.tocUrl.ifBlank { book.bookUrl }
        val analyzeUrl = AnalyzeUrl(
            mUrl = tocUrl,
            baseUrl = book.bookUrl,
            infoMap = ruleData.variables
        )
        val response = httpHandler.getString(analyzeUrl.url, analyzeUrl.headerMap)
        return analyzeChapterList(bookSource, book, tocUrl, analyzeUrl.url, response.body)
    }

    suspend fun getContent(
        bookSource: BookSource,
        book: Book,
        chapter: BookChapter,
        nextChapterUrl: String? = null
    ): String {
        val ruleData = RuleData()
        val analyzeUrl = AnalyzeUrl(
            mUrl = chapter.url,
            baseUrl = book.tocUrl,
            infoMap = ruleData.variables
        )
        val response = httpHandler.getString(analyzeUrl.url, analyzeUrl.headerMap)
        return analyzeContent(bookSource, book, chapter, chapter.url, analyzeUrl.url, response.body, nextChapterUrl)
    }

    // == Private Helpers ==============================================

    private fun buildSearchUrl(template: String, key: String?, page: Int?): String {
        var url = template
        if (key != null) url = url.replace("{key}", key)
        if (page != null) url = url.replace("{page}", page.toString())
        url = Regex("\\{\\{.*?\\}\\}").replace(url, "")
        return url
    }

    private fun analyzeBookList(
        bookSource: BookSource,
        baseUrl: String,
        body: String,
        isSearch: Boolean,
        filter: ((String, String, String?) -> Boolean)? = null,
        shouldBreak: ((Int) -> Boolean)? = null
    ): List<SearchResult> {
        val rule = AnalyzeRule(body, baseUrl, bookSource.toSourceInfo())
        val searchRule = bookSource.ruleSearch
        if (searchRule.isNullOrBlank()) return emptyList()

        val list = rule.getElements(searchRule) ?: return emptyList()
        val results = mutableListOf<SearchResult>()

        for (item in list) {
            val itemRule = AnalyzeRule(item, baseUrl, bookSource.toSourceInfo())
            val name = itemRule.getString(bookSource.ruleSearchName).trim()
            if (name.isEmpty()) continue
            val author = itemRule.getString(bookSource.ruleSearchAuthor).trim()
            val kind = itemRule.getString(bookSource.ruleSearchKind).ifBlank { null }
            val cover = itemRule.getString(bookSource.ruleSearchCoverUrl).ifBlank { null }
            val intro = itemRule.getString(bookSource.ruleSearchIntro).ifBlank { null }
            val lastChapter = itemRule.getString(bookSource.ruleSearchLastChapter).ifBlank { null }
            val url = itemRule.getString(bookSource.ruleSearchUrl).ifBlank { null }

            if (filter != null && !filter(name, author, kind)) continue
            results.add(SearchResult(name, author, kind, cover, intro, lastChapter, url))
            if (shouldBreak != null && shouldBreak(results.size)) break
        }
        return results
    }

    private fun analyzeBookInfo(
        bookSource: BookSource,
        book: Book,
        baseUrl: String,
        redirectUrl: String,
        body: String,
        canReName: Boolean
    ): Book {
        val rule = AnalyzeRule(body, redirectUrl, bookSource.toSourceInfo())
        var name = book.name
        var author = book.author
        var coverUrl = book.coverUrl
        var intro = book.intro
        var tocUrl = book.tocUrl
        var latestChapterTitle = book.latestChapterTitle
        if (canReName) name = rule.getString(bookSource.ruleBookName).trim().ifEmpty { name }
        author = rule.getString(bookSource.ruleBookAuthor).trim().ifEmpty { author }
        coverUrl = rule.getString(bookSource.ruleCoverUrl).trim().ifEmpty { coverUrl }
        intro = rule.getString(bookSource.ruleIntro).trim().ifEmpty { intro }
        latestChapterTitle = rule.getString(bookSource.ruleLastChapter).trim().ifEmpty { latestChapterTitle }
        tocUrl = rule.getString(bookSource.ruleTocUrl).trim().ifEmpty { tocUrl }
        return book.copy(
            name = name,
            author = author,
            coverUrl = coverUrl,
            intro = intro,
            latestChapterTitle = latestChapterTitle,
            tocUrl = tocUrl
        )
    }

    private fun analyzeChapterList(
        bookSource: BookSource,
        book: Book,
        baseUrl: String,
        redirectUrl: String,
        body: String
    ): List<BookChapter> {
        val rule = AnalyzeRule(body, redirectUrl, bookSource.toSourceInfo())
        val tocRule = bookSource.ruleToc
        if (tocRule.isNullOrBlank()) return emptyList()

        val list = rule.getElements(tocRule) ?: return emptyList()
        return list.withIndex().mapNotNull { (index, item) ->
            val itemRule = AnalyzeRule(item, redirectUrl, bookSource.toSourceInfo())
            val title = itemRule.getString(bookSource.ruleChapterName).trim()
            if (title.isEmpty()) return@mapNotNull null
            val url = itemRule.getString(bookSource.ruleChapterUrl).trim()
            BookChapter(title = title, url = url, index = index)
        }
    }

    private fun analyzeContent(
        bookSource: BookSource,
        book: Book,
        chapter: BookChapter,
        baseUrl: String,
        redirectUrl: String,
        body: String,
        nextChapterUrl: String?
    ): String {
        val rule = AnalyzeRule(body, redirectUrl, bookSource.toSourceInfo())
        val contentRule = bookSource.ruleContent
        return if (contentRule.isNullOrBlank()) body else rule.getString(contentRule)
    }
}

// == Helper Extensions =============================================

private fun String.ifNotEmpty(action: (String) -> Unit) {
    if (isNotEmpty()) action(this)
}

private fun BookSource.toSourceInfo(): SourceInfo = SourceInfo(
    url = bookSourceUrl,
    name = bookSourceName
)