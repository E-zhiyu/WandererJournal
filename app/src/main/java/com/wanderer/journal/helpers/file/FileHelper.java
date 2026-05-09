package com.wanderer.journal.helpers.file;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.wanderer.journal.enums.DirectoryPaths;
import com.wanderer.journal.enums.LogTags;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileHelper {
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
