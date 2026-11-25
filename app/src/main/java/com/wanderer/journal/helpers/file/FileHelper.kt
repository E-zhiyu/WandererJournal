package com.wanderer.journal.helpers.file

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.wanderer.journal.auxiliary.classes.CustomDateTimeFormatter
import com.wanderer.journal.auxiliary.classes.file.TextFileData
import com.wanderer.journal.auxiliary.enums.DirectoryPaths
import com.wanderer.journal.auxiliary.enums.unique.LogTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

private const val BACKUP_FILE_NAME = "Wanderer Journal Backup"

// ==================== 文件名 / 扩展名 ====================

/**
 * 获取文件扩展名
 */
fun getFileExtension(fileName: String): String? {
    val lastDotIndex = fileName.lastIndexOf('.')
    return if (lastDotIndex > 0 && lastDotIndex < fileName.length - 1) {
        fileName.substring(lastDotIndex + 1)
    } else null
}

/**
 * 生成备份文件名称
 */
fun generateBackupFileName(): String = String.format(
    Locale.getDefault(),
    "%s_%s.zip",
    BACKUP_FILE_NAME,
    LocalDateTime.now().format(CustomDateTimeFormatter.BACKUP)
)

/**
 * 获取可用的文件名，当文件名被占用时自动添加编号以防文件名冲突
 */
private fun getAvailableFilename(targetDir: File, originName: String): String {
    val namePart = originName.split(".", limit = 2)
    // 若没有扩展名，直接按无后缀处理
    val suffix = namePart.getOrNull(1)
    val originFileName = namePart[0]

    var availableName = originName
    var targetFile = File(targetDir, availableName)

    var i = 0
    while (targetFile.exists()) {
        i++
        availableName = if (suffix != null) {
            String.format(Locale.getDefault(), "%s (%d).%s", originFileName, i, suffix)
        } else {
            String.format(Locale.getDefault(), "%s (%d)", originFileName, i)
        }
        targetFile = File(targetDir, availableName)
    }

    return availableName
}

// ==================== 分享 ====================

/**
 * 分享单个文件
 *
 * 注意：`context.startActivity` 属于 UI 操作，需在主线程执行；
 * 但此方法内没有耗时 IO，故整个方法就在调用方所在线程执行，
 * 不要在 IO 调度器里调用它（或者调用方切回 Main）。
 */
suspend fun shareFile(context: Context, targetFile: File?, intentType: String) {
    if (targetFile == null || !targetFile.exists()) {
        Log.e(LogTags.FILE_HELPER.n(), "分享的文件不存在")
        throw RuntimeException("分享的文件不存在")
    }

    // 获取 content 类型的 Uri
    val contentUri = FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        targetFile
    )

    // 创建分享 Intent
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = intentType
        putExtra(Intent.EXTRA_STREAM, contentUri)
        putExtra(Intent.EXTRA_TEXT, targetFile.name)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooserIntent = Intent.createChooser(shareIntent, targetFile.name)
    if (chooserIntent.resolveActivity(context.packageManager) != null) {
        withContext(Dispatchers.Main) {
            context.startActivity(chooserIntent)
        }
    } else {
        throw RuntimeException("未找到可分享的应用")
    }
}

// ==================== 删除 / 移动 / 复制 ====================

/**
 * 删除文件
 */
suspend fun deleteFile(uri: Uri, context: Context) = withContext(Dispatchers.IO) {
    when (uri.scheme) {
        ContentResolver.SCHEME_FILE -> {
            // 绝对路径的 file:// 协议临时文件
            val path = uri.path ?: return@withContext
            val file = File(path)
            if (file.exists()) {
                val deleted = file.delete()
                Log.d(LogTags.FILE_HELPER.n(), "File delete: $deleted -> ${file.absolutePath}")
            }
        }

        ContentResolver.SCHEME_CONTENT -> {
            // FileProvider 生成的 content:// 协议临时文件
            try {
                context.contentResolver.delete(uri, null, null)
                Log.d(LogTags.FILE_HELPER.n(), "ContentUri delete success: $uri")
            } catch (e: UnsupportedOperationException) {
                Log.e(LogTags.FILE_HELPER.n(), "ContentUri delete failed: $uri")
            }
        }
    }
}

/**
 * 移动文件
 *
 * @return 移动完成后的文件对象，移动失败则返回 null
 */
suspend fun moveFile(originFile: File?, targetDir: File?): File? = withContext(Dispatchers.IO) {
    require(targetDir != null && targetDir.isDirectory) {
        "参数错误，targetDir不是目录对象"
    }

    if (originFile == null || !originFile.exists()) {
        Log.w(LogTags.FILE_HELPER.n(), "待移动的文件不存在")
        return@withContext null
    }

    val targetFile = File(targetDir, getAvailableFilename(targetDir, originFile.name))

    try {
        val movedPath = Files.move(originFile.toPath(), targetFile.toPath())
        Log.i(LogTags.FILE_HELPER.n(), "文件移动成功")
        movedPath.toFile()
    } catch (_: IOException) {
        Log.e(LogTags.FILE_HELPER.n(), "文件移动失败")
        null
    }
}

/**
 * 复制文件
 *
 * @param buffer 缓冲区，用于复制多个文件时共享，减小内存压力
 * @return 复制成功则返回复制成功的文件，失败则返回 null
 */
suspend fun copyFile(
    context: Context,
    uri: Uri,
    targetDir: File?,
    buffer: ByteArray? = null
): File? = withContext(Dispatchers.IO) {
    if (targetDir == null || !targetDir.exists()) {
        Log.e(LogTags.FILE_HELPER.n(), "目标文件夹不存在")
        return@withContext null
    }

    // 获取或生成文件名
    var fileName: String? = null
    val documentFile = DocumentFile.fromSingleUri(context, uri)
    if (documentFile?.exists() == true) {
        fileName = documentFile.name
    }
    if (fileName == null) {
        fileName = "copied_media_${System.currentTimeMillis()}.jpg"
    }

    val targetFile = File(targetDir, fileName)

    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(targetFile).use { outputStream ->
            val buf = buffer ?: ByteArray(1024 * 32)
            var bytesRead: Int
            while (inputStream.read(buf).also { bytesRead = it } != -1) {
                outputStream.write(buf, 0, bytesRead)
            }
            outputStream.flush()
        }
    } ?: return@withContext null

    targetFile
}

// ==================== 读取 ====================

/**
 * 读取文件内容以及最后编辑时间
 */
suspend fun readContentWithLastModifyTime(uri: Uri, context: Context): TextFileData =
    withContext(Dispatchers.IO) {
        val content = readContent(uri, context)
        val lastModifyTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(getLastModifyTimeByUri(context, uri)),
            ZoneId.systemDefault()
        )
        TextFileData(content, lastModifyTime)
    }

/**
 * 获取文本文件的总行数
 */
suspend fun getLinesCount(uri: Uri, context: Context): Int = withContext(Dispatchers.IO) {
    var lineCount = 0
    context.contentResolver.openInputStream(uri)?.use { isStream ->
        BufferedReader(InputStreamReader(isStream)).use { reader ->
            while (reader.readLine() != null) {
                lineCount++
            }
        }
    }
    lineCount
}

/**
 * 通过 Uri 获取文件大小，单位为 B
 */
suspend fun getFileSizeByUri(context: Context?, uri: Uri?): Long = withContext(Dispatchers.IO) {
    if (context == null || uri == null) return@withContext -1L

    when (uri.scheme) {
        ContentResolver.SCHEME_CONTENT -> {
            context.contentResolver.query(
                uri, arrayOf(OpenableColumns.SIZE), null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                        return@withContext cursor.getLong(sizeIndex)
                    }
                }
            }
            -1L
        }

        ContentResolver.SCHEME_FILE -> {
            val file = uri.path?.let { File(it) }
            if (file?.exists() == true) file.length() else -1L
        }

        else -> -1L
    }
}

/**
 * 通过 Uri 获取文件名
 */
suspend fun getFileNameByUri(context: Context?, uri: Uri?): String? =
    withContext(Dispatchers.IO) {
        if (context == null || uri == null) return@withContext null

        when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                context.contentResolver.query(
                    uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1 && !cursor.isNull(index)) {
                            return@withContext cursor.getString(index)
                        }
                    }
                }
                null
            }

            ContentResolver.SCHEME_FILE -> {
                val path = uri.path ?: return@withContext null
                val lastSlashIndex = path.lastIndexOf('/')
                if (lastSlashIndex != -1) path.substring(lastSlashIndex + 1) else path
            }

            else -> null
        }
    }

/**
 * 根据 Uri 获取文件最后修改时间的时间戳
 */
suspend fun getLastModifyTimeByUri(context: Context?, uri: Uri?): Long =
    withContext(Dispatchers.IO) {
        if (context == null || uri == null) return@withContext -1L

        when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                context.contentResolver.query(
                    uri,
                    arrayOf(DocumentsContract.Document.COLUMN_LAST_MODIFIED),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(
                            DocumentsContract.Document.COLUMN_LAST_MODIFIED
                        )
                        if (index != -1 && !cursor.isNull(index)) {
                            return@withContext cursor.getLong(index)
                        }
                    }
                }
                -1L
            }

            ContentResolver.SCHEME_FILE -> {
                val file = uri.path?.let { File(it) }
                if (file?.exists() == true) file.lastModified() else -1L
            }

            else -> -1L
        }
    }

/**
 * 读取文本文件内容（Uri 版本）
 */
suspend fun readContent(uri: Uri, context: Context): String =
    withContext(Dispatchers.IO) {
        val builder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { isStream ->
            BufferedReader(InputStreamReader(isStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line).append("\n")
                }
            }
        }
        builder.toString()
    }

/**
 * 读取文本文件内容（File 版本）
 */
suspend fun readContent(file: File): String = withContext(Dispatchers.IO) {
    val content = StringBuilder()
    BufferedReader(FileReader(file)).use { reader ->
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            content.append(line)
        }
    }
    content.toString()
}

// ==================== 写入 ====================

/**
 * 将字符串写入到临时数据文件中
 */
suspend fun writeContentToTempDataFile(context: Context, fileName: String, content: String) =
    withContext(Dispatchers.IO) {
        val tempDataDir = DirectoryPaths.DATA_TEMP.getDir(context)
        tempDataDir?.let { createFileAndWrite(it, fileName, content) }
    }

/**
 * 创建文件并写入指定的字符串
 */
suspend fun createFileAndWrite(dir: File, fileName: String, content: String) =
    withContext(Dispatchers.IO) {
        val targetFile = File(dir, fileName)
        BufferedWriter(FileWriter(targetFile)).use { writer ->
            writer.write(content)
        }
    }

// ==================== 目录清理 ====================

suspend fun clearTempDataDir(context: Context) = withContext(Dispatchers.IO) {
    clearDir(DirectoryPaths.DATA_TEMP.getDir(context))
}

suspend fun clearMediaDir(context: Context) = withContext(Dispatchers.IO) {
    clearDir(DirectoryPaths.MEDIA.getDir(context))
}

suspend fun clearMediaTempDir(context: Context) = withContext(Dispatchers.IO) {
    clearDir(DirectoryPaths.MEDIA_TEMP.getDir(context))
}

/**
 * 清理某个目录
 */
suspend fun clearDir(dir: File?) = withContext(Dispatchers.IO) {
    Log.i(LogTags.DATA_IO_HELPER.n(), "开始清除文件……")
    if (dir == null || !dir.isDirectory || !dir.exists()) {
        Log.e(LogTags.DATA_IO_HELPER.n(), "非目录对象或目录不存在")
        return@withContext
    }

    val files = dir.listFiles()
    if (files == null) {
        Log.e(LogTags.DATA_IO_HELPER.n(), "无法获取文件列表")
        return@withContext
    }

    for (file in files) {
        if (!file.delete()) {
            Log.w(LogTags.DATA_IO_HELPER.n(), "无法删除文件：${file.name}")
        }
    }

    Log.i(LogTags.DATA_IO_HELPER.n(), "文件清理完毕")
}