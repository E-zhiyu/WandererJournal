package com.wanderer.journal.helpers.file;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import com.wanderer.journal.auxiliary.classes.CustomDateTimeFormatter;
import com.wanderer.journal.auxiliary.classes.file.MediaDetail;
import com.wanderer.journal.auxiliary.classes.file.MediaFileInfo;
import com.wanderer.journal.auxiliary.enums.DirectoryPaths;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.data.save.db.converters.DateTimeConverter;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class MediaHelper {
    /**
     * 后台测绘并将 WebView 转换为全量长图
     *
     * @param webView 需要测绘的 WebView
     */
    @Nullable
    public static Bitmap captureWebView(WebView webView) {
        if (webView == null) return null;

        try {
            //对 WebView 进行布局
            int screenWidthPx = AppearanceHelper.getScreenWidth(webView.getContext());
            float density = webView.getContext().getResources().getDisplayMetrics().density;
            Log.d(LogTags.MEDIA_HELPER.n(), "屏幕密度：" + density);
            int realHeight = (int) (webView.getContentHeight() * density);
            webView.measure(screenWidthPx, realHeight);
            webView.layout(0, 0, screenWidthPx, realHeight);
            Log.d(LogTags.MEDIA_HELPER.n(), "宽度：" + screenWidthPx);
            Log.d(LogTags.MEDIA_HELPER.n(), "高度：" + realHeight);

            //绘制到画布
            Bitmap bitmap = Bitmap.createBitmap(screenWidthPx, realHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            webView.scrollTo(0, 0); //滚动到顶部
            webView.draw(canvas);

            return bitmap;
        } catch (OutOfMemoryError e) {
            Log.e(LogTags.MEDIA_HELPER.n(), "内存不足，无法转换为图片");
            return null;
        }
    }

    /**
     * 将 Bitmap 保存为文件
     *
     * @param context 上下文
     * @param bitmap  需要保存的 Bitmap 实例
     * @return 保存的图片文件
     */
    @Nullable
    public static File saveBitmapToFile(@NonNull Context context, Bitmap bitmap) {
        // 创建存储路径，通常放在缓存目录，分享完不需要占用用户太多空间
        File shareFolder = DirectoryPaths.MEDIA_TEMP.getDir(context);
        if (shareFolder == null) {
            return null;
        }

        File imageFile = new File(shareFolder, "diary_share_" + System.currentTimeMillis() + ".jpg");

        try (FileOutputStream stream = new FileOutputStream(imageFile)) {
            // 压缩并写入文件
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();

            return imageFile;
        } catch (IOException e) {
            Log.e(LogTags.MEDIA_HELPER.n(), "无法创建图片");
            return null;
        }
    }

    /**
     * 将媒体文件保存至相册
     *
     * @param context  上下文
     * @param mediaUri 媒体文件的 Uri
     * @return 保存后的媒体文件的 file 类型 Uri
     */
    public static Observable<Uri> saveMediaToGalleryObservable(
            Context context,
            Uri mediaUri
    ) {
        return Observable.defer(() -> {
            if (mediaUri == null || mediaUri.getPath() == null) {
                return Observable.error(new RuntimeException("无法获取媒体文件路径"));
            }

            File sourceFile = new File(mediaUri.getPath());
            if (!sourceFile.exists()) {
                return Observable.error(new RuntimeException("媒体文件不存在"));
            }

            String fileName = sourceFile.getName();
            // 根据文件后缀获取 MimeType (例如 image/jpeg, video/mp4)
            String extension = MimeTypeMap.getFileExtensionFromUrl(mediaUri.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            boolean isVideo = mimeType != null && mimeType.startsWith("video");

            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();

            // 设置文件的显示名称和类型
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

            Uri collectionUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 使用分区存储
                if (isVideo) {
                    collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    values.put(
                            MediaStore.Video.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_MOVIES + "/" + context.getPackageName()
                    );
                } else {
                    collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    values.put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/" + context.getPackageName()
                    );
                }
                // IS_PENDING = 1 表示文件正在写入，此时其他APP（如系统相册）不可见，防止文件损坏
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);
            } else {
                // Android 9 及以下老版本处理
                File targetDir = isVideo ?
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) :
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File appDir = new File(targetDir, context.getPackageName());
                if (!appDir.exists() && !appDir.mkdirs()) {
                    return Observable.error(new RuntimeException("无法创建媒体文件保存目录"));
                }
                File targetFile = new File(appDir, fileName);
                values.put(MediaStore.MediaColumns.DATA, targetFile.getAbsolutePath());

                if (isVideo) {
                    collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else {
                    collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
            }

            // 在 MediaStore 中插入一条空记录，获取公共区域的 Uri
            Uri itemUri = resolver.insert(collectionUri, values);
            if (itemUri == null) {
                return Observable.error(new RuntimeException("无法获取保存后的媒体文件路径"));
            }

            // 开始从私有目录拷贝数据到公共区域
            try (InputStream is = new FileInputStream(sourceFile);
                 OutputStream os = resolver.openOutputStream(itemUri)) {

                if (os == null) return null;

                byte[] buffer = new byte[4096];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    os.write(buffer, 0, byteCount);
                }
                os.flush();

                // Android 10+ 写入完成后，释放 IS_PENDING 状态，让相册可见
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    resolver.update(itemUri, values, null, null);
                }

                return Observable.just(itemUri);
            } catch (IOException e) {
                resolver.delete(itemUri, null, null);
                return Observable.error(e);
            }
        });
    }

    /**
     * 根据图片的 Uri 判断其是否为 HDR 图片
     *
     * @param context  上下文
     * @param imageUri 图片的 Uri (例如 content://... 或 file://...)
     * @return true 表示为 HDR 图片（包含 GainMap），false 表示否或系统不支持
     */
    public static Single<Boolean> isHdrImage(Context context, Uri imageUri) {
        return Single.fromCallable(() -> {
            // GainMap API 是在 Android 14 (API 34) 引入的，低于此版本的系统无法原生解析 Ultra HDR 的增益图
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                return false;
            }

            if (imageUri == null) {
                return false;
            }

            InputStream inputStream = null;
            Bitmap bitmap = null;
            try {
                // 打开 Uri 输入流
                inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    return false;
                }

                // 配置解码选项：通过降采样（inSampleSize）减少内存占用
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4; // 缩小为原图的 1/4 解码，足够读取 HDR 元数据/GainMap

                // 解码得到 Bitmap
                bitmap = BitmapFactory.decodeStream(inputStream, null, options);

                // 判断 Bitmap 是否包含 Ultra HDR 的增益图 (Gainmap)
                if (bitmap != null && bitmap.hasGainmap()) {
                    return true;
                }
            } finally {
                // 及时释放资源
                if (bitmap != null) {
                    bitmap.recycle();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            return false;
        });
    }

    /**
     * 读取媒体文件夹中的文件
     *
     * @param context 上下文
     * @return 读取到的媒体文件信息
     */
    @NonNull
    public static List<MediaFileInfo> readMediaDir(Context context) throws IOException {
        List<MediaFileInfo> result = new ArrayList<>();
        File mediaDir = DirectoryPaths.MEDIA.getDir(context);
        if (mediaDir == null || !mediaDir.exists() || !mediaDir.isDirectory()) {
            return result;
        }

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // 读取目录下的图片文件
        File[] childFiles = mediaDir.listFiles((dir, name) -> {
            String extension = FileHelper.getFileExtension(name);
            if (extension == null) {
                return false;
            }
            // 根据扩展名查询 MIME 类型
            String mimeType = mimeTypeMap.getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT));
            // 判断 MIME 类型是否为 image/*
            return mimeType != null && mimeType.startsWith("image/");
        });

        if (childFiles == null) {
            return result;
        }

        // 解析文件信息
        for (File file : childFiles) {
            Path path = file.toPath();

            //获取大小信息
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            long size = attrs.size();

            //获取最后修改时间
            FileTime lastModifiedTime = attrs.lastModifiedTime();
            long creationTimestamp = lastModifiedTime.toMillis();

            String fileName = file.getName();
            Uri uri = Uri.fromFile(file);

            result.add(new MediaFileInfo(uri, size, fileName, creationTimestamp));
        }

        return result;
    }

    /**
     * 获取媒体文件的详细数据
     *
     * @param context 上下文
     * @param uri     媒体文件的 Uri
     * @return 媒体文件详细数据
     */
    @NonNull
    @Contract("_, _ -> new")
    public static MediaDetail getMediaDetail(@NonNull Context context, Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        int wi = 0, hei = 0, iso = 0;
        String device = "";
        double aperture = 0, exp = 0, focalLength = 0;
        boolean flashFired = false;
        LocalDateTime time = null;

        //获取文件大小
        long fileSize = FileHelper.getFileSizeByUri(context, uri);

        //获取文件名
        String fileName = FileHelper.getFileNameByUri(context, uri);

        //读取 Exif 信息
        try (InputStream inputStream = resolver.openInputStream(uri)) {
            if (inputStream != null) {
                ExifInterface exif = new ExifInterface(inputStream);

                // --- 拍摄时间 / 修改时间 ---
                String timeStr = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL); // 优先获取原始拍摄时间
                if (timeStr != null) {
                    time = LocalDateTime.parse(timeStr, CustomDateTimeFormatter.DATE_TIME_EXIF);
                } else {
                    long lastModified = FileHelper.getLastModifyTimeByUri(context, uri);
                    time = DateTimeConverter.toLocalDateTime(lastModified);
                }

                // --- 像素尺寸 ---
                wi = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
                hei = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);

                // 处理部分机型拍摄旋转后的宽高属性
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    int temp = wi;
                    wi = hei;
                    hei = temp;
                }

                // --- 拍摄设备 (品牌 & 型号) ---
                String make = exif.getAttribute(ExifInterface.TAG_MAKE);
                String model = exif.getAttribute(ExifInterface.TAG_MODEL);
                if (model != null) device += model;
                if (make != null) device += (", " + make);

                // --- 光圈大小 ---
                aperture = exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, 0.0);

                // --- 快门速度 ---
                String exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
                if (exposureTime != null) {
                    try {
                        exp = Double.parseDouble(exposureTime);
                    } catch (NumberFormatException e) {
                        exp = -1;
                    }
                } else {
                    exp = -1;
                }

                // --- ISO 值 ---
                iso = exif.getAttributeInt(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, 0);

                // --- 焦距 ---
                focalLength = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0);

                // --- 是否使用闪光灯 ---
                int flash = exif.getAttributeInt(ExifInterface.TAG_FLASH, -1);
                if (flash != -1) {
                    // 比特位 0 表示闪光灯是否触发 (Flash fired)
                    flashFired = (flash & 0x1) != 0;
                }
            }
        }

        return new MediaDetail(
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
        );
    }
}
