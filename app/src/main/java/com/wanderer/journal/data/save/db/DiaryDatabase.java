package com.wanderer.journal.data.save.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.daos.MediaDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

@Database(
        entities = {
                DiaryEntity.class,
                ParagraphEntity.class,
                MediaEntity.class
        },
        version = 1
)
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
                            .addMigrations()
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    public abstract DiaryDao diaryDao();

    public abstract ParagraphDao paragraphDao();

    public abstract MediaDao mediaDao();
}
