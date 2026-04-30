package com.wanderer.journal.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Query;

import com.wanderer.journal.data.save.db.entities.DiaryEntity;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface DiaryDao {
    /**
     * 获取日记数量
     * @return 日记总数
     */
    @Query("SELECT COUNT(*) FROM diaries")
    int getDiaryCount();

    /**
     * 获取最早的日记日期
     * @return 最早的日记日期
     */
    @Query("SELECT diaryDate FROM diaries ORDER BY diaryDate LIMIT 1")
    LocalDate getEarliestDiaryDate();

    /**
     * 获取所有日记
     * @return 由所有日记实体类组成的列表，按照日期倒序排序
     */
    @Query("SELECT * FROM diaries ORDER BY diaryDate DESC")
    List<DiaryEntity> getAllDiaries();
}
