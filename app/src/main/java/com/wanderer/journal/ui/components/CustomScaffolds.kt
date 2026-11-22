package com.wanderer.journal.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.wanderer.journal.auxiliary.classes.Interval

/**
 * 带有 LargeTopAppBar 且包含 LazyColumn 的脚手架
 *
 * @param onNavigateBack 返回回调
 * @param title 界面标题
 * @param snackHostState snackBar 状态
 * @param actions 标题栏的操作图标布局
 * @param floatingActionButton FAB 按钮
 * @param useDefaultTopPadding LazyColumn 中是否使用默认的顶部内边距
 */
@Composable
fun LazyLargeTopAppBarScaffold(
    onNavigateBack: (() -> Unit)? = null,
    title: String,
    snackHostState: @Composable (() -> Unit) = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    floatingActionButton: @Composable (() -> Unit) = {},
    useDefaultTopPadding: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    val scrollState = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(onClick = it) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = actions,
                scrollBehavior = scrollState
            )
        },
        floatingActionButton = floatingActionButton,
        snackbarHost = snackHostState
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .nestedScroll(scrollState.nestedScrollConnection)
                .padding(horizontal = Interval.SidePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding(),
                top = if (useDefaultTopPadding) Interval.DefaultPadding else 0.dp
            )
        ) {
            content()
        }
    }
}

/**
 * 带有 LargeTopAppBar 的列表界面脚手架
 *
 * @param onNavigateBack 返回回调
 * @param title 界面标题
 * @param snackHostState snackBar 状态
 * @param actions 标题栏的操作图标布局
 * @param floatingActionButton FAB 按钮
 * @param topbarScrollBehavior 自定义标题栏的滚动行为
 */
@Composable
fun LargeTopAppBarListScaffold(
    onNavigateBack: (() -> Unit)? = null,
    title: String,
    snackHostState: @Composable (() -> Unit) = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    floatingActionButton: @Composable (() -> Unit) = {},
    topbarScrollBehavior: TopAppBarScrollBehavior? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val scrollState = topbarScrollBehavior ?: TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(onClick = it) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = actions,
                scrollBehavior = scrollState
            )
        },
        floatingActionButton = floatingActionButton,
        snackbarHost = snackHostState
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .nestedScroll(scrollState.nestedScrollConnection)
        ) {
            content()
        }
    }
}