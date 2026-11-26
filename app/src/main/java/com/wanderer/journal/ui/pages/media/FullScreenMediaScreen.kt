package com.wanderer.journal.ui.pages.media

import androidx.compose.runtime.Composable
import com.wanderer.journal.ui.theme.MaterialAppTheme

@Composable
fun FullScreenMediaScreen(
    onNavigateBack: () -> Unit
) {

}

@Composable
private fun FullScreenMediaScreenPreview() {
    MaterialAppTheme {
        FullScreenMediaScreen(
            onNavigateBack = {}
        )
    }
}