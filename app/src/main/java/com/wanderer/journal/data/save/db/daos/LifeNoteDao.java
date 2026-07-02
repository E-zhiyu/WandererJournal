package com.wanderer.journal.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Query;

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
}
