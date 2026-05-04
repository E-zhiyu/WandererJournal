package com.wanderer.journal.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.wanderer.journal.data.save.db.entities.DiaryEntity;

import java.time.LocalDate;
import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface DiaryDao {
    /**
     * 获取日记数量
     *
     * @return 日记总数
     */
    @Query("SELECT COUNT(*) FROM diaries")
    int getDiaryCount();

    /**
     * 获取最早的日记日期
     *
     * @return 最早的日记日期
     */
    @Query("SELECT diaryDate FROM diaries ORDER BY diaryDate LIMIT 1")
    LocalDate getEarliestDiaryDate();

    /**
     * 获取所有日记
     *
     * @return 由所有日记实体类组成的列表，按照日期倒序排序，并支持响应式更新
     */
    @Query("SELECT * FROM diaries ORDER BY diaryDate DESC")
    Flowable<List<DiaryEntity>> getAllDiariesFlowable();

    /**
     * 插入一条日记
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
}
