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

import io.reactivex.rxjava3.core.Completable;
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
     * @param safeKeyword         搜索关键词
     * @param filterSearchKeyword 是否启用搜索过滤
     * @return 排序后的角色数据
     */
    @Transaction
    @Query("SELECT * FROM roles " +
            "WHERE :filterSearchKeyword = 0 " +
            "OR name LIKE '%' || :safeKeyword || '%' ESCAPE '/' " +
            "OR identity LIKE '%' || :safeKeyword || '%' ESCAPE '/' " +
            "OR impression LIKE '%' || :safeKeyword || '%' ESCAPE '/' " +
            // 将 IN 子查询改为高效的 EXISTS 关联查询，并在 SQL 层面指定转义符
            "OR EXISTS (SELECT 1 FROM roleAlias WHERE roleAlias.roleId = roles.roleId AND alia LIKE '%' || :safeKeyword || '%' ESCAPE '/') " +
            "ORDER BY relationship DESC")
    Flowable<List<RoleEntityModel>> getAllRoleWithSearchFlowable(String safeKeyword, int filterSearchKeyword);

    /**
     * 查询所有角色数据，并按照关系由近到远排序
     *
     * @return 所有角色组成的列表
     */
    @Query("SELECT * FROM roles ORDER BY relationship DESC")
    Single<List<RoleEntity>> getAllRoleSingle();

    /**
     * 获取最常用的几个角色
     *
     * @return 最常用的几个角色的列表
     */
    @Query("SELECT * FROM roles WHERE useCount > 0 ORDER BY useCount LIMIT 7")
    Flowable<List<RoleEntity>> getCommonRoleFlowable();

    /**
     * 将角色使用次数加一
     *
     * @param roleId 需要增加使用次数的角色 ID
     * @return 是否完成
     */
    @Query("UPDATE roles SET useCount = useCount + 1 WHERE roleId = :roleId")
    Completable addRoleUseCount(long roleId);

    /**
     * 清空某个角色的使用次数
     *
     * @param roleId 需要清空使用次数的角色 ID
     * @return 是否完成
     */
    @Query("UPDATE roles SET useCount = 0 WHERE roleId = :roleId")
    Completable clearRoleUseCount(long roleId);

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
     * @param oldRefName 旧名称
     * @param newRefName 新名称
     * @param roleId     角色 ID
     */
    @Query("UPDATE paragraphs SET content = REPLACE(content, '[role_ref:@' || :oldRefName || '](' || :roleId || ')', '[role_ref:@' || :newRefName || '](' || :roleId || ')') " +
            "WHERE content LIKE '%[role_ref:@' || :oldRefName || '](' || :roleId || ')%'")
    void renameInParagraph(String oldRefName, String newRefName, long roleId);

    /**
     * 通过角色 ID 获取角色数据
     *
     * @param roleId 角色 ID
     * @return 该 ID 对应的角色数据
     */
    @Query("SELECT * FROM roles WHERE roleId = :roleId")
    RoleEntity getRoleById(long roleId);

    /**
     * 获取同名橘色数量
     *
     * @param name   角色名称
     * @param roleId 角色编号，用于排除自身
     * @return 同名角色的数量
     */
    @Query("SELECT COUNT(*) FROM roles WHERE name = :name AND roleId != :roleId")
    Single<Integer> getRoleCountWithSameNameSingle(String name, long roleId);

    /**
     * 更新角色事务
     *
     * @param role     修改后的角色（主键不能为0）
     * @param aliaList 角色别名列表
     */
    @Transaction
    default void updateRoleAndAlia(@NonNull RoleEntity role, List<String> aliaList) {
        long roleId = role.getRoleId();
        if (roleId == 0) throw new SQLiteException("角色主键无效，无法更新");

        //获取角色的旧数据
        RoleEntity oldRole = getRoleById(roleId);
        String oldName = oldRole.getName();
        String oldDisplayName = oldRole.getDisplayName();

        //更新角色
        updateRole(role);

        //先删除旧的别名
        deleteAliasByRoleId(roleId);

        //再添加新的别名
        List<RoleAliaEntity> aliaEntityList = aliaList.stream()
                .map(alia -> new RoleAliaEntity(alia, roleId))
                .collect(Collectors.toList());
        insertRoleAlias(aliaEntityList);

        //更新角色引用文本（引用文本变化时）
        String oldRefName = oldDisplayName.isEmpty() ? oldName : oldDisplayName;
        String newRefName = role.getDisplayName().isEmpty() ? role.getName() : role.getDisplayName();
        if (!oldRefName.equals(newRefName)) {
            renameInParagraph(oldRefName, newRefName, roleId);
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
