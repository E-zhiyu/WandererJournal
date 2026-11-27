package com.wanderer.journal.ui.pages.media

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.webkit.MimeTypeMap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.enums.unique.KeyStrings
import com.wanderer.journal.data.save.preference.MediaPreference
import com.wanderer.journal.helpers.file.isHdrImage
import com.wanderer.journal.helpers.file.saveMediaToGallery
import com.wanderer.journal.helpers.file.shareFile
import com.wanderer.journal.ui.components.dialogs.ExceptionDialog
import com.wanderer.journal.ui.theme.MaterialAppTheme
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import java.io.File

@Composable
fun FullScreenMediaScreen(
    uris: List<String>,
    initialPos: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = context as? Activity

    // 控件显隐 + 淡入淡出
    var controlsVisible by remember { mutableStateOf(true) }

    // 错误对话框
    var errorMessage by remember { mutableStateOf<String?>(null) }
    if (errorMessage != null) {
        ExceptionDialog(
            onDismiss = { errorMessage = null },
            message = errorMessage!!
        )
    }

    // Snackbar 状态
    val snackHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.media_saved_to_gallery)

    // Pager
    val pagerState = rememberPagerState(
        initialPage = initialPos.coerceIn(0, (uris.size - 1).coerceAtLeast(0)),
        pageCount = { uris.size }
    )

    // 当前页的 HDR 状态
    var isHdr by remember { mutableStateOf(true) }

    // 翻页时异步检测 HDR
    LaunchedEffect(pagerState.currentPage) {
        val pos = pagerState.currentPage
        val uriStr = uris.getOrNull(pos) ?: return@LaunchedEffect
        if (!MediaPreference.getHdrDisplay(context)) return@LaunchedEffect
        try {
            isHdr = isHdrImage(context, uriStr.toUri())
        } catch (e: Exception) {
            errorMessage = e.message ?: "检测 HDR 失败"
            isHdr = false
        }
    }

    // 应用 HDR 色彩模式
    LaunchedEffect(isHdr) {
        val colorMode = if (isHdr) ActivityInfo.COLOR_MODE_HDR else ActivityInfo.COLOR_MODE_DEFAULT
        activity?.window?.colorMode = colorMode
    }

    // 系统栏显隐跟随 controlsVisible
    LaunchedEffect(controlsVisible) {
        val window = activity?.window ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (controlsVisible) {
            controller.show(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    // 进入页面时配置一次系统栏行为
    DisposableEffect(Unit) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        onDispose {
            if (window != null) {
                WindowCompat.getInsetsController(window, window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(window, true)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackHostState) },
        topBar = {
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    title = {
                        // TODO:标题
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        MaterialTheme.colorScheme.background.copy(
                            alpha = 0.4f
                        )
                    )
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val uri = uris.getOrNull(page)
            if (uri != null) {
                ZoomableAsyncImage(
                    onClick = { controlsVisible = !controlsVisible },
                    model = uri.toUri(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // HDR 标志
            AnimatedVisibility(
                visible = controlsVisible && isHdr,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 120.dp)
            ) {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.HDR),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // 底部按钮组
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                // TODO:调整按钮的位置
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val pos = pagerState.currentPage
                            val uriStr = uris.getOrNull(pos) ?: return@IconButton
                            scope.launch {
                                try {
                                    val uri = uriStr.toUri()
                                    val file = File(uri.path ?: error("无法获取文件路径"))
                                    val ext = MimeTypeMap.getFileExtensionFromUrl(uriStr)
                                    val mime = MimeTypeMap.getSingleton()
                                        .getMimeTypeFromExtension(ext.lowercase())
                                    shareFile(context, file, mime ?: "*/*")
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "分享失败"
                                }
                            }
                        },
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            MaterialTheme.shapes.extraLarge
                        )
                    ) {
                        Icon(
                            painterResource(R.drawable.outline_share_24),
                            contentDescription = stringResource(R.string.btn_share),
                        )
                    }
                    IconButton(
                        onClick = {
                            val pos = pagerState.currentPage
                            val uriStr = uris.getOrNull(pos) ?: return@IconButton
                            scope.launch {
                                try {
                                    saveMediaToGallery(context, uriStr.toUri())
                                    snackHostState.showSnackbar(savedMessage)
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "保存失败"
                                }
                            }
                        },
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            MaterialTheme.shapes.extraLarge
                        )
                    ) {
                        Icon(
                            painterResource(R.drawable.outline_download_24),
                            contentDescription = stringResource(R.string.btn_save),
                        )
                    }

                    IconButton(
                        onClick = {
                            val uriStr = uris.getOrNull(pagerState.currentPage) ?: return@IconButton
                            val intent = Intent(context, MediaInfoActivity::class.java).apply {
                                putExtra(KeyStrings.FILE_URIS.v(), uriStr)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            MaterialTheme.shapes.extraLarge
                        )
                    ) {
                        Icon(
                            painterResource(R.drawable.outline_info_24),
                            contentDescription = null,
                        )
                    }
                }
            }
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
private fun FullScreenMediaScreenPreview() {
    MaterialAppTheme {
        FullScreenMediaScreen(
            uris = emptyList(),
            initialPos = 0,
            onNavigateBack = {}
        )
    }
}