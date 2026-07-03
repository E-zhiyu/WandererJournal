package com.wanderer.journal.data.save.db.daos;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.wanderer.journal.data.save.db.entities.LifeNoteEntity;
import com.wanderer.journal.data.save.db.entities.LifeNoteHistoryEntity;
import com.wanderer.journal.data.save.db.entities.composite.LifeNoteWithHistoryModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface LifeNoteDao {
    /**
     * 获取人生笔记数量
     *
     * @return 人生笔记数量，支持响应式更新
     */
    @Query("SELECT COUNT(*) FROM lifeNotes")
    Flowable<Integer> getLifeNoteCountFlowable();

    /**
     * 获取所有人生笔记
     *
     * @param safeKeyword  转义后的搜索关键词
     * @param filterSearch 是否启用搜索
     * @return 人生笔记列表
     */
    @Query("SELECT * FROM lifeNotes " +
            "WHERE :filterSearch = 0 " +
            "OR insight LIKE '%' || :safeKeyword || '%' ESCAPE '/' " +
            "OR elaboration LIKE '%' || :safeKeyword || '%' ESCAPE '/' " +
            "ORDER BY dateTime DESC")
    Flowable<List<LifeNoteEntity>> getAllLifeNoteFlowable(String safeKeyword, int filterSearch);

    /**
     * 通过 ID 查询人生笔记
     *
     * @param id 需要获取的笔记的 ID
     * @return 人生笔记数据实体
     */
    @Query("SELECT * FROM lifeNotes WHERE noteId = :id")
    Single<Optional<LifeNoteWithHistoryModel>> getLifeNoteOptionalSingleById(long id);

    /**
     * 通过 ID 查询人生笔记
     *
     * @param id 需要获取的笔记的 ID
     * @return 人生笔记数据实体
     */
    @Query("SELECT * FROM lifeNotes WHERE noteId = :id")
    LifeNoteEntity getLifeNoteById(long id);

    /**
     * 新增人生笔记
     *
     * @param entity 人生笔记数据实体
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insertLifeNote(LifeNoteEntity entity);

    /**
     * 更新人生笔记
     *
     * @param entity 新人生笔记实体
     */
    @Update
    void updateLifeNote(LifeNoteEntity entity);

    /**
     * 插入人生笔记修改历史记录
     *
     * @param entity 人生笔记修改历史数据
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertLifeNoteHistory(LifeNoteHistoryEntity entity);

    /**
     * 修改人生笔记的事务
     *
     * @param newLifeNote 新人生笔记数据
     */
    @Transaction
    default void modifyLifeNote(@NonNull LifeNoteEntity newLifeNote) {
        //获取旧数据
        long noteId = newLifeNote.getNoteId();
        LifeNoteEntity oldLifeNote = getLifeNoteById(noteId);

        //生成修改历史记录
        String oldInsight = oldLifeNote.getInsight();
        String oldElaboration = oldLifeNote.getElaboration();
        LocalDateTime oldDateTime = oldLifeNote.getDateTime();
        LifeNoteHistoryEntity historyEntity = new LifeNoteHistoryEntity(noteId, oldInsight, oldElaboration, oldDateTime);
        insertLifeNoteHistory(historyEntity);

        //更新人生笔记
        updateLifeNote(newLifeNote);
    }
}
