package io.legado.shared.webbook

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.legado.shared.model.BookSource

class WebBookTest {

    private fun createTestBookSource(): BookSource {
        return BookSource(
            bookSourceUrl = "https://example.com/source",
            bookSourceName = "Test Source",
            searchUrl = "https://example.com/search?q={key}&page={page}",
            ruleSearch = ".book-list .item",
            ruleSearchName = ".title@text",
            ruleSearchAuthor = ".author@text",
            ruleSearchKind = ".kind@text",
            ruleSearchCoverUrl = ".cover@src",
            ruleSearchIntro = ".intro@text",
            ruleSearchLastChapter = ".last@text",
            ruleSearchUrl = ".link@href",
            ruleBookName = ".book-title@text",
            ruleBookAuthor = ".book-author@text",
            ruleCoverUrl = ".book-cover@src",
            ruleIntro = ".book-intro@text",
            ruleLastChapter = ".book-last@text",
            ruleTocUrl = ".toc-link@href",
            ruleToc = ".chapter-list .chapter",
            ruleChapterName = ".chapter-title@text",
            ruleChapterUrl = ".chapter-link@href",
            ruleContent = "#content@text",
            ruleBookContent = ""
        )
    }

    @Test
    fun searchBookBuildsCorrectUrl() {
        val bookSource = createTestBookSource()
        assertTrue(bookSource.searchUrl?.contains("{key}") == true)
        assertTrue(bookSource.searchUrl?.contains("{page}") == true)
    }

    @Test
    fun bookSourceHasAllRules() {
        val bookSource = createTestBookSource()
        assertTrue(bookSource.ruleSearch != null)
        assertTrue(bookSource.ruleSearchName != null)
        assertTrue(bookSource.ruleSearchAuthor != null)
        assertTrue(bookSource.ruleBookName != null)
        assertTrue(bookSource.ruleContent != null)
    }

    @Test
    fun bookSourceGetContentRule() {
        val bookSource = createTestBookSource()
        val contentRule = bookSource.getContentRule()
        assertEquals(bookSource.ruleContent, contentRule.content)
    }

    @Test
    fun searchResultHoldsCorrectData() {
        val result = SearchResult(
            name = "Test Novel",
            author = "Test Author",
            kind = "Fantasy",
            coverUrl = "https://img.com/cover.jpg",
            intro = "A great book",
            lastChapter = "Chapter 100",
            url = "https://example.com/book/1"
        )

        assertEquals("Test Novel", result.name)
        assertEquals("Test Author", result.author)
        assertEquals("Fantasy", result.kind)
    }

    @Test
    fun bookChapterHoldsCorrectData() {
        val chapter = BookChapter(
            title = "Chapter 1",
            url = "https://example.com/ch1",
            index = 0
        )

        assertEquals("Chapter 1", chapter.title)
        assertEquals(0, chapter.index)
    }

    @Test
    fun ruleDataStoresVariables() {
        val ruleData = RuleData()
        ruleData.put("key1", "value1")
        ruleData.put("title", "My Book")

        assertEquals("value1", ruleData.get("key1"))
        assertEquals("My Book", ruleData.get("title"))
        assertEquals("", ruleData.get("nonexistent"))
    }

    @Test
    fun bookSourceIsEnabledByDefault() {
        val bookSource = createTestBookSource()
        assertTrue(bookSource.enabled)
        assertTrue(bookSource.enabledExplore)
    }
}