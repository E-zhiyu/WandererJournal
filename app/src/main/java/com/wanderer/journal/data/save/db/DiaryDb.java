package com.wanderer.journal.data.save.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.wanderer.journal.data.backup.DataBackupDao;
import com.wanderer.journal.data.save.db.converters.DateTimeConverter;
import com.wanderer.journal.data.save.db.converters.UriConverter;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.daos.LifeNoteDao;
import com.wanderer.journal.data.save.db.daos.MediaDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.daos.RoleDao;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.data.save.db.entities.LifeNoteEntity;
import com.wanderer.journal.data.save.db.entities.LifeNoteHistoryEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.RoleAliaEntity;
import com.wanderer.journal.data.save.db.entities.RoleEntity;

@Database(
        entities = {
                DiaryEntity.class,
                ParagraphEntity.class,
                MediaEntity.class,
                EmotionTagEntity.class,
                EmotionParagraphRefEntity.class,
                RoleEntity.class,
                RoleAliaEntity.class,
                LifeNoteEntity.class,
                LifeNoteHistoryEntity.class
        },
        version = 8
)
@TypeConverters({
        DateTimeConverter.class,
        UriConverter.class
})
public abstract class DiaryDb extends RoomDatabase {
    private static volatile DiaryDb INSTANCE; //单例实例

    /**
     * 获取数据库实例
     *
     * @param context 上下文
     * @return 数据库实例
     */
    public static DiaryDb getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DiaryDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    DiaryDb.class,
                                    "diary_database"
                            )
                            .addMigrations(
                                    DatabaseMigrations.MIGRATION_1_2,
                                    DatabaseMigrations.MIGRATION_2_3,
                                    DatabaseMigrations.MIGRATION_3_4,
                                    DatabaseMigrations.MIGRATION_4_5,
                                    DatabaseMigrations.MIGRATION_5_6,
                                    DatabaseMigrations.MIGRATION_6_7,
                                    DatabaseMigrations.MIGRATION_7_8
                            )
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    public abstract DiaryDao diaryDao();

    public abstract ParagraphDao paragraphDao();

    public abstract MediaDao mediaDao();

    public abstract EmotionTagDao emotionTagDao();

    public abstract RoleDao roleDao();

    public abstract DataBackupDao dataBackupDao();
    public abstract LifeNoteDao lifeNoteDao();
}
