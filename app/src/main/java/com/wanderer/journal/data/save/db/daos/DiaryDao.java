package com.wanderer.journal.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.composite.DiaryWithSummary;

import java.time.LocalDate;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface DiaryDao {
    /**
     * 获取日记数量
     *
     * @return 日记总数，支持响应式更新
     */
    @Query("SELECT COUNT(*) FROM diaries")
    Flowable<Integer> getDiaryCount();

    /**
     * 获取最早的日记日期
     *
     * @return 最早的日记日期，支持响应式更新
     */
    @Query("SELECT diaryDate FROM diaries ORDER BY diaryDate LIMIT 1")
    Flowable<LocalDate> getEarliestDiaryDate();

    /**
     * 获取所有日记
     *
     * @return 由{@link DiaryWithSummary}组成的列表，支持响应式更新
     */
    @Query("SELECT d.*," +
            "IFNULL((SELECT SUBSTR(content, 1, 30) FROM paragraphs WHERE parentDiaryId = d.diaryId ORDER BY createTime LIMIT 1), '') as paragraphFragment," +
            "(SELECT COUNT(*) FROM paragraphs WHERE parentDiaryId = d.diaryId) as paragraphCount " +
            "FROM diaries d " +
            "ORDER BY diaryDate DESC")
    Flowable<List<DiaryWithSummary>> getAllDiariesFlowable();

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
     * 删除日记
     *
     * @param diary 待删除的日记实例
     * @return 是否完成
     */
    @Delete
    Completable deleteDiary(DiaryEntity diary);

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
