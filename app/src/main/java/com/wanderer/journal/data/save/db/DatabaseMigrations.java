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
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_paragraphs_paragraphId` ON `paragraphs` (`paragraphId`)");
        }
    };

    //媒体表添加媒体 ID 的索引
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_medias_mediaId` ON `medias` (`mediaId`)");
        }
    };

    //添加角色表和角色别名表
    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `roles` (`roleId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `identity` TEXT, `impression` TEXT, `relationship` INTEGER NOT NULL DEFAULT 2)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_roles_roleId` ON `roles` (`roleId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_roles_name` ON `roles` (`name`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_roles_relationship` ON `roles` (`relationship`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_roles_identity` ON `roles` (`identity`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_roles_impression` ON `roles` (`impression`)");
            db.execSQL("CREATE TABLE IF NOT EXISTS `roleAlias` (`aliaId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `roleId` INTEGER NOT NULL, `alia` TEXT, FOREIGN KEY(`roleId`) REFERENCES `roles`(`roleId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_roleAlias_roleId` ON `roleAlias` (`roleId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_roleAlias_alia` ON `roleAlias` (`alia`)");
        }
    };

    //添加角色显示名称字段
    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE roles ADD COLUMN displayName TEXT DEFAULT ''");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_roles_displayName` ON `roles` (`displayName`)");
        }
    };
}
