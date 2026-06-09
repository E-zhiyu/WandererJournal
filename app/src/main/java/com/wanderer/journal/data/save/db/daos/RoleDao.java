package com.wanderer.journal.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.wanderer.journal.data.save.db.entities.composite.RoleEntityModel;

import java.util.List;

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

    /**
     * 获取所有角色数据并按照关系由近到远排序
     * @return 排序后的角色数据
     */
    @Transaction
    @Query("SELECT * FROM roles ORDER BY relationship DESC")
    Flowable<List<RoleEntityModel>> getAllRoleFlowable();
}
