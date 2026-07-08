package io.legado.android

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView

/**
 * Helper to create a View with Compose content.
 * Isolates the setContent call to avoid overload ambiguity.
 */
object ComposeViewHelper {
    fun create(context: Context, content: @Composable () -> Unit): View {
        val view = ComposeView(context)
        view.setContent(content)
        return view
    }
}
