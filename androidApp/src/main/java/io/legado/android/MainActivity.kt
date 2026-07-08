package io.legado.android

import android.os.Bundle
import androidx.compose.runtime.Composable
import io.legado.android.ui.LegadoNavigation
import io.legado.android.ui.theme.LegadoTheme
import io.legado.shared.model.Book
import io.legado.shared.model.BookSource
import io.legado.android.ComposeActivity

class MainActivity : ComposeActivity() {

    override val content: @Composable () -> Unit = {
        LegadoTheme {
            LegadoNavigation(
                books = sampleBooks,
                sources = sampleSources
            )
        }
    }

    companion object {
        // Sample data for development
        private val sampleBooks = listOf(
            Book(name = "Sample Novel", author = "Author One", durChapterTitle = "Chapter 1"),
            Book(name = "Another Book", author = "Author Two", durChapterTitle = "Chapter 5")
        )
        private val sampleSources = listOf(
            BookSource(bookSourceName = "Example Source", bookSourceUrl = "https://example.com", enabled = true),
            BookSource(bookSourceName = "Another Source", bookSourceUrl = "https://another.com", enabled = false)
        )
    }
}
