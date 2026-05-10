package com.wanderer.journal.helpers.file;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wanderer.journal.enums.DirectoryPaths;
import com.wanderer.journal.enums.LogTags;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class ZipHelper {
    /**
     * 将源文件或目录打包到 ZipOutputStream
     *
     * @param sourceFile    需要添加到压缩包的文件或目录
     * @param fileNameInZip 在压缩包中的路径名称
     * @param zos           ZipOutputStream 实例，用于将文件写入压缩包
     */
    private static void addToZip(@NonNull File sourceFile, String fileNameInZip, ZipOutputStream zos) throws IOException {
        if (sourceFile.isHidden()) return;

        if (sourceFile.isDirectory()) {
            // 如果是目录，确保以 "/" 结尾
            String folderName = fileNameInZip.endsWith("/") ? fileNameInZip : fileNameInZip + "/";
            zos.putNextEntry(new ZipEntry(folderName));
            zos.closeEntry();

            File[] children = sourceFile.listFiles();
            if (children != null) {
                for (File child : children) {
                    // 递归添加子文件，路径累加
                    addToZip(child, folderName + child.getName(), zos);
                }
            }
        } else {
            // 如果是文件，直接写入
            try (FileInputStream fis = new FileInputStream(sourceFile)) {
                ZipEntry zipEntry = new ZipEntry(fileNameInZip);
                zos.putNextEntry(zipEntry);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) >= 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
    }

    /**
     * 创建备份文件压缩包
     *
     * @param tempDir      临时数据目录
     * @param imageDir     媒体目录，若为 null 则不添加媒体目录
     * @param outputStream 由 SAF Uri 转换而来的输出流
     * @throws IOException 文件写入失败引发的异常
     */
    public static void createBackupZip(@NonNull File tempDir, @Nullable File imageDir, OutputStream outputStream) throws IOException {
        // 将 ZipOutputStream 包装在外部传入的流上
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {

            // 1. 打包临时 JSON 目录下的所有文件（不包含目录本身）
            File[] tempFiles = tempDir.listFiles();
            if (tempFiles != null) {
                for (File file : tempFiles) {
                    addToZip(file, file.getName(), zos);
                }
            }

            // 2. 将图片目录整体添加进去
            if (imageDir != null && imageDir.exists() && imageDir.isDirectory()) {
                addToZip(imageDir, imageDir.getName(), zos);
            }
        }
    }

    /**
     * 创建将导出的临时备份文件打包为压缩包的任务
     *
     * @param targetUri 目标压缩包的 Uri
     * @param context   上下文
     * @return 是否成功
     */
    public static Completable zipBackupTask(Uri targetUri, Context context) {
        File tempDir = DirectoryPaths.DATA_TEMP.getDir(context);
        File imageDir = DirectoryPaths.MEDIA.getDir(context);

        return Completable.fromAction(() -> {
            if (tempDir == null) {
                throw new IOException("无法获取临时数据目录");
            }

            // 1. 获取 SAF 的输出流
            OutputStream os = context.getContentResolver().openOutputStream(targetUri);
            if (os == null) {
                throw new IOException("无法打开输出流，请检查权限或路径");
            }

            try {
                // 2. 调用压缩逻辑
                // 现在的逻辑是直接流式写入 SAF 指向的文件，不再需要中间的临时 Zip 文件
                ZipHelper.createBackupZip(tempDir, imageDir, os);
            } finally {
                // 3. 无论成功失败，清理临时 JSON 目录
                FileHelper.clearTempDataDir(context);
            }
        });
    }

    /**
     * 扫描压缩文件并返回所有位于根目录下的文件名
     *
     * @param uri     需要扫描的压缩文件的 Uri
     * @param context 上下文
     * @return 可通过 RxJava 观察的文件名字符串列表
     */
    public static Single<List<String>> scanZipFile(Uri uri, @NonNull Context context) {
        return Single.fromCallable(() -> {
            List<String> fileNameList = new ArrayList<>();
            try (InputStream is = context.getContentResolver().openInputStream(uri);
                 ZipInputStream zis = new ZipInputStream(is)) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    fileNameList.add(entry.getName());
                    zis.closeEntry();
                }
            } catch (IOException e) {
                throw new RuntimeException("压缩包扫描失败", e);
            }

            return fileNameList;
        });
    }

    public static Single<List<File>> unpackWithFilter(Context context, Uri zipUri, File targetDir, List<String> allowedFiles) {
        return Single.fromCallable(() -> {
            List<File> extractedFiles = new ArrayList<>();
            byte[] buffer = new byte[1024 * 8];

            try (InputStream is = context.getContentResolver().openInputStream(zipUri);
                 ZipInputStream zis = new ZipInputStream(is)) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String name = entry.getName();

                    // 1. 检查是否在用户选择的列表中
                    if (!allowedFiles.contains(name)) {
                        zis.closeEntry();
                        continue;
                    }

                    // 2. 确定输出位置
                    File outFile = new File(targetDir, name);
                    if (entry.isDirectory()) {
                        if (!outFile.mkdirs()) {
                            Log.w(LogTags.ZIP_HELPER.n(), "无法创建输出目录");
                        }
                    } else {
                        // 3. 执行解压具体写入操作
                        extractFile(zis, outFile, buffer);
                        extractedFiles.add(outFile);
                    }
                    zis.closeEntry();
                }
            }
            return extractedFiles;
        });
    }

    /**
     * 具体解压写入文件的实现
     */
    private static void extractFile(ZipInputStream zis, @NonNull File outFile, byte[] buffer) throws IOException {
        // 确保父目录存在
        File parent = outFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            Log.e(LogTags.ZIP_HELPER.n(), "父目录不存在且无法创建");
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            // 刷新缓冲区并同步到磁盘
            fos.getFD().sync();
        }
    }
}
