package com.wanderer.journal.helpers.file

import android.content.Context
import android.net.Uri
import android.util.Log
import com.wanderer.journal.auxiliary.enums.DirectoryPaths
import com.wanderer.journal.auxiliary.enums.unique.LogTags
import com.wanderer.journal.data.backup.BackupJson
import com.wanderer.journal.data.backup.helpers.BackupHelperBase
import com.wanderer.journal.data.backup.maps.DataInfoMap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ZipHelper {
    /**
     * 将源文件或目录打包到 [ZipOutputStream]。
     *
     * @param sourceFile 需要添加到压缩包的文件或目录
     * @param fileNameInZip 在压缩包中的路径名称
     * @param zos [ZipOutputStream] 实例，用于将文件写入压缩包
     */
    private fun addToZip(sourceFile: File, fileNameInZip: String, zos: ZipOutputStream) {
        if (sourceFile.isHidden) return

        if (sourceFile.isDirectory) {
            // 如果是目录，确保以 "/" 结尾
            val folderName = if (fileNameInZip.endsWith("/")) fileNameInZip else "$fileNameInZip/"
            zos.putNextEntry(ZipEntry(folderName))
            zos.closeEntry()

            sourceFile.listFiles()?.forEach { child ->
                // 递归添加子文件，路径累加
                addToZip(child, folderName + child.name, zos)
            }
        } else {
            // 如果是文件，直接写入
            FileInputStream(sourceFile).use { fis ->
                zos.putNextEntry(ZipEntry(fileNameInZip))

                val buffer = ByteArray(1024)
                while (true) {
                    val length = fis.read(buffer)
                    if (length < 0) break
                    zos.write(buffer, 0, length)
                }
                zos.closeEntry()
            }
        }
    }

    /**
     * 生成备份文件。
     *
     * @param targetUri 目标压缩包的 Uri
     * @param context 上下文
     * @param includeMedia 是否打包临时文件目录
     */
    @JvmStatic
    suspend fun createBackupFile(
        targetUri: Uri,
        context: Context,
        includeMedia: Boolean,
    ) = withContext(Dispatchers.IO) {
        val tempDir = DirectoryPaths.DATA_TEMP.getDir(context)
            ?: throw IOException("无法获取临时数据目录")
        val mediaDir = DirectoryPaths.MEDIA.getDir(context)

        // 1. 获取 SAF 的输出流
        val os: OutputStream = context.contentResolver.openOutputStream(targetUri)
            ?: throw IOException("无法打开输出流，请检查权限或路径")

        try {
            // 将 ZipOutputStream 包装在外部传入的流上
            ZipOutputStream(os).use { zos ->
                // 1. 打包临时 JSON 目录下的所有 JSON 文件（不包含目录本身）
                tempDir.listFiles()?.forEach { file ->
                    if (!file.name.endsWith(".json")) return@forEach
                    addToZip(file, file.name, zos)
                }

                // 2. 将图片目录整体添加进去
                if (includeMedia && mediaDir != null && mediaDir.exists() && mediaDir.isDirectory) {
                    addToZip(mediaDir, mediaDir.name, zos)
                }
            }
        } finally {
            // 3. 无论成功失败，清理临时 JSON 目录
            FileHelper.clearTempDataDir(context)
        }
    }

    /**
     * 从备份文件中获取备份数据信息。
     *
     * @param context 上下文
     * @param zipUri 指向备份文件的 Uri
     * @return 备份数据信息，无法读取时返回 `null`
     */
    @JvmStatic
    fun getDataInfoFromBackupFile(context: Context, zipUri: Uri): DataInfoMap? {
        val jsonBuffer = ByteArray(1024 * 8)
        val inputStream = context.contentResolver.openInputStream(zipUri) ?: return null

        inputStream.use { input ->
            ZipInputStream(input).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val name = entry.name

                    if (name == BackupHelperBase.DATA_INFO_JSON && !entry.isDirectory) {
                        // 读取文件内容
                        val buffer = ByteArrayOutputStream()
                        while (true) {
                            val len = zis.read(jsonBuffer)
                            if (len == -1) break
                            buffer.write(jsonBuffer, 0, len)
                        }
                        val content = buffer.toString()

                        // 反序列化
                        return BackupJson.decodeFromString(DataInfoMap.serializer(), content)
                    } else {
                        zis.closeEntry()
                    }

                    entry = zis.nextEntry
                }
            }
        }

        return null
    }

    /**
     * 根据用户选择结果解压备份文件。
     *
     * @param context 上下文
     * @param zipUri 压缩包的 Uri
     * @param allowedFiles 允许解压的文件名称列表
     * @param includeMedia 是否解压媒体文件
     * @return 已解压好的 JSON 文件列表，用于后续读取并导入数据
     */
    @JvmStatic
    suspend fun unpackBackupFileWithFilter(
        context: Context,
        zipUri: Uri,
        allowedFiles: Set<String>,
        includeMedia: Boolean,
    ): List<File> = withContext(Dispatchers.IO) {
        // 获取存放解压文件的目标文件夹
        val tempJsonDir = DirectoryPaths.DATA_TEMP.getDir(context)
            ?: throw IOException("无法获取临时数据目录")
        val mediaDir = DirectoryPaths.MEDIA.getDir(context)
        val mediaDirName = DirectoryPaths.MEDIA.childDirName + "/"

        // 清空媒体文件目录
        if (includeMedia && mediaDir != null) {
            FileHelper.clearMediaDir(context)
        }

        // 创建共享缓冲区，防止频繁 GC
        val extractedJsonFiles = mutableListOf<File>()
        val jsonBuffer = ByteArray(1024 * 8)
        val mediaBuffer = ByteArray(1024 * 32)

        // 开始执行解压逻辑
        val inputStream = context.contentResolver.openInputStream(zipUri)
            ?: throw IOException("无法打开备份文件的输入流")

        inputStream.use { input ->
            ZipInputStream(input).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val name = entry.name

                    if (name.startsWith(mediaDirName) && name != mediaDirName && includeMedia) {
                        // 处理媒体文件，去掉开头的文件夹路径
                        val mediaRoot = mediaDir
                            ?: throw IOException("无法获取媒体文件目录")
                        val outFile = File(mediaRoot, name.substring(mediaDirName.length))
                        extractFile(zis, outFile, mediaBuffer)
                    } else if (allowedFiles.contains(name)) {
                        // 处理根目录下的 JSON 文件
                        val outFile = File(tempJsonDir, name)
                        extractFile(zis, outFile, jsonBuffer)
                        extractedJsonFiles.add(outFile)
                    }
                    zis.closeEntry()

                    entry = zis.nextEntry
                }
            }
        }

        extractedJsonFiles
    }

    /**
     * 具体解压写入文件的实现。
     */
    private fun extractFile(zis: ZipInputStream, outFile: File, buffer: ByteArray) {
        // 确保父目录存在
        val parent = outFile.parentFile
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            Log.e(LogTags.ZIP_HELPER.n(), "父目录不存在且无法创建")
            return
        }

        FileOutputStream(outFile).use { fos ->
            while (true) {
                val len = zis.read(buffer)
                if (len <= 0) break
                fos.write(buffer, 0, len)
            }
            // 刷新缓冲区并同步到磁盘
            fos.fd.sync()
        }
    }
}
