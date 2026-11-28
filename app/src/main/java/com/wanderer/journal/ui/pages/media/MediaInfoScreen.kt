package com.wanderer.journal.ui.pages.media

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.CustomDateTimeFormatter
import com.wanderer.journal.auxiliary.classes.file.MediaDetail
import com.wanderer.journal.helpers.file.getMediaDetail
import com.wanderer.journal.helpers.text.TextHelper
import com.wanderer.journal.ui.components.LazyLargeTopAppBarScaffold
import com.wanderer.journal.ui.components.dialogs.ExceptionDialog
import com.wanderer.journal.ui.theme.MaterialAppTheme
import kotlin.math.roundToLong

private sealed interface MediaInfoUiState {
    data object Loading : MediaInfoUiState
    data class Success(val detail: MediaDetail) : MediaInfoUiState
    data class Error(val message: String) : MediaInfoUiState
}

@Composable
fun MediaInfoScreen(
    uri: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var state by remember { mutableStateOf<MediaInfoUiState>(MediaInfoUiState.Loading) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 异步加载媒体详情
    LaunchedEffect(uri) {
        state = MediaInfoUiState.Loading
        state = try {
            MediaInfoUiState.Success(getMediaDetail(context, uri.toUri()))
        } catch (e: Exception) {
            MediaInfoUiState.Error(e.message ?: "读取媒体信息失败")
        }
    }

    // 错误对话框（仅用于 Success 之后的额外错误；加载错误也可复用）
    if (errorMessage != null) {
        ExceptionDialog(
            onDismiss = { errorMessage = null },
            message = errorMessage!!
        )
    }

    when (val s = state) {
        is MediaInfoUiState.Loading -> {
        }

        is MediaInfoUiState.Error -> {
            Text(
                text = s.message,
                color = MaterialTheme.colorScheme.error
            )
        }

        is MediaInfoUiState.Success -> {
            MediaInfoContent(
                detail = s.detail,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
private fun MediaInfoContent(
    detail: MediaDetail,
    onNavigateBack: () -> Unit
) {
    LazyLargeTopAppBarScaffold(
        title = stringResource(R.string.media_info),
        onNavigateBack = onNavigateBack
    ) {
        // ---- 时间信息 ----
        item {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.time_with_colon),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = detail.dateTime?.format(CustomDateTimeFormatter.DateTimeMediaDetail)
                        ?: stringResource(R.string.not_applicable),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // ---- 文件信息 ----
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.file_info),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(5.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 文件名
                        Text(
                            text = detail.fileName ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )

                        val sizeDisplay = String.format(
                            LocalLocale.current.platformLocale,
                            "%s  %d×%dpx",
                            TextHelper.shortenFileSize(detail.size),
                            detail.width,
                            detail.height
                        )
                        // 文件大小和像素尺寸大小
                        Text(
                            text = sizeDisplay,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // ---- 拍摄参数（设备为空则整块隐藏） ----
        if (!detail.device.isNullOrEmpty()) {
            item {
                Row {
                    Text(
                        text = stringResource(R.string.media_param_info),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = detail.device,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )

                        val shutterDisplay = if (detail.shutter < 1.0 && detail.shutter > 0) {
                            "1/${(1.0 / detail.shutter).roundToLong()}"
                        } else {
                            detail.shutter.toString()
                        }
                        val param1 = String.format(
                            LocalLocale.current.platformLocale,
                            "f/%.2f  %ss  ISO%d",
                            detail.aperture, shutterDisplay, detail.iso
                        )
                        val param2 = String.format(
                            LocalLocale.current.platformLocale,
                            "%.2fmm  %s",
                            detail.focalLength,
                            if (detail.isFlashUsed) "使用了闪光灯" else "未使用闪光灯"
                        )
                        Text(param1, style = MaterialTheme.typography.bodyMedium)
                        Text(param2, style = MaterialTheme.typography.bodyMedium)
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
private fun MediaInfoPreview() {
    MaterialAppTheme {
        MediaInfoScreen(
            uri = "",
            onNavigateBack = {}
        )
    }
}