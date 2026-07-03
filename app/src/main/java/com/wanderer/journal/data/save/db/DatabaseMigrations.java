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
    //添加角色使用次数字段
    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE roles ADD COLUMN useCount INTEGER NOT NULL DEFAULT 0");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_roles_useCount` ON `roles` (`useCount`)");
            db.execSQL("ALTER TABLE roles ADD COLUMN displayName TEXT DEFAULT ''");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_roles_displayName` ON `roles` (`displayName`)");
        }
    };

    //添加人生笔记实体及其修改历史表
    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `lifeNotes` (`noteId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `insight` TEXT, `elaboration` TEXT, `dateTime` INTEGER)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lifeNotes_noteId` ON `lifeNotes` (`noteId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lifeNotes_insight` ON `lifeNotes` (`insight`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lifeNotes_elaboration` ON `lifeNotes` (`elaboration`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lifeNotes_dateTime` ON `lifeNotes` (`dateTime`)");
            db.execSQL("CREATE TABLE IF NOT EXISTS `lifeNoteHistories` (`historyId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `noteId` INTEGER NOT NULL, `insight` TEXT, `elaboration` TEXT, `updateDateTime` INTEGER, FOREIGN KEY(`noteId`) REFERENCES `lifeNotes`(`noteId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lifeNoteHistories_historyId` ON `lifeNoteHistories` (`historyId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lifeNoteHistories_noteId` ON `lifeNoteHistories` (`noteId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lifeNoteHistories_updateDateTime` ON `lifeNoteHistories` (`updateDateTime`)");
        }
    };
}
