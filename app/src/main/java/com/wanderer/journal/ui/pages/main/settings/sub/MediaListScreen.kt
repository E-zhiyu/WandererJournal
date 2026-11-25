package com.wanderer.journal.ui.pages.main.settings.sub

import android.content.res.Configuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.wanderer.journal.ui.theme.MaterialAppTheme
import kotlinx.coroutines.channels.Channel

private val snackMessageChannel = Channel<String>(Channel.BUFFERED) // Snackbar 一次性文本频道

@Composable
fun MediaListScreen(
    onNavigateBack: () -> Unit
) {
    // Snackbar
    val snackHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        for (message in snackMessageChannel) {
            if (message.isNotEmpty()) {
                snackHostState.showSnackbar(message)
            }
        }
    }

    // TODO:构建网格布局
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun MediaListScreenPreview() {
    MaterialAppTheme {
        MediaListScreen(
            {}
        )
    }
}