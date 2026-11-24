package com.wanderer.journal.ui.components.dialogs

import android.content.ClipData
import android.widget.Toast
import androidx.activity.compose.LocalActivity
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
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.mikepenz.markdown.m3.Markdown
import com.wanderer.journal.R
import com.wanderer.journal.helpers.permission.PermissionRequest
import com.wanderer.journal.helpers.permission.requestAutoStartPermission
import com.wanderer.journal.helpers.permission.requestLocationPermission
import com.wanderer.journal.helpers.permission.requestPermission
import kotlinx.coroutines.launch

/**
 * 显示错误对话框
 *
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
 * 带有操作的文本提示对话框
 *
 * @param onDismiss 对话框消失回调，用于设置对话框状态
 * @param confirmTitle 确认按钮的文本
 * @param onConfirm 确认按钮点击回调，传递 null 则不包含确认按钮
 * @param cancelTitle 取消按钮的文本
 * @param onCancel 取消按钮点击回调
 * @param title 对话框标题
 * @param message 对话框显示的文本内容
 * @param useMarkdown 是否使用 Markdown 显示消息
 */
@Composable
fun ActionDialog(
    onDismiss: () -> Unit,
    confirmTitle: String = stringResource(R.string.confirm),
    onConfirm: (() -> Unit)? = { onDismiss() },
    cancelTitle: String = stringResource(R.string.cancel),
    onCancel: (() -> Unit)? = { onDismiss() },
    title: String,
    message: String,
    useMarkdown: Boolean = false
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        modifier = Modifier.padding(top = 40.dp, bottom = 40.dp),
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                if (useMarkdown) {
                    // 更换一个 Markdown 方案，该方案的代码语句背景色不对
                    Markdown(message)
                } else {
                    Text(message)
                }
            }
        },
        confirmButton = {
            onConfirm?.let {
                TextButton(onClick = {
                    it()
                    onDismiss()
                }) {
                    Text(confirmTitle)
                }
            }
        },
        dismissButton = {
            onCancel?.let {
                TextButton(onClick = {
                    it()
                    onDismiss()
                }) {
                    Text(cancelTitle)
                }
            }
        }
    )
}

@Composable
fun PermissionExplanationDialog(
    permissionRequest: PermissionRequest,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    when (permissionRequest) {
        // 其他普通权限
        is PermissionRequest.Common -> {
            if (XXPermissions.isGrantedPermissions(context, permissionRequest.permissions)) {
                permissionRequest.onAllGranted(permissionRequest.permissions)
                return
            }

            val message = permissionRequest.descriptionFun(permissionRequest.permissions)
            ActionDialog(
                onDismiss = onDismiss,
                onConfirm = {
                    onDismiss()
                    requestPermission(
                        context,
                        activity,
                        permissionRequest.onAllGranted,
                        permissionRequest.onDoNotAskAgain,
                        permissionRequest.onRequested,
                        permissions = permissionRequest.permissions
                    )
                },
                confirmTitle = stringResource(R.string.go_to_request),
                title = stringResource(R.string.request_permission),
                message = message,
                useMarkdown = true
            )
        }

        // 定位权限
        is PermissionRequest.Location -> {
            val fineLocation = PermissionLists.getAccessFineLocationPermission()
            val coarseLocation = PermissionLists.getAccessCoarseLocationPermission()
            val isFineGranted = XXPermissions.isGrantedPermission(context, fineLocation)
            val isCoarseGranted = XXPermissions.isGrantedPermission(context, coarseLocation)
            if (isFineGranted && isCoarseGranted) {
                permissionRequest.hasFineLocation()
                return
            }

            val message = permissionRequest.descriptionFun(listOf(fineLocation, coarseLocation))
            ActionDialog(
                onDismiss = onDismiss,
                onConfirm = {
                    onDismiss()
                    requestLocationPermission(
                        context,
                        activity,
                        hasFineLocation = permissionRequest.hasFineLocation,
                        onlyCoarseLocation = permissionRequest.onlyCoarseLocation,
                        onDoNotAskAgain = permissionRequest.onDoNotAskAgain,
                        onRequested = permissionRequest.onRequested
                    )
                },
                confirmTitle = stringResource(R.string.go_to_request),
                title = stringResource(R.string.request_permission),
                message = message,
                useMarkdown = true
            )
        }

        // 自启动权限
        is PermissionRequest.AutoStart -> {
            val message = permissionRequest.descriptionFun(listOf())
            ActionDialog(
                onDismiss = onDismiss,
                onConfirm = {
                    onDismiss()
                    requestAutoStartPermission(context)
                },
                confirmTitle = stringResource(R.string.go_to_request),
                title = stringResource(R.string.request_permission),
                message = message,
                useMarkdown = true
            )
        }
    }
}