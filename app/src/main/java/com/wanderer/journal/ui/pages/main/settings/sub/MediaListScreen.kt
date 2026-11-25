package com.wanderer.journal.ui.pages.main.settings.sub

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.ui.pages.main.settings.sub.media.Ordering
import com.wanderer.journal.ui.theme.MaterialAppTheme
import kotlinx.coroutines.channels.Channel

private val snackMessageChannel = Channel<String>(Channel.BUFFERED) // Snackbar 一次性文本频道

@Composable
fun MediaListScreen(
    onNavigateBack: () -> Unit
) {
    val title = stringResource(R.string.media_list)

    // Snackbar
    val snackHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        for (message in snackMessageChannel) {
            if (message.isNotEmpty()) {
                snackHostState.showSnackbar(message)
            }
        }
    }

    var ordering by remember { mutableStateOf<Ordering?>(null) }

    // 滚动状态
    val scrollState = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollState
            )
        },
        snackbarHost = { SnackbarHost(snackHostState) }
    ) { innerPadding ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .nestedScroll(scrollState.nestedScrollConnection)
                .padding(horizontal = Interval.SidePadding),
            contentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding(),
                top = Interval.DefaultPadding
            ),
            columns = GridCells.Adaptive(100.dp)
        ) {
            // 下拉框按钮
            item {
                SplitButtonLayout(
                    leadingButton = {
                        TextButton(
                            onClick = {}
                        ) {
                            ordering?.let { Text(it.name) }
                        }
                    },
                    trailingButton = {
                        IconButton(
                            onClick = {
                                // TODO:呼出下拉框
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_unfold_more_24),
                                contentDescription = "Dropdown Menu"
                            )
                        }
                    }
                )
            }

            // TODO:网格布局
        }
    }
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