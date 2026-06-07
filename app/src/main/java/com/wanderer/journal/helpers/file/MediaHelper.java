package com.wanderer.journal.helpers.file;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
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

import com.wanderer.journal.auxiliary.enums.DirectoryPaths;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.rxjava3.core.Observable;

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
            int screenWidthPx = ViewEdgeHelper.getScreenWidth(webView.getContext());
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
}
