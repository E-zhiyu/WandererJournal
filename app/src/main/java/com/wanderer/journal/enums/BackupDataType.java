package com.wanderer.journal.enums;

import android.content.Context;

import androidx.annotation.Nullable;

import com.wanderer.journal.helpers.file.backup.BackupHelperBase;
import com.wanderer.journal.helpers.file.backup.DiaryBackupHelper;

import java.util.function.Function;

/**
 * 备份的数据类型
 */
public enum BackupDataType {
    DIARY(
            "日记数据",
            "diary.json",
            DiaryBackupHelper::new
    );
    private final String title;
    private final String fileName;
    private final Function<Context, BackupHelperBase<?, ?>> helperFactory;

    BackupDataType(String title, String fileName, Function<Context, BackupHelperBase<?, ?>> helperFactory) {
        this.title = title;
        this.fileName = fileName;
        this.helperFactory = helperFactory;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * 创建备份帮助器
     *
     * @param context 上下文
     * @return 备份帮助器实例
     */
    public BackupHelperBase<?, ?> createBackupHelper(Context context) {
        return helperFactory.apply(context);
    }

    /**
     * 根据文件名称判断数据种类
     * @param fileName 文件名称
     * @return 数据类型，若无法匹配类型则返回 null
     */
    @Nullable
    public static BackupDataType fromFileName(String fileName) {
        for (BackupDataType type : values()) {
            if (type.getFileName().equals(fileName)) {
                return type;
            }
        }
        return null; // 或者返回一个表示“未知”的类型
    }
}
