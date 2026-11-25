package com.wanderer.journal.data.backup.maps

import com.wanderer.journal.data.backup.pojo.RoleAliaPojo
import com.wanderer.journal.data.backup.pojo.RolePojo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 一次角色数据备份中包含的所有数据集合。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class RoleDataMap(
    val roleList: List<RolePojo>? = null,           // 角色列表
    val roleAliaList: List<RoleAliaPojo>? = null,   // 角色别称
)
