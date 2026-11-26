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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import coil.compose.AsyncImage
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

    // Pager
    val pagerState = rememberPagerState(
        initialPage = initialPos.coerceIn(0, (uris.size - 1).coerceAtLeast(0)),
        pageCount = { uris.size }
    )

    // 当前页的 HDR 状态
    var isHdr by remember { mutableStateOf(false) }

    // 翻页时异步检测 HDR
    LaunchedEffect(pagerState.currentPage) {
        isHdr = false
        val pos = pagerState.currentPage
        val uriStr = uris.getOrNull(pos) ?: return@LaunchedEffect
        if (!MediaPreference.getHdrDisplay(context)) return@LaunchedEffect
        try {
            val hdr = isHdrImage(context, uriStr.toUri())
            isHdr = hdr
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
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        onDispose {
            // 离开时恢复系统栏，避免影响其他页面
            if (window != null) {
                WindowCompat.getInsetsController(window, window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(window, true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // 点击切换控件显隐
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { controlsVisible = !controlsVisible }
                )
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val uri = uris.getOrNull(page)
            if (uri != null) {
                // TODO:改为可缩放，但是要注意手指点击时能否隐藏按钮（已知存在手势冲突）
                AsyncImage(
                    model = uri.toUri(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // HDR 标志（带淡入淡出）
        AnimatedVisibility(
            visible = controlsVisible && isHdr,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 100.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color.Black.copy(alpha = 0.4f)
            ) {
                Text(
                    text = stringResource(R.string.HDR),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        // 底部按钮组（分享 / 保存）
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = Color.Black.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = {
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
                    }) {
                        Icon(
                            painterResource(R.drawable.outline_share_24),
                            contentDescription = stringResource(R.string.btn_share),
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        val pos = pagerState.currentPage
                        val uriStr = uris.getOrNull(pos) ?: return@IconButton
                        scope.launch {
                            try {
                                saveMediaToGallery(context, uriStr.toUri())
                                // TODO:可用 Snackbar/Toast
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "保存失败"
                            }
                        }
                    }) {
                        Icon(
                            painterResource(R.drawable.outline_download_24),
                            contentDescription = stringResource(R.string.btn_save),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // 详情按钮（右下角，与底部按钮同高）
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 25.dp, bottom = 20.dp)
        ) {
            IconButton(
                onClick = {
                    val uriStr = uris.getOrNull(pagerState.currentPage) ?: return@IconButton
                    // 过渡期仍启动原 Activity；迁移后改为 navController.navigate(MediaInfoRoute(...))
                    val intent = Intent(context, MediaInfoActivity::class.java).apply {
                        putExtra(KeyStrings.FILE_URIS.v(), uriStr)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.background(
                    Color.Black.copy(alpha = 0.4f),
                    MaterialTheme.shapes.large
                )
            ) {
                Icon(
                    painterResource(R.drawable.outline_info_24),
                    contentDescription = null,
                    tint = Color.White
                )
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