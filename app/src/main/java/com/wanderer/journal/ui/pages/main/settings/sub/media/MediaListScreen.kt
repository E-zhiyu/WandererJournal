package com.wanderer.journal.ui.pages.main.settings.sub.media

import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.auxiliary.classes.file.MediaFileInfo
import com.wanderer.journal.auxiliary.enums.unique.KeyStrings
import com.wanderer.journal.auxiliary.interfaces.ImageCompressCallback
import com.wanderer.journal.helpers.file.compressImage
import com.wanderer.journal.helpers.text.shortenFileSize
import com.wanderer.journal.ui.components.LargeRadiusDropdownMenu
import com.wanderer.journal.ui.components.dialogs.DialogScaffold
import com.wanderer.journal.ui.components.dialogs.ExceptionDialog
import com.wanderer.journal.ui.components.dialogs.MessageDialog
import com.wanderer.journal.ui.pages.media.FullScreenMediaActivity
import com.wanderer.journal.ui.theme.MaterialAppTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun MediaListScreen(
    onNavigateBack: () -> Unit,
    viewModel: MediaListViewModel = viewModel(),
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showOnlyUnreferenced by rememberSaveable { mutableStateOf(false) }  // 是否仅显示未关联的媒体
    var compressTarget by remember { mutableStateOf<MediaListItem?>(null) } // 需要压缩的目标
    var isCompressing by remember { mutableStateOf(false) }                 // 是否正在压缩
    var errorMessage by remember { mutableStateOf<String?>(null) }          // 错误提示信息

    // 每次界面回到前台时重新扫描媒体目录并查询数据库，
    // 以便从全屏查看、系统相册等界面返回后能反映最新的文件/数据库状态。
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME, lifecycleOwner) {
        viewModel.refresh()
    }

    // ViewModel 加载失败时显示错误对话框
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            errorMessage = error.localizedMessage ?: error.toString()
            viewModel.clearError()
        }
    }

    /**
     * 开始压缩指定媒体文件。
     */
    val tipCompressSuccess = stringResource(R.string.compress_success)
    fun startCompression(target: MediaListItem) {
        isCompressing = true
        scope.launch {
            try {
                compressImage(
                    uri = target.info.uri,
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    callback = object : ImageCompressCallback {
                        override fun failed(message: String) {
                            scope.launch {
                                isCompressing = false
                                snackHostState.showSnackbar(message)
                            }
                        }

                        override fun succeed() {
                            scope.launch {
                                isCompressing = false
                                viewModel.refresh()
                                snackHostState.showSnackbar(tipCompressSuccess)
                            }
                        }
                    },
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                isCompressing = false
                errorMessage = e.localizedMessage ?: e.toString()
            } finally {
                isCompressing = false
            }
        }
    }

    /**
     * 打开全屏媒体查看界面。
     *
     * 旧界面使用共享元素转场；Compose 中如果把 LocalView（整个 ComposeView）作为共享元素，
     * 动画会作用到整个页面而不是单个网格项，因此这里先直接启动 Activity。
     * TODO:后续如果把全屏查看也迁移到 Compose，可以使用 SharedTransitionLayout 实现逐项转场。
     */
    val openMedia: (Int, List<MediaListItem>) -> Unit = { index, itemList ->
        val uriStrings = itemList.map { it.info.uri.toString() }.toTypedArray()
        val intent = Intent(context, FullScreenMediaActivity::class.java).apply {
            putExtra(KeyStrings.FILE_URIS.v(), uriStrings)
            putExtra(KeyStrings.VIEW_HOLDER_POSITION.v(), index)
        }

        if (activity != null) {
            activity.startActivity(intent)
        } else {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    // 主内容
    MediaListContent(
        uiState = uiState,
        showOnlyUnreferenced = showOnlyUnreferenced,
        onToggleUnreferencedFilter = { showOnlyUnreferenced = it },
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onSelectOrdering = viewModel::setOrdering,
        onSetAscending = viewModel::setAscending,
        onOpenMedia = openMedia,
        onCompressMedia = { if (!isCompressing) compressTarget = it },
        snackHostState = snackHostState,
    )

    // 压缩确认对话框
    compressTarget?.let { target ->
        MessageDialog(
            onDismiss = { compressTarget = null },
            confirmTitle = stringResource(R.string.confirm),
            onConfirm = {
                compressTarget = null
                startCompression(target)
            },
            cancelTitle = stringResource(R.string.cancel),
            title = stringResource(R.string.compress),
            message = stringResource(R.string.compress_confirm_message),
            useMarkdown = true,
        )
    }

    // 压缩进度对话框
    if (isCompressing) {
        DialogScaffold(
            onDismiss = {},
            confirmTitle = "",
            onConfirm = null,
            cancelTitle = "",
            onCancel = null,
            title = stringResource(R.string.compressing),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.compressing_message))
            }
        }
    }

    // 异常对话框
    errorMessage?.let { message ->
        ExceptionDialog(
            onDismiss = { errorMessage = null },
            message = message,
        )
    }
}

/**
 * 媒体列表的内容部分（不持有 ViewModel，便于 Preview 和测试）。
 */
@Composable
private fun MediaListContent(
    uiState: MediaListUiState,
    showOnlyUnreferenced: Boolean,
    onToggleUnreferencedFilter: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onSelectOrdering: (Ordering) -> Unit,
    onSetAscending: (Boolean) -> Unit,
    onOpenMedia: (Int, List<MediaListItem>) -> Unit,
    onCompressMedia: (MediaListItem) -> Unit,
    snackHostState: SnackbarHostState,
) {
    val title = stringResource(R.string.media_list)
    val configuration = LocalConfiguration.current
    val columnCount = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        9
    } else {
        4
    }

    val displayedItems = remember(uiState.items, showOnlyUnreferenced) {
        if (showOnlyUnreferenced) {
            uiState.items.filter { it.isUnreferenced }
        } else {
            uiState.items
        }
    }
    val unreferencedCount = remember(uiState.items) {
        uiState.items.count { it.isUnreferenced }
    }

    var orderingMenuExpanded by remember { mutableStateOf(false) }

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
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            painter = painterResource(R.drawable.outline_refresh_24),
                            contentDescription = stringResource(R.string.refresh),
                        )
                    }
                },
                scrollBehavior = scrollState,
            )
        },
        snackbarHost = { SnackbarHost(snackHostState) },
    ) { innerPadding ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .nestedScroll(scrollState.nestedScrollConnection)
                .padding(horizontal = Interval.SidePadding),
            columns = GridCells.Fixed(columnCount),
            horizontalArrangement = Arrangement.spacedBy(Interval.AdjacentPadding),
            verticalArrangement = Arrangement.spacedBy(Interval.AdjacentPadding),
            contentPadding = PaddingValues(
                top = Interval.DefaultPadding,
                bottom = innerPadding.calculateBottomPadding(),
            ),
        ) {
            // 排序、筛选和统计信息
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box {
                            SplitButtonLayout(
                                leadingButton = {
                                    SplitButtonDefaults.LeadingButton(onClick = {}) {
                                        Text(stringResource(uiState.ordering.titleRes))
                                    }
                                },
                                trailingButton = {
                                    SplitButtonDefaults.TrailingButton(onClick = {
                                        orderingMenuExpanded = true
                                    }) {
                                        Icon(
                                            painter = painterResource(R.drawable.outline_unfold_more_24),
                                            contentDescription = null,
                                        )
                                    }
                                },
                            )

                            LargeRadiusDropdownMenu(
                                expanded = orderingMenuExpanded,
                                onDismissRequest = { orderingMenuExpanded = false },
                            ) {
                                Ordering.entries.forEachIndexed { index, ordering ->
                                    SelectableDropdownMenuItem(
                                        selected = ordering == uiState.ordering,
                                        text = { Text(stringResource(ordering.titleRes)) },
                                        onClick = {
                                            orderingMenuExpanded = false
                                            onSelectOrdering(ordering)
                                        },
                                        shapes = MenuDefaults.itemShape(
                                            index,
                                            Ordering.entries.size,
                                        ),
                                    )
                                }
                                HorizontalDivider()
                                SelectableDropdownMenuItem(
                                    selected = uiState.isAscending,
                                    text = { Text(stringResource(R.string.from_small_to_large)) },
                                    onClick = {
                                        orderingMenuExpanded = false
                                        onSetAscending(true)
                                    },
                                    shapes = MenuDefaults.itemShape(0, 2),
                                )
                                SelectableDropdownMenuItem(
                                    selected = !uiState.isAscending,
                                    text = { Text(stringResource(R.string.from_large_to_small)) },
                                    onClick = {
                                        orderingMenuExpanded = false
                                        onSetAscending(false)
                                    },
                                    shapes = MenuDefaults.itemShape(1, 2),
                                )
                                HorizontalDivider()
                                SelectableDropdownMenuItem(
                                    selected = showOnlyUnreferenced,
                                    text = { Text(stringResource(R.string.filter_unlinked_media)) },
                                    onClick = {
                                        onToggleUnreferencedFilter(!showOnlyUnreferenced)
                                    },
                                    shapes = MenuDefaults.itemShape(0, 1),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.media_list_summary,
                            uiState.items.size,
                            unreferencedCount,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (uiState.isLoading) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (displayedItems.isEmpty() && !uiState.isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(
                                if (showOnlyUnreferenced) {
                                    R.string.no_unlinked_media
                                } else {
                                    R.string.no_media
                                }
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = displayedItems,
                    key = { _, item -> item.info.uri.toString() },
                ) { index, item ->
                    MediaGridItem(
                        item = item,
                        onClick = { onOpenMedia(index, displayedItems) },
                        onCompress = { onCompressMedia(item) },
                    )
                }
            }
        }
    }
}

/**
 * 单个媒体网格项。
 */
@Composable
private fun MediaGridItem(
    item: MediaListItem,
    onClick: () -> Unit,
    onCompress: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(Radius.Small))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { menuExpanded = true },
            )
    ) {
        AsyncImage(
            model = item.info.uri,
            contentDescription = stringResource(R.string.preview_of_media_file),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.outline_error_24),
        )

        // 未被数据库引用的媒体文件标识
        if (item.isUnreferenced) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shape = RoundedCornerShape(Radius.Small),
            ) {
                Text(
                    text = stringResource(R.string.media_not_linked),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }

        // 文件大小
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp),
            color = Color.Black.copy(alpha = 0.55f),
            contentColor = Color.White,
            shape = RoundedCornerShape(Radius.Small),
        ) {
            Text(
                text = shortenFileSize(item.info.size),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // 长按菜单
        LargeRadiusDropdownMenu(
            modifier = Modifier.align(Alignment.TopEnd),
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.compress)) },
                onClick = {
                    menuExpanded = false
                    onCompress()
                },
            )
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MediaListScreenPreview() {
    val items = remember {
        listOf(
            MediaListItem(
                info = MediaFileInfo(
                    "file:///preview_1.jpg".toUri(),
                    1024L * 1024L,
                    "preview_1.jpg",
                    0L,
                ),
                isUnreferenced = false,
            ),
            MediaListItem(
                info = MediaFileInfo(
                    "file:///preview_2.jpg".toUri(),
                    2048L * 1024L,
                    "preview_2.jpg",
                    1L,
                ),
                isUnreferenced = true,
            ),
        )
    }

    MaterialAppTheme {
        MediaListContent(
            uiState = MediaListUiState(
                items = items,
                isLoading = false,
            ),
            showOnlyUnreferenced = false,
            onToggleUnreferencedFilter = {},
            onNavigateBack = {},
            onRefresh = {},
            onSelectOrdering = {},
            onSetAscending = {},
            onOpenMedia = { _, _ -> },
            onCompressMedia = {},
            snackHostState = remember { SnackbarHostState() },
        )
    }
}
