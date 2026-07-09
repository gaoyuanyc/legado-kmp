package io.legado.shared.readbook

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import io.legado.shared.model.ReplaceRule

class ReadBookEngineTest {

    private fun createEngine(): Pair<ReadBookEngine, List<ChapterContent>> {
        val engine = ReadBookEngine(config = ReadConfig(pageSize = 100))
        val chapters = listOf(
            ChapterContent(title = "Chapter 1", content = "A".repeat(500), index = 0),
            ChapterContent(title = "Chapter 2", content = "B".repeat(500), index = 1)
        )
        engine.loadBook(BookInfo(name = "Test"), chapters)
        return Pair(engine, chapters)
    }

    @Test
    fun loadBookInitializesState() {
        val (engine, _) = createEngine()
        assertEquals(0, engine.currentChapterIndex)
        assertEquals(0, engine.currentPos)
    }

    @Test
    fun nextPageMovesWithinChapter() {
        val (engine, _) = createEngine()
        val result = engine.nextPage()
        assertTrue(result)
        assertTrue(engine.currentPageIndex > 0)
    }

    @Test
    fun nextChapterMovesToNext() {
        val (engine, _) = createEngine()
        engine.updateConfig(ReadConfig(pageSize = 10000))
        val result = engine.nextChapter()
        assertTrue(result)
        assertEquals(1, engine.currentChapterIndex)
    }

    @Test
    fun getProgressReturnsValidValue() {
        val (engine, _) = createEngine()
        val progress = engine.getProgress()
        assertTrue(progress >= 0.0 && progress <= 1.0)
    }

    @Test
    fun processContentAppliesRules() {
        val engine = ReadBookEngine()
        val rules = listOf(
            ReplaceRule(pattern = "bad", replacement = "good", isEnabled = true, isRegex = false)
        )
        val result = engine.processContent("This is bad text", rules)
        assertEquals("This is good text", result)
    }

    @Test
    fun stripHtmlRemovesTags() {
        val engine = ReadBookEngine()
        val result = engine.stripHtml("<p>Hello <b>World</b></p>")
        assertFalse(result.contains("<"))
        assertTrue(result.contains("Hello"))
    }
}