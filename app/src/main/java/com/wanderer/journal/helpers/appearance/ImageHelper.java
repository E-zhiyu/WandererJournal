package com.wanderer.journal.helpers.appearance;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wanderer.journal.auxiliary.enums.DirectoryPaths;
import com.wanderer.journal.auxiliary.enums.LogTags;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageHelper {
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
            Log.e(LogTags.IMAGE_HELPER.n(), "无法创建图片");
            return null;
        }
    }
}
