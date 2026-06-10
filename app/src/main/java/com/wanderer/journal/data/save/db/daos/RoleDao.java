package com.wanderer.journal.data.save.db.daos;

import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.wanderer.journal.data.save.db.entities.RoleAliaEntity;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.db.entities.composite.RoleEntityModel;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
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
     *
     * @return 排序后的角色数据
     */
    @Transaction
    @Query("SELECT * FROM roles ORDER BY relationship DESC")
    Flowable<List<RoleEntityModel>> getAllRoleFlowable();

    /**
     * 插入一条角色记录
     *
     * @param role 新角色记录
     * @return 插入的角色记录的主键值
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long insertRole(RoleEntity role);

    @Update
    void updateRole(RoleEntity role);

    /**
     * 插入角色别名记录
     *
     * @param aliaEntityList 别名列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertRoleAlias(List<RoleAliaEntity> aliaEntityList);

    /**
     * 通过角色 ID 删除别名
     *
     * @param roleId 需要删除的别名的角色 ID
     */
    @Query("DELETE FROM roleAlias WHERE roleId = :roleId")
    void deleteAliasByRoleId(long roleId);

    /**
     * 添加角色事务
     *
     * @param role     新角色
     * @param aliaList 该角色的别名列表
     */
    @Transaction
    default void addRole(RoleEntity role, @NonNull List<String> aliaList) {
        Long roleId = insertRole(role);
        List<RoleAliaEntity> aliaEntityList = aliaList.stream()
                .map(alia -> new RoleAliaEntity(alia, roleId))
                .collect(Collectors.toList());
        insertRoleAlias(aliaEntityList);
    }

    /**
     * 更新角色事务
     *
     * @param role     修改后的角色（主键不能为0）
     * @param aliaList 角色别名列表
     */
    @Transaction
    default void updateRole(@NonNull RoleEntity role, List<String> aliaList) {
        long roleId = role.getRoleId();
        if (roleId == 0) throw new SQLiteException("角色主键无效，无法更新");

        //更新角色
        updateRole(role);

        //先删除旧的别名
        deleteAliasByRoleId(roleId);

        //再添加新的别名
        List<RoleAliaEntity> aliaEntityList = aliaList.stream()
                .map(alia -> new RoleAliaEntity(alia, roleId))
                .collect(Collectors.toList());
        insertRoleAlias(aliaEntityList);
    }

    /**
     * 删除角色
     *
     * @param role 待删除的角色
     * @return 是否完成
     */
    @Delete
    Completable deleteRole(RoleEntity role);

    /**
     * 导出角色数据
     *
     * @return 角色列表
     */
    @Query("SELECT * FROM roles")
    List<RoleEntity> exportRoleData();

    /**
     * 导出角色别称数据
     *
     * @return 角色别称列表
     */
    @Query("SELECT * FROM roleAlias")
    List<RoleAliaEntity> exportRoleAliaData();

    /**
     * 清空角色表
     */
    @Query("DELETE FROM roles")
    void clearRole();

    /**
     * 清空角色别名表
     */
    @Query("DELETE FROM roleAlias")
    void clearRoleAlia();

    /**
     * 导入角色数据
     *
     * @param roleList 角色列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void importRoleData(List<RoleEntity> roleList);

    /**
     * 导入角色别名数据
     *
     * @param roleAliaEntityList 角色别名列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void importRoleAliaList(List<RoleAliaEntity> roleAliaEntityList);
}
