package io.legado.android

import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView

/**
 * A simple Compose-based Activity that avoids setContent overload ambiguity.
 */
abstract class ComposeActivity : androidx.activity.ComponentActivity() {

    abstract val content: @Composable () -> Unit

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this)
        val composeView = ComposeView(this).apply {
            setComposableContent(this@ComposeActivity.content)
        }
        root.addView(composeView, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        setContentView(root)
    }
}

/**
 * Extension to set content without overload ambiguity.
 */
fun ComposeView.setComposableContent(content: @Composable () -> Unit) {
    setContent { content() }
}
