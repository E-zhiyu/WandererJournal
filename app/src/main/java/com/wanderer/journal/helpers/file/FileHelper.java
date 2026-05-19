package com.wanderer.journal.helpers.file;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.wanderer.journal.enums.DirectoryPaths;
import com.wanderer.journal.enums.LogTags;
import com.wanderer.journal.helpers.classes.TextFileData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import io.reactivex.rxjava3.core.Single;

public class FileHelper {
    /**
     * 通过多线程读取文件内容以及最后编辑时间
     *
     * @param uri     待读取的文件的 Uri
     * @param context 上下文
     * @return 文件数据实例
     */
    public static Single<TextFileData> readContentWithLastModifyTime(Uri uri, Context context) {
        return Single.fromCallable(() -> {
            String content = readContent(uri, context);
            LocalDateTime lastModifyTime = getFileLastModifyTime(uri, context);
            return new TextFileData(content, lastModifyTime);
        });
    }

    /**
     * 获取文件最后的编辑时间
     *
     * @param uri     需要获取创建时间的文件的 Uri
     * @param context 上下文
     * @return 文件最后编辑时间的时间戳
     */
    public static LocalDateTime getFileLastModifyTime(Uri uri, Context context) {
        long lastModified = 0;

        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if (documentFile.exists()) {
            lastModified = documentFile.lastModified();
        }

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
    }

    /**
     * 获取文本文件的总行数
     *
     * @param uri     待获取行数的文件 Uri
     * @param context 上下文
     * @return 总行数
     * @throws IOException 文件读取失败引发的异常
     */
    public static int getLines(Uri uri, Context context) throws IOException {
        int lineCount = 0;
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while (reader.readLine() != null) {
                lineCount++;
            }
        }

        return lineCount;
    }

    /**
     * 读取文本文件的内容
     *
     * @param uri     待读取的文本文件的 Uri
     * @param context 上下文
     * @return 文本文件的内容字符串
     * @throws IOException 文件内容读取失败引发的异常
     */
    @NonNull
    public static String readContent(Uri uri, Context context) throws IOException {
        StringBuilder builder = new StringBuilder();

        try (InputStream is = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    /**
     * 读取文本文件内容
     *
     * @param file 待读取的文本文件
     * @return 文件内容字符串
     * @throws IOException 文件内容读取失败引发的异常
     */
    @NonNull
    public static String readContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }

        return content.toString();
    }

    /**
     * 将字符串写入到临时数据文件中
     *
     * @param context  上下文
     * @param fileName 临时数据文件名称
     * @param content  文件内容
     * @throws IOException 写入失败引发的异常
     */
    public static void writeContentToTempDataFile(Context context, String fileName, String content) throws IOException {
        File tempDataDir = DirectoryPaths.DATA_TEMP.getDir(context);
        createFileAndWrite(tempDataDir, fileName, content);
    }

    /**
     * 创建文件并写入指定的字符串
     *
     * @param dir      文件所在目录
     * @param fileName 文件名称
     * @param content  文件内容
     * @throws IOException 无法将字符串写入文件时引发的异常
     */
    public static void createFileAndWrite(File dir, String fileName, String content) throws IOException {
        File targetFile = new File(dir, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            writer.write(content);
        }
    }

    /**
     * 清空临时数据文件目录
     *
     * @param context 上下文
     */
    public static void clearTempDataDir(Context context) {
        File tempDataDir = DirectoryPaths.DATA_TEMP.getDir(context);
        clearDir(tempDataDir);
    }

    /**
     * 清空媒体文件夹
     *
     * @param context 上下文
     */
    public static void clearMediaDir(Context context) {
        File mediaDir = DirectoryPaths.MEDIA.getDir(context);
        clearDir(mediaDir);
    }

    /**
     * 清理某个目录
     *
     * @param dir 需要被清理的目录
     */
    public static void clearDir(@Nullable File dir) {
        //判断目录是否可用
        Log.i(LogTags.DATA_IO_HELPER.n(), "开始清除临时文件……");
        if (dir == null || !dir.isDirectory() || !dir.exists()) {
            Log.e(LogTags.DATA_IO_HELPER.n(), "非目录对象或目录不存在");
            return;
        }

        //获取文件列表
        File[] files = dir.listFiles();
        if (files == null) {
            Log.e(LogTags.DATA_IO_HELPER.n(), "无法获取文件列表");
            return;
        }

        //遍历删除列表中的文件
        for (File file : files) {
            if (!file.delete()) {
                Log.w(LogTags.DATA_IO_HELPER.n(), "无法删除文件：" + file.getName());
            }
        }

        Log.i(LogTags.DATA_IO_HELPER.n(), "文件清理完毕");
    }
}