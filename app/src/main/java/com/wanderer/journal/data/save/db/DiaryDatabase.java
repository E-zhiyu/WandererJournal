package com.wanderer.journal.data.save.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.wanderer.journal.data.save.db.converters.DateTimeConverter;
import com.wanderer.journal.data.save.db.converters.UriConverter;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.daos.MediaDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.daos.RoleDao;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
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
                RoleAliaEntity.class
        },
        version = 5
)
@TypeConverters({
        DateTimeConverter.class,
        UriConverter.class
})
public abstract class DiaryDatabase extends RoomDatabase {
    private static volatile DiaryDatabase INSTANCE; //单例实例

    /**
     * 获取数据库实例
     *
     * @param context 上下文
     * @return 数据库实例
     */
    public static DiaryDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DiaryDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    DiaryDatabase.class,
                                    "diary_database"
                            )
                            .addMigrations(
                                    DatabaseMigrations.MIGRATION_1_2,
                                    DatabaseMigrations.MIGRATION_2_3,
                                    DatabaseMigrations.MIGRATION_3_4,
                                    DatabaseMigrations.MIGRATION_4_5
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
}
