package com.wanderer.journal.data.save.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DatabaseMigrations {
    //情绪标签添加分类字段
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE emotionTags ADD COLUMN type INTEGER NOT NULL DEFAULT 1");
        }
    };

    //段落表添加段落 ID 的索引
    public static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_paragraphs_paragraphId` ON `paragraphs` (`paragraphId`)");
        }
    };

    //媒体表添加媒体 ID 的索引
    public static final Migration MIGRATION_3_4 = new Migration(3,4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_medias_mediaId` ON `medias` (`mediaId`)");
        }
    };
}
