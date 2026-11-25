package com.wanderer.journal.data.save.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wanderer.journal.data.backup.DataBackupDao
import com.wanderer.journal.data.save.db.daos.BackupDao
import com.wanderer.journal.data.save.db.daos.DiaryDao
import com.wanderer.journal.data.save.db.daos.EmotionTagDao
import com.wanderer.journal.data.save.db.daos.LifeNoteDao
import com.wanderer.journal.data.save.db.daos.MediaDao
import com.wanderer.journal.data.save.db.daos.ParagraphDao
import com.wanderer.journal.data.save.db.daos.RoleDao
import com.wanderer.journal.data.save.db.entities.DiaryEntity
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity
import com.wanderer.journal.data.save.db.entities.LifeNoteEntity
import com.wanderer.journal.data.save.db.entities.LifeNoteHistoryEntity
import com.wanderer.journal.data.save.db.entities.LocationEntity
import com.wanderer.journal.data.save.db.entities.MediaEntity
import com.wanderer.journal.data.save.db.entities.ParagraphEntity
import com.wanderer.journal.data.save.db.entities.RoleAliaEntity
import com.wanderer.journal.data.save.db.entities.RoleEntity

@Database(
    entities = [
        DiaryEntity::class,
        ParagraphEntity::class,
        LocationEntity::class,
        MediaEntity::class,
        EmotionTagEntity::class,
        EmotionParagraphRefEntity::class,
        RoleEntity::class,
        RoleAliaEntity::class,
        LifeNoteEntity::class,
        LifeNoteHistoryEntity::class,
    ],
    version = 11,
)
@TypeConverters(
    TypeConverter::class
)
abstract class DiaryDb : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao

    abstract fun paragraphDao(): ParagraphDao

    abstract fun mediaDao(): MediaDao

    abstract fun emotionTagDao(): EmotionTagDao

    abstract fun roleDao(): RoleDao

    /**
     * 备份功能使用的原始数据库访问接口。
     */
    abstract fun backupDao(): BackupDao

    /**
     * 获取备份数据的导入导出器。
     */
    fun dataBackupDao(): DataBackupDao = DataBackupDao(this)

    abstract fun lifeNoteDao(): LifeNoteDao

    companion object {
        @Volatile
        private var INSTANCE: DiaryDb? = null

        /**
         * 获取数据库实例。
         *
         * @param context 上下文
         * @return 数据库实例
         */
        @JvmStatic
        fun getInstance(context: Context): DiaryDb {
            return INSTANCE ?: synchronized(DiaryDb::class.java) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDb::class.java,
                    "diary_database",
                )
                    .addMigrations(
                        DatabaseMigrations.MIGRATION_1_2,
                        DatabaseMigrations.MIGRATION_2_3,
                        DatabaseMigrations.MIGRATION_3_4,
                        DatabaseMigrations.MIGRATION_4_5,
                        DatabaseMigrations.MIGRATION_5_6,
                        DatabaseMigrations.MIGRATION_6_7,
                        DatabaseMigrations.MIGRATION_7_8,
                        DatabaseMigrations.MIGRATION_8_9,
                        DatabaseMigrations.MIGRATION_9_10,
                        DatabaseMigrations.MIGRATION_10_11,
                    )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
