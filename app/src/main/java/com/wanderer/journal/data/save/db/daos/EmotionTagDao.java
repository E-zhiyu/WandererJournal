package com.wanderer.journal.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.composite.ui.EmotionTagUiModel;

import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface EmotionTagDao {
    /**
     * 插入新情绪标签
     *
     * @param emotionTag 新情绪标签实例
     * @return 是否完成
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insertEmotionTagCompletable(EmotionTagEntity emotionTag);

    /**
     * 插入与段落的关系
     *
     * @param ref 关系实体
     * @return 是否完成
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insertEmotionParagraphRefCompletable(EmotionParagraphRefEntity ref);

    /**
     * 修改情绪标签
     *
     * @param emotionTag 修改后的情绪标签
     * @return 是否完成
     */
    @Update
    Completable updateEmotionTagCompletable(EmotionTagEntity emotionTag);

    /**
     * 更新情绪标签与段落的关系
     *
     * @param ref 更新后的关系
     * @return 是否完成
     */
    @Update
    Completable updateEmotionParagraphRefCompletable(EmotionParagraphRefEntity ref);

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
     * 获取可以选择的情绪标签数据
     *
     * @param paragraphId 正在编辑情绪标签的段落 ID
     * @return 可供选择的情绪标签 Item 列表
     */
    @Query(
            "SELECT " +
                    "    e.*, " +
                    "    (r.paragraphId IS NOT NULL) AS isChecked, " +
                    "    COALESCE(r.degree, 1) AS degree " +
                    "FROM emotionTags e " +
                    "LEFT JOIN emotionParagraphCrossRef r " +
                    "    ON e.emotionId = r.emotionId AND r.paragraphId = :paragraphId "
    )
    Flowable<List<EmotionTagUiModel>> getSelectableEmotionTagFlowable(long paragraphId);

    /**
     * 获取在 ID 列表中的情绪标签
     *
     * @param emotionIdList 需要获取的标签对应的 ID 列表
     * @return ID 在列表中的情绪标签实例
     */
    @Query("SELECT * FROM emotionTags WHERE emotionId IN (:emotionIdList)")
    Single<List<EmotionTagEntity>> getEmotionTagSingleByIdList(Set<Long> emotionIdList);

    /**
     * 查询某个情绪标签绑定的段落数量
     *
     * @param emotionTagId 情绪标签的 ID
     * @return 包含绑定段落数量的{@link Single}对象
     */
    @Query("SELECT COUNT(*) FROM emotionParagraphCrossRef WHERE emotionId == :emotionTagId")
    Single<Integer> getParagraphCountSingleByEmotionTagId(long emotionTagId);

    /**
     * 删除单个情绪标签
     *
     * @param emotionTag 需要被删除的情绪标签
     * @return 是否完成
     */
    @Delete
    Completable deleteEmotionTagCompletable(EmotionTagEntity emotionTag);

    /**
     * 删除情绪标签与段落的关系
     *
     * @param ref 关系实体
     * @return 是否完成
     */
    @Delete
    Completable deleteEmotionParagraphRefCompletable(EmotionParagraphRefEntity ref);
}
