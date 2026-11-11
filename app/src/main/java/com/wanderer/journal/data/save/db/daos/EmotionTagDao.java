package com.wanderer.journal.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.wanderer.journal.auxiliary.classes.statistics.ParagraphEmotionTagStatistics;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.data.save.db.entities.composite.union.UsedEmotionTagUnionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * 根据段落编号删除段落与情绪标签的映射关系
     *
     * @param paragraphId 段落编号
     */
    @Query("DELETE FROM emotionParagraphCrossRef WHERE paragraphId = :paragraphId")
    void deleteEmotionTagRefByParagraphId(long paragraphId);

    /**
     * 插入段落与情绪标签的映射关系
     *
     * @param refList 待插入的映射关系
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertEmotionTagRef(List<EmotionParagraphRefEntity> refList);

    /**
     * 更新段落与情绪标签的映射关系
     *
     * @param tagSelectMap 情绪标签的选择状况
     * @param paragraphId  段落编号
     */
    @Transaction
    default void updateParagraphEmotionTagRef(Map<Long, Integer> tagSelectMap, long paragraphId) {
        deleteEmotionTagRefByParagraphId(paragraphId);
        if (tagSelectMap == null || tagSelectMap.isEmpty()) return;

        List<EmotionParagraphRefEntity> refList = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : tagSelectMap.entrySet()) {
            refList.add(new EmotionParagraphRefEntity(entry.getKey(), paragraphId, entry.getValue()));
        }
        insertEmotionTagRef(refList);
    }

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
    @Query("SELECT * FROM emotionTags ORDER BY type")
    Flowable<List<EmotionTagEntity>> getEmotionTagFlowable();

    /**
     * 获取所有情绪标签并带有使用次数
     *
     * @return 所有情绪标签组成的列表
     */
    @Query("SELECT e.*, " +
            "(SELECT COUNT(*) FROM emotionParagraphCrossRef ref WHERE e.emotionId = ref.emotionId) AS useCount " +
            "FROM emotionTags e " +
            "ORDER BY type")
    Flowable<List<UsedEmotionTagUnionModel>> getEmotionTagUseCountModelFlowable();

    /**
     * 获取使用过的情绪标签及其使用次数
     *
     * @return 包含情绪标签和使用次数的复合实体列表
     */
    @Query("SELECT e.*, " +
            "(SELECT COUNT(*) FROM emotionParagraphCrossRef ref WHERE e.emotionId = ref.emotionId) AS useCount " +
            "FROM emotionTags e " +
            "WHERE useCount > 0 " +
            "ORDER BY useCount DESC")
    Flowable<List<UsedEmotionTagUnionModel>> getUsedEmotionTagFlowable();

    /**
     * 获取一个段落与情绪标签映射表的统计数据
     */
    @Query("WITH perParagraph AS (" +
            "    SELECT" +
            "        paragraphId," +
            "        COUNT(*)     AS tagCountInParagraph," +
            "        SUM(degree)  AS degreeSumInParagraph" +
            "    FROM emotionParagraphCrossRef" +
            "    GROUP BY paragraphId" +
            ")," +
            "avgCalc AS (" +
            "    SELECT AVG(degreeSumInParagraph) AS avgDegree FROM perParagraph" +
            ")" +
            "SELECT" +
            "    MAX(tagCountInParagraph) AS maxTagCountInOneParagraph," +
            "    AVG(tagCountInParagraph) AS avgTagCountInOneParagraph," +
            "    MAX(degreeSumInParagraph) AS maxEmotionDegreeInOneParagraph," +
            "    AVG(degreeSumInParagraph) AS avgEmotionDegreeInOneParagraph," +
            "    (SELECT COUNT(*) FROM perParagraph, avgCalc" +
            "     WHERE degreeSumInParagraph > avgCalc.avgDegree)" +
            "        AS strongEmotionDegreeParagraphCount " +
            "FROM perParagraph;")
    Flowable<ParagraphEmotionTagStatistics> getParagraphEmotionTagStatisticsFlowable();

    /**
     * 通过 ID 获取情绪标签
     *
     * @param id 需要获取的情绪标签的 ID
     * @return 情绪标签数据实体
     */
    @Query("SELECT * FROM emotionTags WHERE emotionId = :id")
    Single<Optional<EmotionTagEntity>> getEmotionTagOptionalSingleById(long id);

    /**
     * 删除单个情绪标签
     *
     * @param emotionTag 需要被删除的情绪标签
     * @return 是否完成
     */
    @Delete
    Completable deleteEmotionTagCompletable(EmotionTagEntity emotionTag);
}
