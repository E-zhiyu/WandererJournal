package com.wanderer.journal.data.backup.pojo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 人生笔记的备份 POJO。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class LifeNotePojo(
    val noteId: Long = 0,
    val insight: String? = null,
    val elaboration: String? = null,
    val dateTime: Long = 0,
)
