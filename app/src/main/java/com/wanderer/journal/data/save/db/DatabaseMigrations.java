package com.wanderer.journal.data.save.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DatabaseMigrations {
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE emotionTags ADD COLUMN type INTEGER NOT NULL DEFAULT 1");
        }
    };
}
