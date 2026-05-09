package com.wanderer.journal.enums;

import android.content.Context;

import com.wanderer.journal.helpers.file.backup.BackupHelperBase;
import com.wanderer.journal.helpers.file.backup.DiaryBackupHelper;

import java.util.function.Function;

/**
 * 导入导出的数据类型
 */
public enum DataType {
    DIARY(
            "日记数据",
            "diary.json",
            DiaryBackupHelper::new
    );
    private final String title;
    private final String fileName;
    private final Function<Context, BackupHelperBase<?, ?>> helperFactory;

    DataType(String title, String fileName, Function<Context, BackupHelperBase<?, ?>> helperFactory) {
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
}
