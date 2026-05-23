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
     * 查询某个日期范围内的段落
     *
     * @param start 起始日期
     * @param end   结束日期（不包含）
     * @return 在日期范围内的按照日期顺序排序的日记段落分页列表
     */
    @Transaction
    @Query("SELECT * FROM paragraphs WHERE createTime >= :start AND createTime < :end ORDER BY createTime,paragraphId")
    PagingSource<Integer, ParagraphEntityModel> getParagraphPagingSourceInRange(LocalDate start, LocalDate end);

    /**
     * 获取分页中需要跳转到的日记的段落的下标（所有段落都被读取到分页中的情况）
     *
     * @param date 日记的日期
     * @return 小于该日期的段落数量，即需要跳转到的日记的段落下标
     */
    @Query("SELECT COUNT(*) FROM paragraphs WHERE createTime < :date")
    int getAdjustedPositionSingle(LocalDate date);

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

    /**
     * 读取所有数据用于导出
     *
     * @return 段落实体列表
     */
    @Query("SELECT * FROM paragraphs")
    List<ParagraphEntity> exportData();

    /**
     * 清空所有数据，准备导入新数据
     */
    @Query("DELETE FROM paragraphs")
    void clear();

    /**
     * 导入新数据
     *
     * @param paragraphEntityList 待导入数据的实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void importData(List<ParagraphEntity> paragraphEntityList);

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
