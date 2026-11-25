package com.wanderer.journal.data.save.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

/**
 * 备份功能使用的原始数据库访问接口。
 *
 * 只包含 SQL 访问方法，备份数据的导入导出编排由
 * `com.wanderer.journal.data.backup.DataBackupDao` 负责。
 */
@Dao
interface BackupDao {
    /**
     * 导出日记表。
     */
    @Query("SELECT * FROM diaries")
    fun readDiaryData(): List<DiaryEntity>

    /**
     * 导入日记表。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun writeDiaryData(diaryEntityList: List<DiaryEntity>)

    /**
     * 清空日记表。
     */
    @Query("DELETE FROM diaries")
    fun clearDiaryData()

    /**
     * 导出段落表。
     */
    @Query("SELECT * FROM paragraphs")
    fun readParagraphData(): List<ParagraphEntity>

    /**
     * 清空段落表。
     */
    @Query("DELETE FROM paragraphs")
    fun clearParagraphData()

    /**
     * 导入段落表。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun writeParagraphData(paragraphEntityList: List<ParagraphEntity>)

    /**
     * 导出情绪标签数据。
     */
    @Query("SELECT * FROM emotionTags")
    fun readEmotionData(): List<EmotionTagEntity>

    /**
     * 导出情绪标签与段落对应关系数据。
     */
    @Query("SELECT * FROM emotionParagraphCrossRef")
    fun readEmotionRefData(): List<EmotionParagraphRefEntity>

    /**
     * 清空情绪标签表。
     */
    @Query("DELETE FROM emotionTags")
    fun clearEmotionTag()

    /**
     * 清空情绪标签与段落的关系表。
     */
    @Query("DELETE FROM emotionParagraphCrossRef")
    fun clearEmotionParagraphRef()

    /**
     * 导入情绪标签数据。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun writeEmotionTagData(emotionTagEntityList: List<EmotionTagEntity>)

    /**
     * 导入情绪标签与日记段落关系的数据。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun writeEmotionParagraphRefData(refList: List<EmotionParagraphRefEntity>)

    /**
     * 导出媒体表。
     */
    @Query("SELECT * FROM medias")
    fun readMediaData(): List<MediaEntity>

    /**
     * 清空媒体表。
     */
    @Query("DELETE FROM medias")
    fun clearMediaData()

    /**
     * 导入媒体表。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun writeMediaData(mediaEntityList: List<MediaEntity>)

    /**
     * 读取段落位置信息。
     */
    @Query("SELECT * FROM locations")
    fun readLocationData(): List<LocationEntity>

    /**
     * 清空段落位置信息。
     */
    @Query("DELETE FROM locations")
    fun clearLocationData()

    /**
     * 写入段落位置信息。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun writeLocationData(list: List<LocationEntity>)

    /**
     * 导出角色数据。
     */
    @Query("SELECT * FROM roles")
    fun readRoleData(): List<RoleEntity>

    /**
     * 导出角色别称数据。
     */
    @Query("SELECT * FROM roleAlias")
    fun readRoleAliaData(): List<RoleAliaEntity>

    /**
     * 清空角色表。
     */
    @Query("DELETE FROM roles")
    fun clearRole()

    /**
     * 清空角色别名表。
     */
    @Query("DELETE FROM roleAlias")
    fun clearRoleAlia()

    /**
     * 导入角色数据。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun writeRoleData(roleList: List<RoleEntity>)

    /**
     * 导入角色别名数据。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun writeRoleAliaList(roleAliaEntityList: List<RoleAliaEntity>)

    /**
     * 导出人生笔记数据。
     */
    @Query("SELECT * FROM lifeNotes")
    fun readLifeNoteData(): List<LifeNoteEntity>

    /**
     * 导出人生笔记历史记录数据。
     */
    @Query("SELECT * FROM lifeNoteHistories")
    fun readLifeNoteHistoryData(): List<LifeNoteHistoryEntity>

    /**
     * 清除人生笔记表。
     */
    @Query("DELETE FROM lifeNotes")
    fun clearLifeNote()

    /**
     * 清空人生笔记历史记录表。
     */
    @Query("DELETE FROM lifeNoteHistories")
    fun clearLifeNoteHistory()

    /**
     * 导入人生笔记数据。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun writeLifeNote(list: List<LifeNoteEntity>)

    /**
     * 导入人生笔记历史记录。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun writeLifeNoteHistory(list: List<LifeNoteHistoryEntity>)
}
