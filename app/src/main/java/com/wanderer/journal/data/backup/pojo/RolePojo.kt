package com.wanderer.journal.data.backup.pojo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 角色数据的备份 POJO。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class RolePojo(
    val roleId: Long = 0,
    val name: String? = null,
    val displayName: String = "",
    val identity: String? = null,
    val impression: String? = null,
    val relationship: Int = 0,
    val useCount: Int = 0,
    val latestUseTime: Long = 0,
)
