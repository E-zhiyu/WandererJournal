package com.wanderer.journal.data.backup;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.wanderer.journal.data.backup.maps.DiaryDataMap;
import com.wanderer.journal.data.backup.maps.RoleDataMap;
import com.wanderer.journal.data.backup.pojo.DiaryPojo;
import com.wanderer.journal.data.backup.pojo.EmotionParagraphRefPojo;
import com.wanderer.journal.data.backup.pojo.EmotionTagPojo;
import com.wanderer.journal.data.backup.pojo.MediaPojo;
import com.wanderer.journal.data.backup.pojo.ParagraphPojo;
import com.wanderer.journal.data.backup.pojo.RoleAliaPojo;
import com.wanderer.journal.data.backup.pojo.RolePojo;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.RoleAliaEntity;
import com.wanderer.journal.data.save.db.entities.RoleEntity;

import java.util.List;

@Dao
public interface DataBackupDao {
    /**
     * 导出“日记数据”选项对应的数据
     *
     * @return 日记数据集合
     */
    @Transaction
    default DiaryDataMap exportAllDiaryData() {
        //读取日记数据
        List<DiaryEntity> diaryEntityList = exportDiaryData();
        List<DiaryPojo> diaryPojoList = EntityPojoMapper.INSTANCE.toDiaryPojoList(diaryEntityList);

        //读取段落数据
        List<ParagraphEntity> paragraphEntityList = exportParagraphData();
        List<ParagraphPojo> paragraphPojoList = EntityPojoMapper.INSTANCE.toParagraphPojoList(paragraphEntityList);

        //读取媒体数据
        List<MediaEntity> mediaEntityList = exportMediaData();
        List<MediaPojo> mediaPojoList = EntityPojoMapper.INSTANCE.toMediaPojoList(mediaEntityList);

        //读取情绪标签数据
        List<EmotionTagEntity> emotionTagWithParagraphList = exportEmotionData();
        List<EmotionTagPojo> emotionTagPojoList =
                EntityPojoMapper.INSTANCE.toEmotionTagPojoList(emotionTagWithParagraphList);

        //读取情绪标签与段落关系数据
        List<EmotionParagraphRefEntity> emotionParagraphRefEntityList = exportEmotionRefData();
        List<EmotionParagraphRefPojo> emotionParagraphRefPojoList =
                EntityPojoMapper.INSTANCE.toEmotionParagraphRefPojoList(emotionParagraphRefEntityList);

        //实例化Map类
        DiaryDataMap map = new DiaryDataMap();
        map.setDiaryList(diaryPojoList);
        map.setParagraphList(paragraphPojoList);
        map.setMediaList(mediaPojoList);
        map.setEmotionTagList(emotionTagPojoList);
        map.setEmotionParagraphRefList(emotionParagraphRefPojoList);

        return map;
    }

    /**
     * 导入“日记数据”选项对应的数据
     *
     * @param map 日记数据集合
     */
    @Transaction
    default void importAllDiaryData(@NonNull DiaryDataMap map) {
        //清空旧数据
        clearEmotionParagraphRef();
        clearEmotionTag();
        clearMediaData();
        clearParagraphData();
        clearDiaryData();

        //导入日记数据
        List<DiaryPojo> diaryPojoList = map.getDiaryList();
        if (diaryPojoList != null && !diaryPojoList.isEmpty()) {
            List<DiaryEntity> diaryEntityList = EntityPojoMapper.INSTANCE.toDiaryEntityList(diaryPojoList);
            importDiaryData(diaryEntityList);
        }

        //导入段落数据
        List<ParagraphPojo> paragraphPojoList = map.getParagraphList();
        if (paragraphPojoList != null && !paragraphPojoList.isEmpty()) {
            List<ParagraphEntity> paragraphEntityList = EntityPojoMapper.INSTANCE.toParagraphEntityList(paragraphPojoList);
            importParagraphData(paragraphEntityList);
        }

        //导入媒体数据
        List<MediaPojo> mediaPojoList = map.getMediaList();
        if (mediaPojoList != null && !mediaPojoList.isEmpty()) {
            List<MediaEntity> mediaEntityList = EntityPojoMapper.INSTANCE.toMediaEntityList(mediaPojoList);
            importMediaData(mediaEntityList);
        }

        //导入情绪标签数据
        List<EmotionTagPojo> emotionTagPojoList = map.getEmotionTagList();
        if (emotionTagPojoList != null && !emotionTagPojoList.isEmpty()) {
            List<EmotionTagEntity> emotionTagEntityList =
                    EntityPojoMapper.INSTANCE.toEmotionTagEntityList(emotionTagPojoList);
            importEmotionTagData(emotionTagEntityList);
        }

        //导入情绪标签与段落关系数据
        List<EmotionParagraphRefPojo> emotionParagraphRefPojoList = map.getEmotionParagraphRefList();
        if (emotionParagraphRefPojoList != null && !emotionParagraphRefPojoList.isEmpty()) {
            List<EmotionParagraphRefEntity> emotionParagraphRefEntityList =
                    EntityPojoMapper.INSTANCE.toEmotionParagraphRefEntityList(emotionParagraphRefPojoList);
            importEmotionParagraphRefData(emotionParagraphRefEntityList);
        }
    }

    /**
     * 导出所有角色数据
     *
     * @return 角色数据集合
     */
    @Transaction
    default RoleDataMap exportAllRoleData() {
        //读取角色数据
        List<RoleEntity> roleEntityList = exportRoleData();
        List<RolePojo> rolePojoList = EntityPojoMapper.INSTANCE.toRolePojoList(roleEntityList);

        //读取角色别名数据
        List<RoleAliaEntity> roleAliaEntityList = exportRoleAliaData();
        List<RoleAliaPojo> roleAliaPojoList = EntityPojoMapper.INSTANCE.toRoleAliaPojoList(roleAliaEntityList);

        RoleDataMap map = new RoleDataMap();
        map.setRoleList(rolePojoList);
        map.setRoleAliaList(roleAliaPojoList);
        return map;
    }

    /**
     * 导入所有角色数据
     *
     * @param map 角色数据集合
     */
    @Transaction
    default void importAllRoleData(@NonNull RoleDataMap map) {
        //清空旧数据
        clearRoleAlia();
        clearRole();

        //导入角色数据
        List<RolePojo> rolePojoList = map.getRoleList();
        if (rolePojoList != null && !rolePojoList.isEmpty()) {
            List<RoleEntity> roleEntityList = EntityPojoMapper.INSTANCE.toRoleEntityList(rolePojoList);
            importRoleData(roleEntityList);
        }

        //导入角色别名数据
        List<RoleAliaPojo> roleAliaPojoList = map.getRoleAliaList();
        if (roleAliaPojoList != null && !roleAliaPojoList.isEmpty()) {
            List<RoleAliaEntity> roleAliaEntityList = EntityPojoMapper.INSTANCE.toRoleAliaEntityList(roleAliaPojoList);
            importRoleAliaList(roleAliaEntityList);
        }
    }

    /**
     * 导出日记表
     *
     * @return 日记实体列表
     */
    @Query("SELECT * FROM diaries")
    List<DiaryEntity> exportDiaryData();

    /**
     * 导入日记表
     *
     * @param diaryEntityList 待导入的日记数据列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void importDiaryData(List<DiaryEntity> diaryEntityList);

    /**
     * 清空日记表
     */
    @Query("DELETE FROM diaries")
    void clearDiaryData();

    /**
     * 导出段落表
     *
     * @return 段落实体列表
     */
    @Query("SELECT * FROM paragraphs")
    List<ParagraphEntity> exportParagraphData();

    /**
     * 清空段落表
     */
    @Query("DELETE FROM paragraphs")
    void clearParagraphData();

    /**
     * 导入段落表
     *
     * @param paragraphEntityList 待导入数据的实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void importParagraphData(List<ParagraphEntity> paragraphEntityList);

    /**
     * 导出情绪标签数据
     *
     * @return 情绪标签实体列表
     */
    @Query("SELECT * FROM emotionTags")
    List<EmotionTagEntity> exportEmotionData();

    /**
     * 导出情绪标签与段落对应关系数据
     *
     * @return 映射实体列表
     */
    @Query("SELECT * FROM emotionParagraphCrossRef")
    List<EmotionParagraphRefEntity> exportEmotionRefData();

    /**
     * 清空情绪标签表
     */
    @Query("DELETE FROM emotionTags")
    void clearEmotionTag();

    /**
     * 清空情绪标签与段落的关系表
     */
    @Query("DELETE FROM emotionParagraphCrossRef")
    void clearEmotionParagraphRef();

    /**
     * 导入情绪标签数据
     *
     * @param emotionTagEntityList 情绪标签实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void importEmotionTagData(List<EmotionTagEntity> emotionTagEntityList);

    /**
     * 导入情绪标签与日记段落关系的数据
     *
     * @param refList 关系列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void importEmotionParagraphRefData(List<EmotionParagraphRefEntity> refList);

    /**
     * 导出媒体表
     *
     * @return 媒体实体列表
     */
    @Query("SELECT * FROM medias")
    List<MediaEntity> exportMediaData();

    /**
     * 清空媒体表
     */
    @Query("DELETE FROM medias")
    void clearMediaData();

    /**
     * 导入媒体表
     *
     * @param mediaEntityList 新数据实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void importMediaData(List<MediaEntity> mediaEntityList);

    /**
     * 导出角色数据
     *
     * @return 角色列表
     */
    @Query("SELECT * FROM roles")
    List<RoleEntity> exportRoleData();

    /**
     * 导出角色别称数据
     *
     * @return 角色别称列表
     */
    @Query("SELECT * FROM roleAlias")
    List<RoleAliaEntity> exportRoleAliaData();

    /**
     * 清空角色表
     */
    @Query("DELETE FROM roles")
    void clearRole();

    /**
     * 清空角色别名表
     */
    @Query("DELETE FROM roleAlias")
    void clearRoleAlia();

    /**
     * 导入角色数据
     *
     * @param roleList 角色列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void importRoleData(List<RoleEntity> roleList);

    /**
     * 导入角色别名数据
     *
     * @param roleAliaEntityList 角色别名列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void importRoleAliaList(List<RoleAliaEntity> roleAliaEntityList);
}
