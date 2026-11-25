package com.wanderer.journal.helpers.file

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.net.toUri
import com.wanderer.journal.WandererJournal
import com.wanderer.journal.auxiliary.enums.unique.LogTags

/**
 * 获取用于打开 SAF 选择文件的函数
 *
 * @param onResult 目录选择结果回调，形参为选择的文件的 Uri
 * @return 用于启动 SAF 并选择文件的函数，形参为目标文件的类型
 */
@Composable
fun rememberDocumentLauncher(
    onResult: (Uri?) -> Unit
): (fileTypes: Array<String>) -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        WandererJournal.lockLifecycleObserver(false)
        currentOnResult(uri)
    }

    return remember(launcher) {
        { fileTypes: Array<String> ->
            WandererJournal.lockLifecycleObserver(true)
            launcher.launch(fileTypes)
        }
    }
}

/**
 * 获取通过 SAF 创建文件的函数
 *
 * @param onResult 选择完文件创建位置的回调，形参为生成的文件的 Uri
 * @return 打开 SAF 并创建文件的函数，形参为创建文件的类型和默认文件名
 */
@Composable
fun rememberCreateDocumentLauncherManual(
    onResult: (Uri?) -> Unit
): (mineType: String, fileName: String) -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        WandererJournal.lockLifecycleObserver(false)
        val uri = if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data
        } else null
        currentOnResult(uri)
    }

    return remember(launcher) {
        { intentType: String, fileName: String ->
            WandererJournal.lockLifecycleObserver(true)
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = intentType
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
            launcher.launch(intent)
        }
    }
}

/**
 * 获取一个用于启动 SAF 并选择目录的函数
 *
 * @param onResult 目录选择结果回调，形参为选择的目录的 Uri
 * @return 用于启动 SAF 并选择目录的函数，形参为进入 SAF 时的初始位置
 */
@Composable
fun rememberDocumentTreeLauncher(
    onResult: (Uri?) -> Unit
): (String?) -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        // 锁定/解锁生命周期逻辑放这里，或放到外层调用处
        WandererJournal.lockLifecycleObserver(false)
        currentOnResult(uri)
    }

    return remember(launcher) {
        { initialUri: String? ->
            WandererJournal.lockLifecycleObserver(true)
            launcher.launch(
                initialUri?.toUri()
            )
        }
    }
}

/**
 * 从 SAF 返回的 Uri 中提取出可读的路径
 *
 * @param context 上下文
 * @param treeUri 用户选择目录后返回的 Uri
 * @return 可读的路径字符串
 */
fun getReadablePathFromSafUri(context: Context, treeUri: Uri): String {
    val documentId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }
        .getOrNull()
        ?: return getDisplayName(context, treeUri)

    val parts = documentId.split(":")
    val storageId = parts[0]
    val relativePath = parts.getOrElse(1) { "" }

    val pathInfo = when {
        storageId.equals("primary", ignoreCase = true) -> "/storage/emulated/0/$relativePath"
        else -> return getDisplayName(context, treeUri)
    }

    return pathInfo.removeSuffix("/")
}

/**
 * 查询 Uri 的系统显示名称 (Display Name)
 *
 * @param context 上下文
 * @param uri 需要获取显示名称的目录的 Uri
 */
private fun getDisplayName(context: Context, uri: Uri): String {
    // 先尝试按 Document Uri 直接查询（更通用）
    queryDisplayName(context, uri)?.let { return it }

    // 如果是 Tree Uri，再尝试转换后查询
    try {
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            uri,
            DocumentsContract.getTreeDocumentId(uri)
        )
        queryDisplayName(context, documentUri)?.let { return it }
    } catch (e: Exception) {
        Log.e(LogTags.SAF_HELPER.n(), "Tree Uri 转换失败", e)
    }

    return uri.lastPathSegment ?: uri.toString()
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    return try {
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) cursor.getString(index)?.takeIf { it.isNotEmpty() } else null
            } else null
        }
    } catch (e: Exception) {
        Log.e(LogTags.SAF_HELPER.n(), "无法获取 Uri 的显示名称", e)
        null
    }
}