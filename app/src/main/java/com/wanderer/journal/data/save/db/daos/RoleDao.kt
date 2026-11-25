package com.wanderer.journal.data.save.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.wanderer.journal.data.save.db.entities.RoleAliaEntity
import com.wanderer.journal.data.save.db.entities.RoleEntity
import com.wanderer.journal.data.save.db.entities.composite.union.RoleEntityUnionModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import java.util.Optional
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleDao {
    /**
     * 查询角色数量。
     *
     * @return 角色数量，支持响应式更新
     */
    @Query("SELECT COUNT(*) FROM roles")
    fun getRoleCountFlow(): Flow<Int>

    /**
     * 获取符合搜索内容的角色数据并按照关系由近到远排序。
     *
     * @param safeKeyword 搜索关键词
     * @param filterSearchKeyword 是否启用搜索过滤
     * @return 排序后的角色数据
     */
    @Transaction
    @Query(
        "SELECT * FROM roles " +
            "WHERE :filterSearchKeyword = 0 " +
            "OR name LIKE '%' || :safeKeyword || '%' ESCAPE '/' " +
            "OR identity LIKE '%' || :safeKeyword || '%' ESCAPE '/' " +
            "OR impression LIKE '%' || :safeKeyword || '%' ESCAPE '/' " +
            "OR EXISTS (SELECT 1 FROM roleAlias WHERE roleAlias.roleId = roles.roleId AND alia LIKE '%' || :safeKeyword || '%' ESCAPE '/') " +
            "ORDER BY relationship DESC"
    )
    fun getAllRoleWithSearchFlowable(
        safeKeyword: String,
        filterSearchKeyword: Int,
    ): Flowable<List<RoleEntityUnionModel>>

    /**
     * 查询所有角色数据，并按照关系由近到远排序。
     *
     * @return 所有角色组成的列表
     */
    @Query("SELECT * FROM roles ORDER BY relationship DESC")
    fun getAllRoleFlowable(): Flowable<List<RoleEntity>>

    /**
     * 获取最常用的几个角色。
     *
     * @return 最常用的几个角色的列表
     */
    @Query("SELECT * FROM roles WHERE latestUseTime > 0 ORDER BY latestUseTime DESC LIMIT :limit")
    fun getCommonRoleFlowable(limit: Int): Flowable<List<RoleEntity>>

    /**
     * 将角色使用次数加一，并且将上一次使用时间调为现在。
     *
     * @param roleId 需要更新的角色 ID
     * @return 是否完成
     */
    @Query(
        "UPDATE roles SET " +
            "useCount = useCount + 1, " +
            "latestUseTime = CAST(unixepoch('subsec') AS INTEGER) " +
            "WHERE roleId = :roleId"
    )
    fun updateRoleUseData(roleId: Long): Completable

    /**
     * 清空某个角色的使用次数。
     *
     * @param roleId 需要清空使用次数的角色 ID
     * @return 是否完成
     */
    @Query("UPDATE roles SET latestUseTime = 0 WHERE roleId = :roleId")
    fun clearRoleLatestUseTime(roleId: Long): Completable

    /**
     * 通过角色 ID 获取角色数据。
     *
     * @param id 角色 ID
     * @return 该 ID 对应的角色数据，可能为空
     */
    @Transaction
    @Query("SELECT * FROM roles WHERE roleId = :id")
    fun getRoleAndAliasSingleById(id: Long): Single<Optional<RoleEntityUnionModel>>

    /**
     * 判断某个角色 ID 是否在表中。
     *
     * @param id 角色 ID
     * @return 该 ID 是否在表中
     */
    @Query("SELECT EXISTS(SELECT * FROM roles WHERE roleId = :id)")
    fun isRoleIdExists(id: Long): Single<Boolean>

    /**
     * 插入一条角色记录。
     *
     * @param role 新角色记录
     * @return 插入的角色记录的主键值
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRole(role: RoleEntity): Long?

    /**
     * 插入角色别名记录。
     *
     * @param aliaEntityList 别名列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRoleAlias(aliaEntityList: List<RoleAliaEntity>)

    /**
     * 更新角色。
     *
     * @param role 更新后的角色数据
     */
    @Update
    fun updateRole(role: RoleEntity)

    /**
     * 通过角色 ID 删除别名。
     *
     * @param roleId 需要删除的别名的角色 ID
     */
    @Query("DELETE FROM roleAlias WHERE roleId = :roleId")
    fun deleteAliasByRoleId(roleId: Long)

    /**
     * 更新段落中引用的角色的名称。
     *
     * @param oldRefName 旧名称
     * @param newRefName 新名称
     * @param roleId 角色 ID
     */
    @Query(
        "UPDATE paragraphs SET content = REPLACE(content, '[role_ref:@' || :oldRefName || '](' || :roleId || ')', '[role_ref:@' || :newRefName || '](' || :roleId || ')') " +
            "WHERE content LIKE '%[role_ref:@' || :oldRefName || '](' || :roleId || ')%'"
    )
    fun renameInParagraph(oldRefName: String, newRefName: String, roleId: Long)

    /**
     * 通过角色 ID 获取角色数据。
     *
     * @param roleId 角色 ID
     * @return 该 ID 对应的角色数据
     */
    @Query("SELECT * FROM roles WHERE roleId = :roleId")
    fun getRoleById(roleId: Long): RoleEntity?

    /**
     * 获取同名角色数量。
     *
     * @param name 角色名称
     * @param roleId 角色编号，用于排除自身
     * @return 同名角色的数量
     */
    @Query("SELECT COUNT(*) FROM roles WHERE name = :name AND roleId != :roleId")
    fun getRoleCountWithSameNameSingle(name: String, roleId: Long): Single<Int>

    /**
     * 更新角色事务。
     *
     * @param role 修改后的角色（主键不能为 0）
     * @param aliaList 角色别名列表
     */
    @Transaction
    fun updateRoleAndAlia(role: RoleEntity?, aliaList: List<String>) {
        if (role == null || role.roleId == 0L) return
        val roleId = role.roleId

        // 获取角色的旧数据
        val oldRole = getRoleById(roleId) ?: return
        val oldName = oldRole.name
        val oldDisplayName = oldRole.displayName

        // 更新角色
        role.latestUseTime = oldRole.latestUseTime
        role.useCount = oldRole.useCount
        updateRole(role)

        // 先删除旧的别名
        deleteAliasByRoleId(roleId)

        // 再添加新的别名
        val aliaEntityList = aliaList.map { alia -> RoleAliaEntity(alia, roleId) }
        insertRoleAlias(aliaEntityList)

        // 更新角色引用文本（引用文本变化时）
        val oldRefName = if (oldDisplayName.isEmpty()) oldName else oldDisplayName
        val newRefName = if (role.displayName.isEmpty()) role.name else role.displayName
        if (oldRefName != newRefName) {
            renameInParagraph(oldRefName, newRefName, roleId)
        }
    }

    /**
     * 添加角色事务。
     *
     * @param role 新角色
     * @param aliaList 该角色的别名列表
     */
    @Transaction
    fun addRole(role: RoleEntity, aliaList: List<String>?) {
        if (aliaList == null) return

        val roleId = insertRole(role) ?: return
        val aliaEntityList = aliaList.map { alia -> RoleAliaEntity(alia, roleId) }
        insertRoleAlias(aliaEntityList)
    }

    /**
     * 删除角色。
     *
     * @param role 待删除的角色
     */
    @Delete
    fun deleteRole(role: RoleEntity)

    /**
     * 将某个角色的引用冲刷为普通文本。
     *
     * @param roleId 需要冲刷的角色 ID
     * @param roleName 冲刷后显示的角色名称
     */
    @Query(
        "UPDATE paragraphs SET content = REPLACE(content, '[role_ref:@'|| :roleName || '](' || :roleId || ')', '@' || :roleName) " +
            "WHERE content LIKE '%[role_ref:@%](' || :roleId || ')%'"
    )
    fun washRoleRefInParagraph(roleId: Long, roleName: String)

    /**
     * 删除角色并将角色引用文本冲刷为普通文本。
     *
     * @param role 需要删除的角色
     */
    @Transaction
    fun deleteRoleAndWash(role: RoleEntity?) {
        if (role == null) return
        washRoleRefInParagraph(role.roleId, role.name)
        deleteRole(role)
    }
}
