package io.legado.shared.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.legado.shared.model.Book
import io.legado.shared.model.BookSource
import io.legado.shared.model.ReplaceRule
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * SQLDelight integration tests using JDBC SQLite (in-memory).
 * Verifies that all generated database queries work correctly.
 */
class DatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AppDatabase.Schema.create(driver)
        database = AppDatabase(driver)
    }

    private val queries get() = database.legadoQueries

    @Test
    fun insertAndQueryBook() {
        insertTestBook("http://test.com/book1", "Test Book", "Test Author")

        val book = queries.getBook("http://test.com/book1").executeAsOneOrNull()
        assertNotNull(book)
        assertEquals("Test Book", book?.name)
        assertEquals("Test Author", book?.author)
        assertEquals("http://test.com/book1", book?.bookUrl)
    }

    @Test
    fun insertMultipleBooks_queryAll() {
        for (i in 1..5) {
            insertTestBook("http://test.com/book$i", "Book $i", "Author $i")
        }
        val allBooks = queries.allBooks().executeAsList()
        assertEquals(5, allBooks.size)
    }

    @Test
    fun updateBook_progress() {
        insertTestBook("http://test.com/book1", "Test Book", "Author")
        queries.upProgress(42L, "http://test.com/book1")
        val book = queries.getBook("http://test.com/book1").executeAsOneOrNull()
        assertEquals(42L, book?.durChapterPos)
    }

    @Test
    fun deleteBook() {
        insertTestBook("http://test.com/book1", "Test Book", "Author")
        queries.deleteBook("http://test.com/book1")
        val book = queries.getBook("http://test.com/book1").executeAsOneOrNull()
        assertNull(book)
    }

    @Test
    fun hasBook() {
        insertTestBook("http://test.com/book1", "Test Book", "Author")
        assertTrue(queries.hasBook("http://test.com/book1").executeAsOne())
    }

    @Test
    fun bookCount() {
        assertEquals(0L, queries.allBookCount().executeAsOne())
        insertTestBook("http://test.com/book1", "Test Book", "Author")
        assertEquals(1L, queries.allBookCount().executeAsOne())
    }

    @Test
    fun insertAndQuerySource() {
        queries.insertSource(
            "http://test.com/source", "Test Source", null, 0L,
            null, 0L, 1L, 1L,
            null, null, null, null,
            null, null, null, null,
            null, null, 0L, 0L, 0L,
            null, null, null, null,
            null, null, null, null,
            null, 0L, 0L
        )
        val source = queries.getBookSource("http://test.com/source").executeAsOneOrNull()
        assertNotNull(source)
        assertEquals("Test Source", source?.bookSourceName)
    }

    @Test
    fun enableDisableSource() {
        queries.insertSource(
            "http://test.com/source", "Test Source", null, 0L,
            null, 0L, 1L, 1L, null, null, null, null,
            null, null, null, null, null, null, 0L, 0L, 0L,
            null, null, null, null, null, null, null, null, null, 0L, 0L
        )
        queries.enableSource(0L, "http://test.com/source")
        val source = queries.getBookSource("http://test.com/source").executeAsOneOrNull()
        assertEquals(0L, source?.enabled)
    }

    @Test
    fun insertAndQueryCookie() {
        queries.insertCookie("http://test.com", "session=abc123; user=test")
        val cookie = queries.getCookie("http://test.com").executeAsOneOrNull()
        assertNotNull(cookie)
        assertEquals("session=abc123; user=test", cookie?.cookie)
    }

    @Test
    fun deleteAllCookies() {
        queries.insertCookie("http://test1.com", "cookie1")
        queries.insertCookie("http://test2.com", "cookie2")
        queries.deleteAllCookies()
        assertEquals(0, queries.allCookies().executeAsList().size)
    }

    @Test
    fun insertAndQueryGroup() {
        queries.insertGroup(100L, "Favorites", null, 0L, 1L, 1L, -1L, 0L)
        val group = queries.getGroupById(100L).executeAsOneOrNull()
        assertNotNull(group)
        assertEquals("Favorites", group?.groupName)
    }

    @Test
    fun getAllGroups() {
        queries.insertGroup(101L, "Group A", null, 0L, 1L, 1L, -1L, 0L)
        queries.insertGroup(102L, "Group B", null, 1L, 1L, 1L, -1L, 0L)
        val groups = queries.allGroups().executeAsList()
        assertEquals(2, groups.size)
    }

    @Test
    fun insertAndQueryReplaceRule() {
        queries.insertReplaceRule(
            "Remove Ads", null, "<div class=\"ad\">.*?</div>", "",
            null, 0L, 1L, null, 1L, 1L, 3000L, 0L
        )
        val rules = queries.allReplaceRules().executeAsList()
        assertEquals(1, rules.size)
        assertEquals("Remove Ads", rules[0].name)
    }

    @Test
    fun insertAndQueryCache() {
        queries.insertCache("key1", "{\"data\":true}", 0L)
        val cache = queries.getCache("key1").executeAsOneOrNull()
        assertNotNull(cache)
        assertEquals("{\"data\":true}", cache?.value_)
    }

    @Test
    fun insertAndQuerySearchKeyword() {
        queries.insertKeyword("test search", 5L, 1234567L)
        val keyword = queries.getKeyword("test search").executeAsOneOrNull()
        assertNotNull(keyword)
        assertEquals(5L, keyword?.usage)
    }

    @Test
    fun bookMapper_roundTrip() {
        val book = Book(
            bookUrl = "http://test.com/book",
            name = "Test Book",
            author = "Test Author",
            type = 8,
            canUpdate = true
        )

        val generated = Books(
            bookUrl = book.bookUrl,
            tocUrl = "", origin = "loc_book", originName = "",
            name = book.name, author = book.author,
            kind = null, customTag = null, coverUrl = null, customCoverUrl = null,
            intro = null, customIntro = null, charset = null,
            type = book.type.toLong(), group = 0L,
            latestChapterTitle = null, latestChapterTime = 0L,
            lastCheckTime = 0L, lastCheckCount = 0L, totalChapterNum = 0L,
            durChapterTitle = null, durChapterIndex = 0L,
            durVolumeIndex = 0L, chapterInVolumeIndex = 0L,
            durChapterPos = 0L, durChapterTime = 0L,
            wordCount = null,
            canUpdate = if (book.canUpdate) 1L else 0L,
            order = 0L, originOrder = 0L,
            variable = null, readConfig = null, syncTime = 0L
        )

        val mapped = generated.toModel()
        assertEquals("Test Book", mapped.name)
        assertEquals("Test Author", mapped.author)
        assertEquals(book.bookUrl, mapped.bookUrl)
        assertEquals(8, mapped.type)
        assertTrue(mapped.canUpdate)
    }

    private fun insertTestBook(url: String, name: String, author: String) {
        queries.insertBook(
            url, "/toc.html", "loc_book", "Test Source",
            name, author, null, null,
            null, null,
            null, null, "utf-8",
            8L, 0L, null, 0L, 0L, 0L,
            0L, null, 0L, 0L, 0L, 0L, 0L,
            null, 1L, 0L, 0L, null, null, 0L
        )
    }
}
