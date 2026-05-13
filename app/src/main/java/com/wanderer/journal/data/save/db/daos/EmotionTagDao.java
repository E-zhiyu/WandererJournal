package com.wanderer.journal.data.save.db.daos;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.data.save.db.entities.composite.EmotionTagWithParagraph;
import com.wanderer.journal.data.save.db.ref.EmotionParagraphCrossRef;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface EmotionTagDao {
    /**
     * 获取情绪标签数量，支持响应式更新
     *
     * @return 情绪标签的数量
     */
    @Query("SELECT COUNT(*) FROM emotionTags")
    Flowable<Integer> getEmotionTagCount();

    /**
     * 数据导出方法
     *
     * @return 带有段落 ID 列表的情绪标签实体列表
     */
    @Query("SELECT * FROM emotionParagraphCrossRef")
    List<EmotionTagWithParagraph> exportData();

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
     * 清空两个表的数据，准备插入新数据
     */
    @Transaction
    default void clear() {
        clearEmotionParagraphRef();
        clearEmotionTag();
    }

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
    void importEmotionParagraphRefData(List<EmotionParagraphCrossRef> refList);

    /**
     * 导入数据的事务
     *
     * @param dataList 数据列表
     */
    @Transaction
    default void importData(@NonNull List<EmotionTagWithParagraph> dataList) {
        //获取情绪标签实体列表
        List<EmotionTagEntity> emotionTagEntityList = dataList.stream()
                .map(EmotionTagWithParagraph::getEmotionTag)
                .collect(Collectors.toList());

        //获取关系列表
        List<EmotionParagraphCrossRef> refList = dataList.stream()
                .map(emotionTagWithParagraph -> {
                    long emotionId = emotionTagWithParagraph.getEmotionTag().getEmotionId();
                    return emotionTagWithParagraph.getParagraphIdList().stream()
                            .map(l -> new EmotionParagraphCrossRef(emotionId, l))
                            .collect(Collectors.toList());
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        //插入数据库
        importEmotionTagData(emotionTagEntityList);
        importEmotionParagraphRefData(refList);
    }
}
