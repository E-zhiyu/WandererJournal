package com.wanderer.journal.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.data.save.db.entities.LifeNoteEntity;
import com.wanderer.journal.data.save.db.entities.LifeNoteHistoryEntity;
import com.wanderer.journal.data.save.db.entities.LocationEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.RoleAliaEntity;
import com.wanderer.journal.data.save.db.entities.RoleEntity;

import java.util.List;

/**
 * 备份功能使用的原始数据库访问接口。
 *
 * <p>项目当前使用 javac 的 Room annotationProcessor，而不是 KSP/KAPT，因此 Room 无法从
 * Kotlin 源码生成 DAO 实现。这里保留一个只包含 SQL 访问方法的 Java DAO，
 * 由 Kotlin 的 {@code com.wanderer.journal.data.backup.DataBackupDao} 负责事务和 POJO 转换等编排逻辑。</p>
 */
@Dao
public interface BackupDao {
    /**
     * 导出日记表。
     */
    @Query("SELECT * FROM diaries")
    List<DiaryEntity> readDiaryData();

    /**
     * 导入日记表。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeDiaryData(List<DiaryEntity> diaryEntityList);

    /**
     * 清空日记表。
     */
    @Query("DELETE FROM diaries")
    void clearDiaryData();

    /**
     * 导出段落表。
     */
    @Query("SELECT * FROM paragraphs")
    List<ParagraphEntity> readParagraphData();

    /**
     * 清空段落表。
     */
    @Query("DELETE FROM paragraphs")
    void clearParagraphData();

    /**
     * 导入段落表。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeParagraphData(List<ParagraphEntity> paragraphEntityList);

    /**
     * 导出情绪标签数据。
     */
    @Query("SELECT * FROM emotionTags")
    List<EmotionTagEntity> readEmotionData();

    /**
     * 导出情绪标签与段落对应关系数据。
     */
    @Query("SELECT * FROM emotionParagraphCrossRef")
    List<EmotionParagraphRefEntity> readEmotionRefData();

    /**
     * 清空情绪标签表。
     */
    @Query("DELETE FROM emotionTags")
    void clearEmotionTag();

    /**
     * 清空情绪标签与段落的关系表。
     */
    @Query("DELETE FROM emotionParagraphCrossRef")
    void clearEmotionParagraphRef();

    /**
     * 导入情绪标签数据。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeEmotionTagData(List<EmotionTagEntity> emotionTagEntityList);

    /**
     * 导入情绪标签与日记段落关系的数据。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeEmotionParagraphRefData(List<EmotionParagraphRefEntity> refList);

    /**
     * 导出媒体表。
     */
    @Query("SELECT * FROM medias")
    List<MediaEntity> readMediaData();

    /**
     * 清空媒体表。
     */
    @Query("DELETE FROM medias")
    void clearMediaData();

    /**
     * 导入媒体表。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeMediaData(List<MediaEntity> mediaEntityList);

    /**
     * 读取段落位置信息。
     */
    @Query("SELECT * FROM locations")
    List<LocationEntity> readLocationData();

    /**
     * 清空段落位置信息。
     */
    @Query("DELETE FROM locations")
    void clearLocationData();

    /**
     * 写入段落位置信息。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void writeLocationData(List<LocationEntity> list);

    /**
     * 导出角色数据。
     */
    @Query("SELECT * FROM roles")
    List<RoleEntity> readRoleData();

    /**
     * 导出角色别称数据。
     */
    @Query("SELECT * FROM roleAlias")
    List<RoleAliaEntity> readRoleAliaData();

    /**
     * 清空角色表。
     */
    @Query("DELETE FROM roles")
    void clearRole();

    /**
     * 清空角色别名表。
     */
    @Query("DELETE FROM roleAlias")
    void clearRoleAlia();

    /**
     * 导入角色数据。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void writeRoleData(List<RoleEntity> roleList);

    /**
     * 导入角色别名数据。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void writeRoleAliaList(List<RoleAliaEntity> roleAliaEntityList);

    /**
     * 导出人生笔记数据。
     */
    @Query("SELECT * FROM lifeNotes")
    List<LifeNoteEntity> readLifeNoteData();

    /**
     * 导出人生笔记历史记录数据。
     */
    @Query("SELECT * FROM lifeNoteHistories")
    List<LifeNoteHistoryEntity> readLifeNoteHistoryData();

    /**
     * 清除人生笔记表。
     */
    @Query("DELETE FROM lifeNotes")
    void clearLifeNote();

    /**
     * 清空人生笔记历史记录表。
     */
    @Query("DELETE FROM lifeNoteHistories")
    void clearLifeNoteHistory();

    /**
     * 导入人生笔记数据。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void writeLifeNote(List<LifeNoteEntity> list);

    /**
     * 导入人生笔记历史记录。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void writeLifeNoteHistory(List<LifeNoteHistoryEntity> list);
}
