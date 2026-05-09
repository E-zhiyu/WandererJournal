package com.wanderer.journal.helpers.file;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
}
