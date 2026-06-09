package com.wanderer.journal.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Query;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface RoleDao {
    /**
     * 查询角色数量
     *
     * @return 角色数量，支持响应式更新
     */
    @Query("SELECT COUNT(*) FROM roles")
    Flowable<Integer> getRoleCountFlowable();
}
