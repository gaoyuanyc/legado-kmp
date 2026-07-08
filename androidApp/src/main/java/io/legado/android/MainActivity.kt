package io.legado.android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.legado.android.ui.theme.LegadoTheme

class MainActivity : ComposeActivity() {

    override val content: @Composable () -> Unit = {
        LegadoTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                LegadoHomeScreen()
            }
        }
    }
}

@Composable
fun LegadoHomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Legado KMP", style = MaterialTheme.typography.headlineMedium)
    }
}
