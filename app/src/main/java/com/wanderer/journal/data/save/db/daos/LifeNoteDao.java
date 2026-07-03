package com.wanderer.journal.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Query;

import com.wanderer.journal.data.save.db.entities.LifeNoteEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

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
}
