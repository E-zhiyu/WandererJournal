package com.wanderer.journal.data.save.db.daos

import android.location.Location
import android.net.Uri
import android.util.Pair
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.wanderer.journal.auxiliary.classes.statistics.ParagraphStatistics
import com.wanderer.journal.data.save.db.entities.DiaryEntity
import com.wanderer.journal.data.save.db.entities.LocationEntity
import com.wanderer.journal.data.save.db.entities.MediaEntity
import com.wanderer.journal.data.save.db.entities.ParagraphEntity
import com.wanderer.journal.data.save.db.entities.composite.union.ParagraphUnionModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.HashSet
import java.util.Optional
import kotlinx.coroutines.flow.Flow

@Dao
interface ParagraphDao {
    /**
     * 获取段落总数。
     *
     * @return 段落总数
     */
    @Query("SELECT COUNT(*) FROM paragraphs")
    fun getParagraphCountFlow(): Flow<Int>

    /**
     * 读取所有段落并支持局部加载。
     *
     * @param show 是否显示被隐藏的日记段落
     * @return 可局部加载的日记段落列表
     */
    @Transaction
    @Query(
        """
        SELECT * FROM paragraphs
        WHERE (:show OR privacyType != 2)
        ORDER BY createTime
        """
    )
    fun getAllParagraphPagingSource(show: Boolean): PagingSource<Int, ParagraphUnionModel>

    /**
     * 查询某个日期范围内的段落。
     *
     * @param start 起始日期
     * @param end 结束日期（不包含）
     * @param isAuthed 是否通过身份验证，通过验证后能够显示被隐藏的日记段落
     * @return 在日期范围内的按照日期顺序排序的日记段落分页列表
     */
    @Transaction
    @Query(
        """
        SELECT * FROM paragraphs
        WHERE createTime >= :start AND createTime < :end AND (:isAuthed OR privacyType != 2)
        ORDER BY createTime, paragraphId
        """
    )
    fun getParagraphPagingSourceInDateRange(
        start: LocalDate,
        end: LocalDate,
        isAuthed: Int,
    ): PagingSource<Int, ParagraphUnionModel>

    /**
     * 查询指定 ID 的段落。
     *
     * @param paragraphIds 需要查询的段落的 ID
     * @return ID 包含在数组中的段落
     */
    @Transaction
    @Query("SELECT * FROM paragraphs WHERE paragraphId IN (:paragraphIds)")
    fun getParagraphSingleById(paragraphIds: LongArray): Single<List<ParagraphUnionModel>>

    /**
     * 查询某个日期段内的段落数量。
     *
     * @param start 起始日期（包含）
     * @param end 结束日期（不包含）
     * @return 该时间段内的段落数量
     */
    @Query("SELECT COUNT(*) FROM paragraphs WHERE createTime >= :start AND createTime < :end")
    fun getParagraphCountByDateRange(start: LocalDate, end: LocalDate): Single<Int>

    /**
     * 获取分页中需要跳转到的日记的段落的下标（所有段落都被读取到分页中的情况）。
     *
     * @param date 日记的日期
     * @return 小于该日期的段落数量 + 小于该日期的日记数量，即需要跳转到的日记的段落下标
     */
    @Query(
        """
        SELECT
        (SELECT COUNT(*) FROM diaries WHERE diaryDate < :date AND (
           SELECT COUNT(*) FROM paragraphs WHERE parentDiaryId = diaryId AND (:isAuthed OR privacyType != 2)
        ) > 0) +
        (SELECT COUNT(*) FROM paragraphs WHERE createTime < :date AND (:isAuthed OR privacyType != 2))
        """
    )
    fun getAdjustedPositionSingle(date: LocalDate, isAuthed: Boolean): Single<Int>

    /**
     * 使用 RawQuery 动态计算匹配搜索的段落位置。
     *
     * @param query 支持响应式更新的 SupportSQLiteQuery
     * @return 包含所有匹配位置的列表，支持 Flowable 响应式
     */
    @RawQuery(observedEntities = [ParagraphEntity::class, DiaryEntity::class])
    fun getSearchMatchedParagraphIdsRaw(query: SupportSQLiteQuery): Flowable<List<Long>>

    /**
     * 查找某个段落的具体位置，用于在需要跳转到未加载的段落时获取绝对位置。
     *
     * @param paragraphId 需要定位的段落的编号
     * @param isHiddenShown 隐藏的段落是否显示
     * @return 该段落的粗略位置
     */
    @Query(
        """
        WITH params AS (
            SELECT createTime FROM paragraphs WHERE paragraphId = :paragraphId
        )
        SELECT paragraphCount + diaryCount FROM (SELECT
           (SELECT COUNT(*)
               FROM paragraphs, params
               WHERE paragraphs.createTime <= params.createTime
                   AND (:isHiddenShown OR privacyType != 2)
           ) AS paragraphCount,
           (SELECT COUNT(*)
               FROM diaries, params
               WHERE diaries.diaryDate <= params.createTime
                   AND EXISTS (SELECT 1 FROM paragraphs WHERE parentDiaryId = diaryId AND (:isHiddenShown OR privacyType != 2))
           ) AS diaryCount
        )
        """
    )
    fun getParagraphPositionById(paragraphId: Long, isHiddenShown: Boolean): Single<Int>

    /**
     * 获取指定日期的日记首段的编号。
     *
     * @param date 目标日期
     * @param isHiddenParagraphShown 隐藏的段落是否显示
     * @return 首段的编号
     */
    @Query(
        """
        SELECT paragraphId FROM paragraphs
        WHERE createTime >= :date AND (:isHiddenParagraphShown OR privacyType != 2)
        ORDER BY createTime ASC
        LIMIT 1
        """
    )
    fun getDiaryStartPositionByDate(
        date: LocalDate,
        isHiddenParagraphShown: Boolean,
    ): Single<Optional<Long>>

    /**
     * 通过日记 ID 获取段落。
     *
     * @param diaryId 日记 ID
     * @return 该日记的段落列表
     */
    @Query("SELECT * FROM paragraphs WHERE parentDiaryId = :diaryId")
    fun getParagraphByDiaryId(diaryId: Long): List<ParagraphEntity>

    /**
     * 通过段落 ID 获取段落实体。
     *
     * @param paragraphId 段落 ID
     * @return 包裹段落实体的 [Optional] 对象
     */
    @Transaction
    @Query("SELECT * FROM paragraphs WHERE paragraphId = :paragraphId")
    fun getParagraphOptionalSingleById(paragraphId: Long): Single<Optional<ParagraphUnionModel>>

    /**
     * 查询最大的日记长度。
     *
     * @return 最大的日记字符数量
     */
    @Query(
        """
        SELECT COALESCE((
        SELECT TOTAL(LENGTH(content)) AS length FROM paragraphs
        GROUP BY parentDiaryId
        ORDER BY length DESC
        LIMIT 1
        ), 0)
        """
    )
    fun getMaxDiaryLengthFlowable(): Flowable<Int>

    /**
     * 查询平均日记长度。
     *
     * @return 平均日记字符数量
     */
    @Query(
        """
        SELECT COALESCE(AVG(length), 0) FROM (
        SELECT TOTAL(LENGTH(content)) AS length
        FROM paragraphs
        GROUP BY parentDiaryId
        )
        """
    )
    fun getAverageDiaryLengthFlowable(): Flowable<Int>

    /**
     * 获取某段时间的平均日记长度。
     *
     * @param start 起始时间（包含）
     * @param end 结束时间（不包含）
     * @return 该时间段内的平均日记长度
     */
    @Query(
        """
        SELECT COALESCE(AVG(length), 0) FROM (
        SELECT TOTAL(LENGTH(content)) AS length
        FROM paragraphs
        WHERE createTime >= :start AND createTime < :end
        GROUP BY parentDiaryId
        )
        """
    )
    fun getAverageDiaryLengthSingleInTimeRange(
        start: LocalDateTime,
        end: LocalDateTime,
    ): Single<Int>

    /**
     * 插入一条日记段落。
     *
     * @param paragraph 日记段落实例
     * @return 插入的日记段落自动分配的编号
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParagraph(paragraph: ParagraphEntity): Long

    /**
     * 获取插入的段落在当天的位置。
     *
     * @param startDate 段落所属日记的日期
     * @param paragraphTime 段落插入的时间
     * @param paragraphId 需要排除的段落的 ID
     * @return 插入的段落在当天的位置
     */
    @Query(
        """
        SELECT COUNT(*) FROM paragraphs
        WHERE createTime >= :startDate
           AND createTime <= :paragraphTime
           AND paragraphId != :paragraphId
           AND (:isAuthed OR privacyType != 2)
        """
    )
    fun getNewParagraphPosition(
        startDate: LocalDate,
        paragraphTime: LocalDateTime,
        paragraphId: Long,
        isAuthed: Int,
    ): Int

    /**
     * 插入位置信息。
     *
     * @param location 位置信息实体
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertLocation(location: LocationEntity)

    /**
     * 插入段落的事务。
     *
     * @param startDate 写日记界面的起始日期
     * @param paragraph 新段落实例
     * @param mediaList 该段落新添加的媒体列表
     * @param locationPair 段落的位置信息
     * @param isHiddenParagraphShown 隐藏的段落是否显示
     * @param mediaDao 媒体 Dao 类
     * @return 新添加的段落在当前界面中的下标
     */
    @Transaction
    fun addParagraph(
        startDate: LocalDate,
        paragraph: ParagraphEntity?,
        mediaList: List<MediaEntity>,
        locationPair: Pair<Boolean, Location>?,
        isHiddenParagraphShown: Boolean,
        mediaDao: MediaDao?,
    ): Int {
        if (paragraph == null || mediaDao == null) return -1

        // 获取从起始日期开始，有多少个段落日期小于等于该段落
        val earlierThanNewParagraph = getNewParagraphPosition(
            startDate,
            paragraph.createTime,
            paragraph.paragraphId,
            if (isHiddenParagraphShown) 1 else 0,
        )

        // 计算有几个日期分隔符
        val dateSeparatorCount = getDiaryCountByTimeRange(
            startDate.atStartOfDay(),
            paragraph.createTime,
        )

        // 插入段落
        val paragraphId = insertParagraph(paragraph)

        // 插入新媒体
        mediaList.forEach { media -> media.parentParagraphId = paragraphId }
        mediaDao.insertMedia(mediaList)

        // 插入位置信息
        if (locationPair != null) {
            val location = locationPair.second
            val locationEntity = LocationEntity(
                paragraphId,
                location.latitude,
                location.longitude,
                location.altitude,
                locationPair.first,
                location.provider,
            )
            insertLocation(locationEntity)
        }

        return earlierThanNewParagraph + dateSeparatorCount
    }

    /**
     * 批量插入日记段落。
     *
     * @param paragraphEntityList 段落实体列表
     * @return 是否完成
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParagraphCompletable(paragraphEntityList: List<ParagraphEntity>): Completable

    /**
     * 在导入日记时插入段落。
     *
     * @param paragraphEntityList 段落列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParagraphWhenImportingDiary(paragraphEntityList: List<ParagraphEntity>)

    /**
     * 根据段落 ID 更新段落创建时间。
     *
     * @param paragraphId 需要更新的段落的 ID
     * @param time 更新后的创建时间
     */
    @Query("UPDATE paragraphs SET createTime = :time WHERE paragraphId = :paragraphId")
    fun updateCreateTimeById(paragraphId: Long, time: LocalDateTime)

    /**
     * 获取处于时间范围内的日记数量。
     *
     * @param start 起始时间（包含）
     * @param end 结束时间
     * @return 在时间范围内的日记数量
     */
    @Query("SELECT COUNT(*) FROM diaries WHERE diaryDate >= :start AND diaryDate < :end")
    fun getDiaryCountByTimeRange(start: LocalDateTime, end: LocalDateTime): Int

    /**
     * 修改段落创建时间。
     *
     * @param startDate 列表中最开始的日期
     * @param newCreateTime 修改后的创建时间
     * @param paragraphId 需要修改的段落的 ID
     * @param isHiddenParagraphShown 隐藏的段落是否显示
     * @return 修改后的段落在列表中的下标
     */
    fun modifyCreateTime(
        startDate: LocalDate,
        newCreateTime: LocalDateTime,
        paragraphId: Long,
        isHiddenParagraphShown: Boolean,
    ): Int {
        // 获取从起始日期开始，有多少个段落日期小于等于该段落
        val earlierThanNewParagraph = getNewParagraphPosition(
            startDate,
            newCreateTime,
            paragraphId,
            if (isHiddenParagraphShown) 1 else 0,
        )

        // 计算有几个日期分隔符
        val dateSeparatorCount = getDiaryCountByTimeRange(startDate.atStartOfDay(), newCreateTime)

        // 更新段落
        updateCreateTimeById(paragraphId, newCreateTime)

        return earlierThanNewParagraph + dateSeparatorCount
    }

    /**
     * 单线程更新段落。
     *
     * @param paragraphList 更新后的段落列表
     */
    @Update
    fun updateParagraph(paragraphList: List<ParagraphEntity>)

    /**
     * 单线程更新段落。
     *
     * @param paragraph 更新后的段落实体
     */
    @Update
    fun updateParagraph(paragraph: ParagraphEntity)

    /**
     * 通过段落编号获取媒体文件的 Uri。
     *
     * @param paragraphId 段落编号
     * @return 该段落的所有媒体文件的 Uri
     */
    @Query("SELECT fileUri FROM medias WHERE parentParagraphId = :paragraphId")
    fun getMediaUriByParagraphId(paragraphId: Long): List<Uri>

    /**
     * 通过段落编号删除位置。
     *
     * @param paragraphId 段落编号
     */
    @Query("DELETE FROM locations WHERE paragraphId = :paragraphId")
    fun deleteLocationByParagraphId(paragraphId: Long)

    /**
     * 段落更新事务。
     *
     * @param paragraph 更新后的段落实体
     * @param mediaList 最终的媒体列表
     * @param locationPair 段落的位置信息
     * @param mediaDao 媒体 Dao 类
     * @return 需要删除的媒体文件的 Uri 集合
     */
    @Transaction
    fun modifyParagraph(
        paragraph: ParagraphEntity?,
        mediaList: List<MediaEntity>,
        locationPair: Pair<Boolean, Location>?,
        mediaDao: MediaDao,
    ): Set<Uri>? {
        if (paragraph == null) return null
        val paragraphId = paragraph.paragraphId

        // 获取在数据库中的媒体文件 Uri，并计算需要删除的媒体文件的 Uri
        val oldMediaUriSet = HashSet(getMediaUriByParagraphId(paragraphId))
        val newMediaUriSet = mediaList.map { media -> media.fileUri }.toSet()
        oldMediaUriSet.removeAll(newMediaUriSet)

        // 更新段落
        updateParagraph(paragraph)

        // 更新媒体
        mediaDao.deleteMediaByParagraphId(paragraphId)
        mediaDao.insertMedia(mediaList)

        // 更新位置信息
        deleteLocationByParagraphId(paragraphId)
        if (locationPair != null) {
            val location = locationPair.second
            val locationEntity = LocationEntity(
                paragraphId,
                location.latitude,
                location.longitude,
                location.altitude,
                locationPair.first,
                location.provider,
            )
            insertLocation(locationEntity)
        }

        return oldMediaUriSet
    }

    /**
     * 删除段落。
     *
     * @param paragraph 待删除的段落实例
     */
    @Delete
    fun deleteParagraph(paragraph: ParagraphEntity)

    /**
     * 删除某个日期段的段落。
     *
     * @param start 起始日期（包含）
     * @param end 结束日期（不包含）
     */
    @Query("DELETE FROM paragraphs WHERE createTime >= :start AND createTime < :end")
    fun deleteParagraphByDateRange(start: LocalDate, end: LocalDate)

    @Transaction
    fun addDiaryWithParagraphs(
        date: LocalDate,
        paragraphList: List<ParagraphEntity>?,
        diaryDao: DiaryDao,
    ) {
        if (paragraphList.isNullOrEmpty()) return

        // 获取或创建日记 ID
        val diaryId = diaryDao.getOrCreateDiaryIdByDate(date) ?: return

        // 删除当天的日记内容
        deleteParagraphByDateRange(date, date.plusDays(1))

        // 为所有段落关联最新的 ID（防止解析时 ID 还没生成）
        for (paragraph in paragraphList) {
            paragraph.parentDiaryId = diaryId
        }

        // 批量插入段落
        insertParagraphWhenImportingDiary(paragraphList)
    }

    /**
     * 更新段落的隐私类别。
     *
     * @param paragraphId 需要更新的段落的编号
     * @param type 更新后的隐私类别
     * @return 是否完成
     */
    @Query("UPDATE paragraphs SET privacyType = :type WHERE paragraphId = :paragraphId")
    fun updatePrivacyTypeCompletable(paragraphId: Long, type: Int): Completable

    /**
     * 获取段落总结数据。
     *
     * @return 段落总结数据，包括段落长度和时间的总结数据
     */
    @Query(
        """
        SELECT
        (SELECT MAX(LENGTH(content)) FROM paragraphs) AS maxLength,
        (SELECT MIN(LENGTH(content)) FROM paragraphs) AS minLength,
        (SELECT AVG(LENGTH(content)) FROM paragraphs) AS avgLength,
        (SELECT COUNT(*) FROM paragraphs) AS totalCount,
        (SELECT COUNT(*) FROM paragraphs WHERE createTime % 86400000 >= 3600000 AND createTime % 86400000 < 18000000) AS weeHoursCount,
        (SELECT COUNT(*) FROM paragraphs WHERE createTime % 86400000 >= 18000000 AND createTime % 86400000 < 39600000) AS forenoonCount,
        (SELECT COUNT(*) FROM paragraphs WHERE createTime % 86400000 >= 39600000 AND createTime % 86400000 < 50400000) AS noonCount,
        (SELECT COUNT(*) FROM paragraphs WHERE createTime % 86400000 >= 50400000 AND createTime % 86400000 < 64800000) AS afternoonCount,
        (SELECT COUNT(*) FROM paragraphs WHERE createTime % 86400000 >= 64800000 AND createTime % 86400000 < 82800000) AS nightCount,
        (SELECT COUNT(*) FROM paragraphs WHERE createTime % 86400000 >= 82800000 OR createTime % 86400000 < 3600000 AND createTime % 86400000 != 0) AS midnightCount,
        (SELECT COUNT(*) FROM paragraphs WHERE createTime % 86400000 = 0) AS timeUnknownCount
        """
    )
    fun getParagraphStatisticsFlowable(): Flowable<ParagraphStatistics>
}
