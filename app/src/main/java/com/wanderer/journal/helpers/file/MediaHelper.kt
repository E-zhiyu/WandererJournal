package com.wanderer.journal.helpers.file

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.WebView
import androidx.core.graphics.createBitmap
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LifecycleOwner
import com.wanderer.journal.auxiliary.classes.CustomDateTimeFormatter
import com.wanderer.journal.auxiliary.classes.file.MediaDetail
import com.wanderer.journal.auxiliary.classes.file.MediaFileInfo
import com.wanderer.journal.auxiliary.enums.DirectoryPaths
import com.wanderer.journal.auxiliary.enums.unique.LogTags
import com.wanderer.journal.auxiliary.interfaces.ImageCompressCallback
import com.wanderer.journal.data.save.db.TypeConverter
import com.wanderer.journal.helpers.appearance.AppearanceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import top.zibin.luban.api.Luban
import top.zibin.luban.api.OnCompressListener
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Field
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.util.Locale
import kotlin.coroutines.resume


// ==================== WebView 截图 ====================

/**
 * 后台测绘并将 WebView 转换为全量长图
 *
 * 注意：WebView 的 measure/layout/draw 必须在主线程调用，
 * 调用方需保证在主线程（或本方法内切回 Main）。
 */
suspend fun captureWebView(webView: WebView?): Bitmap? = withContext(Dispatchers.Main) {
    webView ?: return@withContext null

    try {
        val context = webView.context
        val screenWidthPx = AppearanceHelper.getScreenWidth(context)
        val density = context.resources.displayMetrics.density
        Log.d(LogTags.MEDIA_HELPER.n(), "屏幕密度：$density")
        val realHeight = (webView.contentHeight * density).toInt()

        webView.measure(screenWidthPx, realHeight)
        webView.layout(0, 0, screenWidthPx, realHeight)
        Log.d(LogTags.MEDIA_HELPER.n(), "宽度：$screenWidthPx")
        Log.d(LogTags.MEDIA_HELPER.n(), "高度：$realHeight")

        val bitmap = createBitmap(screenWidthPx, realHeight)
        val canvas = Canvas(bitmap)
        webView.scrollTo(0, 0)
        webView.draw(canvas)

        bitmap
    } catch (_: OutOfMemoryError) {
        Log.e(LogTags.MEDIA_HELPER.n(), "内存不足，无法转换为图片")
        null
    }
}

/**
 * 将 Bitmap 保存为文件
 */
suspend fun saveBitmapToFile(context: Context, bitmap: Bitmap): File? =
    withContext(Dispatchers.IO) {
        val shareFolder = DirectoryPaths.MEDIA_TEMP.getDir(context) ?: return@withContext null
        val imageFile = File(shareFolder, "diary_share_${System.currentTimeMillis()}.jpg")

        try {
            FileOutputStream(imageFile).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.flush()
            }
            imageFile
        } catch (_: IOException) {
            Log.e(LogTags.MEDIA_HELPER.n(), "无法创建图片")
            null
        }
    }

// ==================== 保存媒体到相册 ====================

/**
 * 将媒体文件保存至相册
 *
 * @return 保存后的媒体文件的 content Uri
 */
suspend fun saveMediaToGallery(context: Context, mediaUri: Uri?): Uri =
    withContext(Dispatchers.IO) {
        if (mediaUri?.path == null) {
            throw RuntimeException("无法获取媒体文件路径")
        }

        val sourceFile = File(mediaUri.path!!)
        if (!sourceFile.exists()) {
            throw RuntimeException("媒体文件不存在")
        }

        val fileName = sourceFile.name
        // 根据文件后缀获取 MimeType
        val extension = MimeTypeMap.getFileExtensionFromUrl(mediaUri.toString())
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension.lowercase(Locale.ROOT))
        val isVideo = mimeType?.startsWith("video") == true

        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        }

        val collectionUri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isVideo) {
                collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                values.put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_MOVIES}/${context.packageName}"
                )
            } else {
                collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                values.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/${context.packageName}"
                )
            }
            values.put(MediaStore.MediaColumns.IS_PENDING, 1)
        } else {
            val targetDir = if (isVideo) {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            }
            val appDir = File(targetDir, context.packageName)
            if (!appDir.exists() && !appDir.mkdirs()) {
                throw RuntimeException("无法创建媒体文件保存目录")
            }
            val targetFile = File(appDir, fileName)
            values.put(MediaStore.MediaColumns.DATA, targetFile.absolutePath)

            collectionUri = if (isVideo) {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        }

        val itemUri = resolver.insert(collectionUri, values)
            ?: throw RuntimeException("无法获取保存后的媒体文件路径")

        try {
            FileInputStream(sourceFile).use { inputStream ->
                resolver.openOutputStream(itemUri)?.use { outputStream ->
                    val buffer = ByteArray(4096)
                    var byteCount: Int
                    while (inputStream.read(buffer).also { byteCount = it } != -1) {
                        outputStream.write(buffer, 0, byteCount)
                    }
                    outputStream.flush()
                } ?: throw IOException("无法打开输出流")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
            }

            itemUri
        } catch (e: IOException) {
            resolver.delete(itemUri, null, null)
            throw e
        }
    }

// ==================== HDR 检测 ====================

/**
 * 根据图片的 Uri 判断其是否为 HDR 图片
 */
suspend fun isHdrImage(context: Context, imageUri: Uri?): Boolean =
    withContext(Dispatchers.IO) {
        // GainMap API 在 Android 14 (API 34) 引入
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return@withContext false
        }
        imageUri ?: return@withContext false

        var inputStream: InputStream? = null
        var bitmap: Bitmap? = null
        try {
            inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext false

            val options = BitmapFactory.Options().apply { inSampleSize = 4 }
            bitmap = BitmapFactory.decodeStream(inputStream, null, options)

            bitmap?.hasGainmap() == true
        } finally {
            bitmap?.recycle()
            try {
                inputStream?.close()
            } catch (_: IOException) {
            }
        }
    }

// ==================== 读取媒体目录 ====================

/**
 * 读取媒体文件夹中的图片文件
 */
suspend fun readMediaDir(context: Context): List<MediaFileInfo> =
    withContext(Dispatchers.IO) {
        val result = mutableListOf<MediaFileInfo>()
        val mediaDir = DirectoryPaths.MEDIA.getDir(context)
        if (mediaDir == null || !mediaDir.exists() || !mediaDir.isDirectory) {
            return@withContext result
        }

        val mimeTypeMap = MimeTypeMap.getSingleton()

        val childFiles = mediaDir.listFiles { _, name ->
            val extension = getFileExtension(name) ?: return@listFiles false
            val mimeType = mimeTypeMap.getMimeTypeFromExtension(extension.lowercase(Locale.ROOT))
            mimeType?.startsWith("image/") == true
        } ?: return@withContext result

        for (file in childFiles) {
            val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
            val size = attrs.size()
            val creationTimestamp = attrs.lastModifiedTime().toMillis()
            val fileName = file.name
            val uri = Uri.fromFile(file)
            result.add(MediaFileInfo(uri, size, fileName, creationTimestamp))
        }

        result
    }

// ==================== 媒体详情 ====================

/**
 * 获取媒体文件的详细数据
 */
suspend fun getMediaDetail(context: Context, uri: Uri): MediaDetail =
    withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var wi = 0
        var hei = 0
        var iso = 0
        var device = ""
        var aperture = 0.0
        var exp = 0.0
        var focalLength = 0.0
        var flashFired = false
        var time: LocalDateTime? = null

        val fileSize = getFileSizeByUri(context, uri)
        val fileName = getFileNameByUri(context, uri)

        try {
            resolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)

                // --- 拍摄时间 / 修改时间 ---
                val timeStr = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                time = if (timeStr != null) {
                    LocalDateTime.parse(timeStr, CustomDateTimeFormatter.DateTimeSecond)
                } else {
                    val lastModified = getLastModifyTimeByUri(context, uri)
                    TypeConverter.toLocalDateTime(lastModified)
                }

                // --- 像素尺寸 ---
                wi = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
                hei = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)

                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                    orientation == ExifInterface.ORIENTATION_ROTATE_270
                ) {
                    val temp = wi
                    wi = hei
                    hei = temp
                }

                // --- 拍摄设备 ---
                val make = exif.getAttribute(ExifInterface.TAG_MAKE)
                val model = exif.getAttribute(ExifInterface.TAG_MODEL)
                if (model != null) device += model
                if (make != null) device += ", $make"

                // --- 光圈 ---
                aperture = exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, 0.0)

                // --- 快门速度 ---
                val exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
                exp = if (exposureTime != null) {
                    exposureTime.toDoubleOrNull() ?: -1.0
                } else -1.0

                // --- ISO ---
                iso = exif.getAttributeInt(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, 0)

                // --- 焦距 ---
                focalLength = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0)

                // --- 闪光灯 ---
                val flash = exif.getAttributeInt(ExifInterface.TAG_FLASH, -1)
                if (flash != -1) {
                    flashFired = (flash and 0x1) != 0
                }
            }
        } catch (e: IOException) {
            // 读取 EXIF 失败时使用已获取的默认值
            Log.w(LogTags.MEDIA_HELPER.n(), "读取 EXIF 失败：${e.message}")
        }

        MediaDetail(
            time,
            fileSize,
            wi,
            hei,
            fileName,
            device,
            aperture,
            exp,
            iso,
            focalLength,
            flashFired
        )
    }

// ==================== EXIF 读写 ====================

/**
 * 从文件中读取 EXIF 数据
 */
suspend fun getExifFromFile(target: File): Map<String, String> =
    withContext(Dispatchers.IO) {
        /**
         * 获取所有 EXIF 的标签
         */
        fun getAllExifTags(): List<String> {
            val tags = mutableListOf<String>()
            val fields: Array<Field> = ExifInterface::class.java.fields
            for (field in fields) {
                try {
                    val value = field.get(null)
                    if (value is String) tags.add(value)
                } catch (_: IllegalAccessException) {
                }
            }
            return tags
        }

        val sourceExif = ExifInterface(target)
        val exifMap = mutableMapOf<String, String>()
        for (tag in getAllExifTags()) {
            sourceExif.getAttribute(tag)?.let { exifMap[tag] = it }
        }
        exifMap
    }

/**
 * 将 EXIF 数据写入到文件中
 */
suspend fun writeExifToFile(target: File, exifMap: Map<String, String>?) =
    withContext(Dispatchers.IO) {
        if (exifMap.isNullOrEmpty()) return@withContext

        val targetExif = ExifInterface(target)
        for ((key, value) in exifMap) {
            targetExif.setAttribute(key, value)
        }
        targetExif.saveAttributes()
    }

// ==================== 图片压缩 ====================

/**
 * 压缩图片并替换原图
 *
 * Luban 是回调式 API，这里用 `suspendCancellableCoroutine` 桥接。
 * 成功时 resume；失败时 resume 对应的错误。
 */
suspend fun compressImage(
    uri: Uri?,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    callback: ImageCompressCallback? = null
) = withContext(Dispatchers.IO) {
    // 判断 Uri 类型
    if (uri == null || ContentResolver.SCHEME_FILE != uri.scheme) {
        Log.e(LogTags.MEDIA_HELPER.n(), "Uri类型为空或不为file")
        callback?.failed("无法获取原图文件")
        return@withContext
    }

    // 获取输出目录
    val outputDir = DirectoryPaths.MEDIA_TEMP.getDir(context)
    if (outputDir == null) {
        Log.e(LogTags.MEDIA_HELPER.n(), "无法获取压缩图片输出目录")
        callback?.failed("无法获取图片输出目录")
        return@withContext
    }

    // 获取原始文件数据
    val path = uri.path ?: run {
        Log.e(LogTags.MEDIA_HELPER.n(), "无法从Uri中获取路径")
        callback?.failed("无法获取原图文件")
        return@withContext
    }
    val originFile = File(path)
    val originExifMap = getExifFromFile(originFile)
    val originSize = Files.readAttributes(
        originFile.toPath(), BasicFileAttributes::class.java
    ).size()
    val originModifiedTime = originFile.lastModified()

    // Luban 的 launch() 要求在带 Looper 的线程调用，
    // 通过 suspendCancellableCoroutine 切回 Main 触发
    withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            Luban.with(context)
                .load(uri)
                .bindLifecycle(lifecycleOwner)
                .setTargetDir(outputDir)
                .setCompressListener(object : OnCompressListener {
                    override fun onStart() {
                    }

                    override fun onSuccess(file: File) {
                        val newSize = Files.readAttributes(
                            file.toPath(), BasicFileAttributes::class.java
                        ).size()

                        if (originSize <= newSize) {
                            Log.w(LogTags.MEDIA_HELPER.n(), "压缩后大小无变化或者更大")
                            callback?.failed("图片已无法继续压缩")
                            cont.resume(Unit)
                            return
                        }

                        val copiedPath = Files.copy(
                            file.toPath(),
                            originFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                        )
                        if (!copiedPath.toFile().setLastModified(originModifiedTime)) {
                            Log.w(LogTags.MEDIA_HELPER.n(), "无法恢复原来的最后编辑时间")
                        }
                        runBlocking { writeExifToFile(copiedPath.toFile(), originExifMap) }

                        callback?.succeed()
                        cont.resume(Unit)
                    }

                    override fun onError(e: Throwable) {
                        // 清理临时目录是 suspend，需在外部协程处理，这里直接调起
                        callback?.failed("图片压缩失败")
                        cont.resume(Unit)
                    }
                })
                .launch()
        }
    }
}