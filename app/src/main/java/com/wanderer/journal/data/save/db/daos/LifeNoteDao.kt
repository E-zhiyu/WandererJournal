package com.wanderer.journal.data.save.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.wanderer.journal.data.save.db.entities.LifeNoteEntity
import com.wanderer.journal.data.save.db.entities.LifeNoteHistoryEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import java.util.Optional
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeNoteDao {
    /**
     * 获取人生笔记数量。
     *
     * @return 人生笔记数量，支持响应式更新
     */
    @Query("SELECT COUNT(*) FROM lifeNotes")
    fun getLifeNoteCountFlow(): Flow<Int>

    /**
     * 获取所有人生笔记。
     *
     * @param safeKeyword 转义后的搜索关键词
     * @param filterSearch 是否启用搜索
     * @return 人生笔记列表
     */
    @Query(
        "SELECT * FROM lifeNotes " +
            "WHERE :filterSearch = 0 " +
            "OR insight LIKE '%' || :safeKeyword || '%' ESCAPE '/' " +
            "OR elaboration LIKE '%' || :safeKeyword || '%' ESCAPE '/' " +
            "ORDER BY dateTime DESC"
    )
    fun getAllLifeNoteFlowable(safeKeyword: String, filterSearch: Int): Flowable<List<LifeNoteEntity>>

    /**
     * 根据人生笔记 ID 获取修改历史记录。
     *
     * @param noteId 人生笔记 ID
     * @return 修改历史记录列表，支持响应式更新
     */
    @Query("SELECT * FROM lifeNoteHistories WHERE noteId = :noteId ORDER BY updateDateTime DESC")
    fun getLifeNoteHistoryFlowableByNoteId(noteId: Long): Flowable<List<LifeNoteHistoryEntity>>

    /**
     * 通过 ID 查询人生笔记。
     *
     * @param id 需要获取的笔记的 ID
     * @return 人生笔记数据实体
     */
    @Query("SELECT * FROM lifeNotes WHERE noteId = :id")
    fun getLifeNoteOptionalSingleById(id: Long): Single<Optional<LifeNoteEntity>>

    /**
     * 通过 ID 查询人生笔记。
     *
     * @param id 需要获取的笔记的 ID
     * @return 人生笔记数据实体
     */
    @Query("SELECT * FROM lifeNotes WHERE noteId = :id")
    fun getLifeNoteById(id: Long): LifeNoteEntity?

    /**
     * 新增人生笔记。
     *
     * @param entity 人生笔记数据实体
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertLifeNote(entity: LifeNoteEntity): Completable

    /**
     * 更新人生笔记。
     *
     * @param entity 新人生笔记实体
     */
    @Update
    fun updateLifeNote(entity: LifeNoteEntity)

    /**
     * 插入人生笔记修改历史记录。
     *
     * @param entity 人生笔记修改历史数据
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertLifeNoteHistory(entity: LifeNoteHistoryEntity)

    /**
     * 修改人生笔记的事务。
     *
     * @param newLifeNote 新人生笔记数据
     */
    @Transaction
    fun modifyLifeNote(newLifeNote: LifeNoteEntity?) {
        if (newLifeNote == null) return

        // 获取旧数据
        val noteId = newLifeNote.noteId
        val oldLifeNote = getLifeNoteById(noteId) ?: return
        val oldInsight = oldLifeNote.insight
        val oldElaboration = oldLifeNote.elaboration

        // 仅当内容真正改变才保存记录
        if (newLifeNote.insight != oldInsight || newLifeNote.elaboration != oldElaboration) {
            // 生成修改历史记录
            val oldDateTime = oldLifeNote.dateTime
            val historyEntity = LifeNoteHistoryEntity(
                noteId,
                oldInsight,
                oldElaboration,
                oldDateTime,
            )
            insertLifeNoteHistory(historyEntity)
        }

        // 更新人生笔记
        updateLifeNote(newLifeNote)
    }

    /**
     * 删除人生笔记。
     *
     * @param lifeNote 需要删除的人生笔记
     */
    @Delete
    fun deleteLifeNote(lifeNote: LifeNoteEntity): Completable

    /**
     * 删除人生笔记历史记录。
     *
     * @param history 需要删除的历史记录
     */
    @Delete
    fun deleteLifeNoteHistory(history: LifeNoteHistoryEntity): Completable
}
