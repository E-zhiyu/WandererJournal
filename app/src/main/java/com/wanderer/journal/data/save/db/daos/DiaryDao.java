package com.wanderer.journal.data.save.db.daos;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.composite.DiaryWithSummaryUiModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface DiaryDao {
    /**
     * 获取日记数量
     *
     * @return 日记总数，支持响应式更新
     */
    @Query("SELECT COUNT(*) FROM diaries")
    Flowable<Integer> getDiaryCountFlowable();

    /**
     * 获取最早的日记日期
     *
     * @return 最早的日记日期，支持响应式更新
     */
    @Query("SELECT diaryDate FROM diaries ORDER BY diaryDate LIMIT 1")
    Flowable<Optional<LocalDate>> getEarliestDiaryDateFlowable();

    /**
     * 获取所有日记
     *
     * @return 由{@link DiaryWithSummaryUiModel}组成的列表，支持响应式更新
     */
    @Query("SELECT d.*," +
            "IFNULL((SELECT SUBSTR(content, 1, 30) FROM paragraphs WHERE parentDiaryId = d.diaryId ORDER BY createTime LIMIT 1), '') as paragraphFragment," +
            "(SELECT COUNT(*) FROM paragraphs WHERE parentDiaryId = d.diaryId) as paragraphCount " +
            "FROM diaries d " +
            "ORDER BY diaryDate DESC")
    Flowable<List<DiaryWithSummaryUiModel>> getAllDiariesFlowable();

    /**
     * 查询指定日期之前（包括该日期）的所有日记的日期
     *
     * @param end 截止日期
     * @return 日期列表
     */
    @Query("SELECT diaryDate FROM diaries WHERE diaryDate <= :end ORDER BY diaryDate")
    Single<List<LocalDate>> getDiaryDateSingle(LocalDate end);

    /**
     * 插入一条日记
     *
     * @param diary 新日记内容
     * @return 日记编号
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long insertDiary(DiaryEntity diary);

    /**
     * 查询目标日期的日记编号
     *
     * @param date 日记的日期
     * @return 日记编号，未查询到返回null
     */
    @Query("SELECT diaryId FROM diaries WHERE diaryDate == :date")
    Long getDiaryIdByDate(LocalDate date);

    /**
     * 通过多线程获取指定日期的日记
     *
     * @param date 待查询的日期
     * @return 该日期的日记（无则通过{@link Maybe}返回 null
     */
    @Query("SELECT * FROM diaries WHERE diaryDate == :date")
    Single<Optional<DiaryEntity>> getDiarySingleByDate(LocalDate date);

    /**
     * 通过日期删除日记
     *
     * @param date 待删除日记的日期
     */
    @Query("DELETE FROM diaries WHERE diaryDate == :date")
    void deleteDiaryByDate(LocalDate date);

    /**
     * 事务处理：获取日期所对应的日记编号
     *
     * @param date 日期
     * @return 日期所对应的日记编号
     */
    @Transaction
    default Long getOrCreateDiaryIdByDate(LocalDate date) {
        Long diaryId = getDiaryIdByDate(date);
        if (diaryId != null) {
            return diaryId;
        } else {
            return insertDiary(new DiaryEntity(date));
        }
    }

    /**
     * 更新日记日期
     *
     * @param diaryId      原始日记的日记编号
     * @param targetDate   需要更改到的日期
     * @param paragraphDao 段落查询接口
     */
    @Transaction
    default void modifyDiaryDate(long diaryId, LocalDate targetDate, @NonNull ParagraphDao paragraphDao) {
        //先删除目标日期的日记
        deleteDiaryByDate(targetDate);

        //实例化新的日记并写入
        DiaryEntity newDiary = new DiaryEntity(targetDate);
        newDiary.setDiaryId(diaryId);
        updateDiary(newDiary);

        //构建新段落列表并写入
        List<ParagraphEntity> originParagraphList = paragraphDao.getParagraphByDiaryId(diaryId);
        List<ParagraphEntity> newParagraphList = originParagraphList.stream()
                .map(paragraph -> {
                    //计算得到新的时间
                    LocalDateTime newDateTime = paragraph.getCreateTime()
                            .withYear(targetDate.getYear())
                            .withMonth(targetDate.getMonthValue())
                            .withDayOfMonth(targetDate.getDayOfMonth());

                    //构建并返回新段落实体
                    ParagraphEntity newParagraph = new ParagraphEntity(diaryId, paragraph.getContent(), newDateTime);
                    newParagraph.setParagraphId(paragraph.getParagraphId());
                    return newParagraph;
                })
                .collect(Collectors.toList());
        paragraphDao.updateParagraph(newParagraphList);
    }

    /**
     * 删除日记
     *
     * @param diary 待删除的日记实例
     * @return 是否完成
     */
    @Delete
    Completable deleteDiaryCompletable(DiaryEntity diary);

    /**
     * 更新日记日期
     *
     * @param diary 更新了日期的日记
     */
    @Update
    void updateDiary(DiaryEntity diary);

    /**
     * 读取所有数据用于导出
     *
     * @return 日记实体列表
     */
    @Query("SELECT * FROM diaries")
    List<DiaryEntity> exportData();

    /**
     * 批量导入数据
     *
     * @param diaryEntityList 待导入的日记数据列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void importData(List<DiaryEntity> diaryEntityList);

    /**
     * 清空表，准备导入新数据
     */
    @Query("DELETE FROM diaries")
    void clear();
}
