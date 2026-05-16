package com.wanderer.journal.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface EmotionTagDao {
    /**
     * 插入新情绪标签
     *
     * @param emotionTag 新情绪标签实例
     * @return 是否完成
     */
    @Insert
    Completable insertEmotionTagCompletable(EmotionTagEntity emotionTag);

    /**
     * 修改情绪标签
     *
     * @param emotionTag 修改后的情绪标签
     * @return 是否完成
     */
    @Update
    Completable updateEmotionTagCompletable(EmotionTagEntity emotionTag);

    /**
     * 获取情绪标签数量，支持响应式更新
     *
     * @return 情绪标签的数量
     */
    @Query("SELECT COUNT(*) FROM emotionTags")
    Flowable<Integer> getEmotionTagCountFlowable();

    /**
     * 获取所有情绪标签并支持响应式更新
     *
     * @return 所有情绪标签组成的列表
     */
    @Query("SELECT * FROM emotionTags")
    Flowable<List<EmotionTagEntity>> getAllEmotionTagFlowable();

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
     * 删除单个情绪标签
     *
     * @param emotionTag 需要被删除的情绪标签
     * @return 是否完成
     */
    @Delete
    Completable deleteEmotionTagCompletable(EmotionTagEntity emotionTag);

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
}
