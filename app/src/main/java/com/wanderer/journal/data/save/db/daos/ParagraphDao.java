package com.wanderer.journal.data.save.db.daos;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.composite.ParagraphEntityModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface ParagraphDao {
    /**
     * 获取段落总数
     * @return 段落总数
     */
    @Query("SELECT COUNT(*) FROM paragraphs")
    Flowable<Integer> getParagraphCountFlowable();

    /**
     * 读取所有段落并支持局部加载
     *
     * @return 可局部加载的日记段落列表
     */
    @Transaction
    @Query("SELECT * FROM paragraphs ORDER BY createTime")
    PagingSource<Integer, ParagraphEntityModel> getAllParagraphPagingSource();

    /**
     * 查询指定 ID 的段落
     *
     * @param paragraphIds 需要查询的段落的 ID
     * @return ID 包含在数组中的段落
     */
    @Transaction
    @Query("SELECT * FROM paragraphs WHERE paragraphId IN (:paragraphIds)")
    Single<List<ParagraphEntityModel>> getParagraphSingleById(long[] paragraphIds);

    /**
     * 查询某个日期范围内的段落
     *
     * @param start 起始日期
     * @param end   结束日期（不包含）
     * @return 在日期范围内的按照日期顺序排序的日记段落分页列表
     */
    @Transaction
    @Query("SELECT * FROM paragraphs WHERE createTime >= :start AND createTime < :end ORDER BY createTime,paragraphId")
    PagingSource<Integer, ParagraphEntityModel> getParagraphPagingSourceByDate(LocalDate start, LocalDate end);

    /**
     * 查询某个日期段内的段落数量
     *
     * @param start 起始日期（包含）
     * @param end   结束日期（不包含）
     * @return 该时间段内的段落数量
     */
    @Query("SELECT COUNT(*) FROM paragraphs WHERE createTime >= :start AND createTime < :end")
    Single<Integer> getParagraphCountByDateRange(LocalDate start, LocalDate end);

    /**
     * 获取分页中需要跳转到的日记的段落的下标（所有段落都被读取到分页中的情况）
     *
     * @param date 日记的日期
     * @return 小于该日期的段落数量+小于该日期的日记数量，即需要跳转到的日记的段落下标
     */
    @Query("SELECT " +
            "(SELECT COUNT(*) FROM diaries WHERE diaryDate < :date) + " +
            "(SELECT COUNT(*) FROM paragraphs WHERE createTime < :date)"
    )
    Single<Integer> getAdjustedPositionSingle(LocalDate date);

    /**
     * 获取匹配搜索的段落的位置
     *
     * @param keyword          搜索关键词
     * @param emotionIds       情绪标签 ID 列表（如果传入空列表，代表不限制情绪，只按关键词搜索）
     * @param useContentFilter 是否过滤段落内容
     * @param useEmotionFilter 是否过滤情绪标签，(1：过滤，0：不过滤)
     * @return 包含所有匹配搜索位置的整数列表（已考虑日期分隔符），支持响应式更新
     */
    @Query(
            "SELECT (pure_paragraph_position + date_separator_count) " +
                    "FROM (" +
                    "    SELECT " +
                    "        paragraphId, " +
                    "        content, " +
                    "        -- 1. 计算纯段落的绝对位置（从 0 开始）\n" +
                    "        (ROW_NUMBER() OVER(ORDER BY createTime ASC) - 1) AS pure_paragraph_position," +
                    "        -- 2. 统计当前段落前的日期分隔符数量\n" +
                    "        (SELECT COUNT(*) FROM diaries d_sub WHERE d_sub.diaryDate <= d.diaryDate) AS date_separator_count" +
                    "    FROM paragraphs " +
                    "    INNER JOIN diaries d ON parentDiaryId = d.diaryId" +
                    ") " +
                    "WHERE (:useContentFilter = 0 OR (content LIKE '%' || :keyword || '%' ESCAPE '/')) " +
                    "  AND (:useEmotionFilter = 0 OR paragraphId IN (" +
                    "      SELECT paragraphId FROM emotionParagraphCrossRef WHERE emotionId IN (:emotionIds)" +
                    "  )) " +
                    "  AND (:useMediaFilter = 0 OR paragraphId IN (" +
                    "      SELECT parentParagraphId FROM medias" +
                    "  ))"
    )
    Flowable<List<Integer>> getSearchMatchedParagraphPositionsFlowableInternal(
            String keyword,
            List<Long> emotionIds,
            int useContentFilter,
            int useEmotionFilter,
            int useMediaFilter
    );

    /**
     * 通过日记 ID 获取段落
     *
     * @param diaryId 日记 ID
     * @return 该日记的段落列表
     */
    @Query("SELECT * FROM paragraphs WHERE parentDiaryId = :diaryId")
    List<ParagraphEntity> getParagraphByDiaryId(long diaryId);

    /**
     * 通过段落 ID 获取段落实体
     *
     * @param paragraphId 段落 ID
     * @return 包裹段落实体的{@link Optional}对象
     */
    @Transaction
    @Query("SELECT * FROM paragraphs WHERE paragraphId = :paragraphId")
    Single<Optional<ParagraphEntityModel>> getParagraphOptionalSingleById(long paragraphId);

    /**
     * 查询最大的日记长度
     *
     * @return 最大的日记字符数量
     */
    @Query(
            "SELECT COALESCE((" +
                    "SELECT TOTAL(LENGTH(content)) AS length FROM paragraphs " +
                    "GROUP BY parentDiaryId " +
                    "ORDER BY length DESC " +
                    "LIMIT 1" +
                    "), 0)"
    )
    Single<Integer> getMaxDiaryLengthSingle();

    /**
     * 查询平均日记长度
     *
     * @return 平均日记字符数量
     */
    @Query(
            "SELECT COALESCE(AVG(length), 0) FROM (" +
                    "SELECT TOTAL(LENGTH(content)) AS length " +
                    "FROM paragraphs " +
                    "GROUP BY parentDiaryId" +
                    ")"
    )
    Single<Integer> getAverageDiaryLengthSingle();

    /**
     * 插入一条日记段落
     *
     * @param paragraph 日记段落实例
     * @return 插入的日记段落自动分配的编号
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertParagraph(ParagraphEntity paragraph);

    /**
     * 插入段落的事务
     *
     * @param paragraph       新段落实例
     * @param newMediaUriList 该段落新添加的媒体 Uri 列表
     * @param db              数据库实例
     */
    @Transaction
    default void insertParagraph(
            ParagraphEntity paragraph,
            @NonNull List<Uri> newMediaUriList,
            @NonNull DiaryDatabase db
    ) {
        //插入段落
        long paragraphId = insertParagraph(paragraph);

        //插入新媒体
        List<MediaEntity> mediaEntityList = newMediaUriList.stream()
                .map(uri -> new MediaEntity(paragraphId, uri))
                .collect(Collectors.toList());
        MediaDao mediaDao = db.mediaDao();
        mediaDao.insertMedia(mediaEntityList);
    }

    /**
     * 批量插入日记段落
     *
     * @param paragraphEntityList 段落实体列表
     * @return 是否完成
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertParagraphCompletable(List<ParagraphEntity> paragraphEntityList);

    /**
     * 在导入日记时插入段落
     *
     * @param paragraphEntityList 段落列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertParagraphWhenImportingDiary(List<ParagraphEntity> paragraphEntityList);

    /**
     * 更新段落
     *
     * @param paragraph 修改后的段落
     * @return 是否成功
     */
    @Update
    Completable updateParagraphCompletable(ParagraphEntity paragraph);

    /**
     * 单线程更新段落
     *
     * @param paragraphList 更新后的段落列表
     */
    @Update
    void updateParagraph(List<ParagraphEntity> paragraphList);

    /**
     * 单线程更新段落
     *
     * @param paragraph 更新后的段落实体
     */
    @Update
    void updateParagraph(ParagraphEntity paragraph);

    /**
     * 段落更新事务
     *
     * @param paragraph       更新后的段落实体
     * @param newMediaUriList 新添加的媒体文件的 Uri 列表
     * @param db              数据库实例
     */
    @Transaction
    default void updateParagraph(
            ParagraphEntity paragraph,
            @NonNull List<Uri> newMediaUriList,
            @NonNull DiaryDatabase db
    ) {
        //更新段落
        updateParagraph(paragraph);

        //插入新媒体
        List<MediaEntity> newMediaList = newMediaUriList.stream()
                .map(uri -> new MediaEntity(paragraph.getParagraphId(), uri))
                .collect(Collectors.toList());
        MediaDao mediaDao = db.mediaDao();
        mediaDao.insertMedia(newMediaList);
    }

    /**
     * 删除段落
     *
     * @param paragraph 待删除的段落实例
     */
    @Delete
    void deleteParagraph(ParagraphEntity paragraph);

    /**
     * 删除某个日期段的段落
     *
     * @param start 起始日期（包含）
     * @param end   结束日期（不包含）
     */
    @Query("DELETE FROM paragraphs WHERE createTime >= :start AND createTime < :end")
    void deleteParagraphByDateRange(LocalDate start, LocalDate end);

    @Transaction
    default void insertDiaryWithParagraphs(LocalDate date, @NonNull List<ParagraphEntity> paragraphList, Context context) {
        DiaryDatabase db = DiaryDatabase.getInstance(context);
        DiaryDao diaryDao = db.diaryDao();

        if (paragraphList.isEmpty()) return;

        // 获取或创建日记 ID
        Long diaryId = diaryDao.getOrCreateDiaryIdByDate(date);

        // 删除当天的日记内容
        deleteParagraphByDateRange(date, date.plusDays(1));

        // 为所有段落关联最新的 ID（防止解析时 ID 还没生成）
        for (ParagraphEntity p : paragraphList) {
            p.setParentDiaryId(diaryId);
        }

        // 批量插入段落
        insertParagraphWhenImportingDiary(paragraphList);
    }
}
