package com.wanderer.journal.auxiliary.enums.types;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wanderer.journal.data.backup.helpers.BackupHelperBase;
import com.wanderer.journal.data.backup.helpers.DiaryBackupHelper;
import com.wanderer.journal.data.backup.helpers.LifeNoteBackupHelper;
import com.wanderer.journal.data.backup.helpers.RoleBackupHelper;

import org.jetbrains.annotations.Contract;

import java.util.function.Function;

/**
 * 备份的数据类型
 */
public enum BackupDataType {
    DIARY(
            "日记数据",
            DiaryBackupHelper::new,
            "diary.json"
    ),
    ROLE(
            "角色数据",
            RoleBackupHelper::new,
            "role.json"
    ),
    LIFE_NOTE(
            "人生笔记数据",
            LifeNoteBackupHelper::new,
            "life_note.json"
    );
    private final String title;
    private final Function<Context, BackupHelperBase<?, ?>> helperFactory;
    private final String[] fileNames;

    BackupDataType(String title, Function<Context, BackupHelperBase<?, ?>> helperFactory,String... fileNames) {
        this.title = title;
        this.helperFactory = helperFactory;
        this.fileNames = fileNames;
    }

    public String getTitle() {
        return title;
    }

    /**
     * 获取最新版本的备份文件名称
     *
     * @return 最新版本的备份文件名称
     */
    @Nullable
    @Contract(pure = true)
    public String getLatestVersionFileName() {
        if (fileNames.length > 0) {
            return fileNames[fileNames.length - 1];
        } else {
            return null;
        }
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
     *
     * @param fileName 文件名称
     * @return 数据类型，若无法匹配类型则返回 null
     */
    @Nullable
    public static BackupDataType fromFileName(String fileName, @NonNull ImportDataVersionType versionType) {
        int ordinal = versionType.ordinal();
        for (BackupDataType type : values()) {
            String effectiveFileName = type.fileNames[ordinal];
            if (effectiveFileName != null && effectiveFileName.equals(fileName)) {
                return type;
            }
        }
        return null;
    }
}
