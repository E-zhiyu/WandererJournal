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
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

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
     * 获取符合搜索内容的角色数据并按照关系由近到远排序
     *
     * @return 排序后的角色数据
     */
    @Transaction
    @Query("SELECT * FROM roles " +
            "WHERE :filterSearchKeyword = 0 OR " +
            "name LIKE '%'||:keyword||'%' OR " +
            "impression LIKE '%'||:keyword||'%' OR " +
            "identity LIKE '%'||:keyword||'%' OR " +
            "roleId IN (SELECT roleId FROM roleAlias WHERE alia LIKE '%'||:keyword||'%') " +
            "ORDER BY relationship DESC")
    Flowable<List<RoleEntityModel>> getAllRoleFlowable(String keyword, int filterSearchKeyword);

    /**
     * 查询所有角色数据，并按照关系由近到远排序
     *
     * @return 所有角色组成的列表
     */
    @Query("SELECT * FROM roles ORDER BY relationship DESC")
    Single<List<RoleEntity>> getAllRoleSingle();

    /**
     * 通过角色 ID 获取角色数据
     *
     * @param id 角色 ID
     * @return 该 ID 对应的角色数据，可能为空
     */
    @Transaction
    @Query("SELECT * FROM roles WHERE roleId = :id")
    Single<Optional<RoleEntityModel>> getRoleAndAliasSingleById(long id);

    /**
     * 插入一条角色记录
     *
     * @param role 新角色记录
     * @return 插入的角色记录的主键值
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long insertRole(RoleEntity role);

    /**
     * 插入角色别名记录
     *
     * @param aliaEntityList 别名列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertRoleAlias(List<RoleAliaEntity> aliaEntityList);

    /**
     * 更新角色
     *
     * @param role 更新后的角色数据
     */
    @Update
    void updateRole(RoleEntity role);

    /**
     * 通过角色 ID 删除别名
     *
     * @param roleId 需要删除的别名的角色 ID
     */
    @Query("DELETE FROM roleAlias WHERE roleId = :roleId")
    void deleteAliasByRoleId(long roleId);

    /**
     * 更新段落中引用的角色的名称
     *
     * @param oldName 旧名称
     * @param newName 新名称
     * @param roleId  角色 ID
     */
    @Query("UPDATE paragraphs SET content = REPLACE(content, '[role_ref:@' || :oldName || '](' || :roleId || ')', '[role_ref:@' || :newName || '](' || :roleId || ')') " +
            "WHERE content LIKE '%[role_ref:@' || :oldName || '](' || :roleId || ')%'")
    void renameInParagraph(String oldName, String newName, long roleId);

    /**
     * 通过角色 ID 获取角色名称
     *
     * @param roleId 角色 ID
     * @return 该 ID 对应的角色名称
     */
    @Query("SELECT name FROM roles WHERE roleId = :roleId")
    String getRoleNameById(long roleId);

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

        //获取角色的旧数据
        String oldName = getRoleNameById(roleId);

        //更新角色
        updateRole(role);

        //先删除旧的别名
        deleteAliasByRoleId(roleId);

        //再添加新的别名
        List<RoleAliaEntity> aliaEntityList = aliaList.stream()
                .map(alia -> new RoleAliaEntity(alia, roleId))
                .collect(Collectors.toList());
        insertRoleAlias(aliaEntityList);

        //更新角色引用文本（仅新旧名称不一致时）
        if (!oldName.equals(role.getName())) {
            renameInParagraph(oldName, role.getName(), roleId);
        }
    }

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
     * 删除角色
     *
     * @param role 待删除的角色
     */
    @Delete
    void deleteRole(RoleEntity role);

    /**
     * 将某个角色的引用冲刷为普通文本
     *
     * @param roleId   需要冲刷的角色 ID
     * @param roleName 冲刷后显示的橘色名称
     */
    @Query("UPDATE paragraphs SET content = REPLACE(content, '[role_ref:@'|| :roleName || '](' || :roleId || ')', '@' || :roleName) " +
            "WHERE content LIKE '%[role_ref:@%](' || :roleId || ')%'")
    void washRoleRefInParagraph(long roleId, String roleName);

    /**
     * 删除角色并将角色引用文本冲刷为普通文本
     *
     * @param role 需要删除的角色
     */
    @Transaction
    default void deleteRoleAndWash(@NonNull RoleEntity role) {
        washRoleRefInParagraph(role.getRoleId(), role.getName());
        deleteRole(role);
    }
}
