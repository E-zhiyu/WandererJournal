package com.wanderer.journal.ui.components.dialogs

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanderer.journal.R
import kotlinx.coroutines.launch

/**
 * 显示错误对话框
 * @param onDismiss 对话框消失回调
 * @param message 需要显示的内容
 */
@Composable
fun ExceptionDialog(
    onDismiss: () -> Unit,
    message: String
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val scrollState = rememberScrollState()
    val errTitle = stringResource(R.string.error_info)

    AlertDialog(
        modifier = Modifier.padding(top = 40.dp, bottom = 40.dp),
        onDismissRequest = onDismiss,
        title = { Text(errTitle) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                Text(message.ifEmpty { "发生错误" })
            }
        },
        confirmButton = {
            if (message.isNotEmpty()) {
                TextButton(onClick = {
                    scope.launch {
                        val clipData = ClipData.newPlainText("[Wanderer]$errTitle", message)
                        val clipEntry = ClipEntry(clipData)
                        clipboard.setClipEntry(clipEntry)
                        Toast.makeText(context, "错误信息已复制", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("复制内容")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 显示错误对话框
 * @param onDismiss 对话框消失回调
 * @param throwable 需要显示的内容
 */
@Composable
fun ExceptionDialog(
    onDismiss: () -> Unit,
    throwable: Throwable
) {
    ExceptionDialog(onDismiss, throwable.message ?: "")
}