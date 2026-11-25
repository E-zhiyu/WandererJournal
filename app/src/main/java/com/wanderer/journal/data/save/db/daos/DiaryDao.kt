package com.wanderer.journal.data.save.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.wanderer.journal.data.save.db.entities.DiaryEntity
import com.wanderer.journal.data.save.db.entities.ParagraphEntity
import com.wanderer.journal.data.save.db.entities.composite.ui.DiaryContentUnionModel
import com.wanderer.journal.data.save.db.entities.composite.union.DiaryLengthUnionModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import java.time.LocalDate
import java.util.Optional
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    /**
     * 获取日记数量。
     *
     * @return 日记总数，支持响应式更新
     */
    @Query("SELECT COUNT(*) FROM diaries")
    fun getDiaryCountFlow(): Flow<Int>

    /**
     * 获取最早的日记日期。
     *
     * @return 最早的日记日期，支持响应式更新
     */
    @Query("SELECT diaryDate FROM diaries ORDER BY diaryDate LIMIT 1")
    fun getEarliestDiaryDateFlow(): Flow<LocalDate>

    /**
     * 获取最早的日记日期。
     *
     * @return 最早的日记日期
     */
    @Query("SELECT diaryDate FROM diaries ORDER BY diaryDate LIMIT 1")
    fun getEarliestDiaryDateSingle(): Single<Optional<LocalDate>>

    /**
     * 获取所有日记，并附带日记段落片段和日记长度数据。
     *
     * @return 由 [DiaryContentUnionModel] 组成的列表，支持响应式更新
     */
    @Query(
        """
        SELECT d.*,
        CASE
          WHEN (SELECT privacyType FROM paragraphs
                WHERE parentDiaryId = d.diaryId ORDER BY createTime LIMIT 1) = 2
          THEN '<该段落的内容受保护>'
          ELSE IFNULL((SELECT SUBSTR(content, 1, 30) FROM paragraphs
                       WHERE parentDiaryId = d.diaryId ORDER BY createTime LIMIT 1), '<无日记内容>')
        END as paragraphFragment,
        (SELECT SUM(LENGTH(content)) FROM paragraphs
         WHERE parentDiaryId = d.diaryId) as charCount
        FROM diaries d
        ORDER BY diaryDate DESC
        """
    )
    fun getAllDiariesFlow(): Flow<List<DiaryContentUnionModel>>

    /**
     * 获取日记段落的字符数量数据，支持响应式更新。
     *
     * @param start 起算日期（包含）
     * @param end 截止日期（包含）
     * @return 在指定日期段的日记的段落数量数据
     */
    @Query(
        """
        SELECT diaryDate AS diaryDate,
        (SELECT SUM(LENGTH(content)) FROM paragraphs WHERE parentDiaryId = diaryId) AS diaryLength
        FROM diaries
        WHERE diaryDate >= :start AND diaryDate <= :end
        """
    )
    fun getDiaryParagraphWordCountFlowable(
        start: LocalDate,
        end: LocalDate,
    ): Flowable<List<DiaryLengthUnionModel>>

    /**
     * 查询指定日期之前（包括该日期）的所有日记的日期。
     *
     * @param end 截止日期
     * @return 日期列表，升序排序
     */
    @Query("SELECT diaryDate FROM diaries WHERE diaryDate <= :end ORDER BY diaryDate")
    fun getDiaryDateSingle(end: LocalDate): Single<List<LocalDate>>

    /**
     * 插入一条日记。
     *
     * @param diary 新日记内容
     * @return 日记编号
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertDiary(diary: DiaryEntity): Long?

    /**
     * 查询目标日期的日记编号。
     *
     * @param date 日记的日期
     * @return 日记编号，未查询到返回 null
     */
    @Query("SELECT diaryId FROM diaries WHERE diaryDate == :date")
    fun getDiaryIdByDate(date: LocalDate): Long?

    /**
     * 通过日期删除日记。
     *
     * @param date 待删除日记的日期
     */
    @Query("DELETE FROM diaries WHERE diaryDate == :date")
    fun deleteDiaryByDate(date: LocalDate)

    /**
     * 事务处理：获取日期所对应的日记编号。
     *
     * @param date 日期
     * @return 日期所对应的日记编号
     */
    @Transaction
    fun getOrCreateDiaryIdByDate(date: LocalDate): Long? {
        val diaryId = getDiaryIdByDate(date)
        return diaryId ?: insertDiary(DiaryEntity(date))
    }

    /**
     * 更新日记日期。
     *
     * @param diaryId 原始日记的日记编号
     * @param targetDate 需要更改到的日期
     * @param paragraphDao 段落查询接口
     */
    @Transaction
    fun modifyDiaryDate(diaryId: Long, targetDate: LocalDate, paragraphDao: ParagraphDao?) {
        if (paragraphDao == null) return

        // 先删除目标日期的日记
        deleteDiaryByDate(targetDate)

        // 实例化新的日记并写入
        val newDiary = DiaryEntity(targetDate)
        newDiary.diaryId = diaryId
        updateDiary(newDiary)

        // 构建新段落列表并写入
        val originParagraphList = paragraphDao.getParagraphByDiaryId(diaryId)
        val newParagraphList = originParagraphList.map { paragraph ->
            // 计算得到新的时间并更新
            val time = paragraph.createTime.toLocalTime()
            paragraph.createTime = targetDate.atTime(time)
            paragraph
        }
        paragraphDao.updateParagraph(newParagraphList)
    }

    /**
     * 删除日记。
     *
     * @param diary 待删除的日记实例
     */
    @Delete
    fun deleteDiary(diary: DiaryEntity)

    /**
     * 更新日记日期。
     *
     * @param diary 更新了日期的日记
     */
    @Update
    fun updateDiary(diary: DiaryEntity)
}
