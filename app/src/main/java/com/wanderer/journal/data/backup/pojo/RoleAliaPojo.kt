package com.wanderer.journal.data.backup.pojo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 角色别名的备份 POJO。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class RoleAliaPojo(
    val aliaId: Long = 0,
    val roleId: Long = 0,
    val alia: String? = null,
)
