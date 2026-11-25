package com.wanderer.journal.data.backup.pojo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 人生笔记历史记录的备份 POJO。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class LifeNoteHistoryPojo(
    val historyId: Long = 0,
    val noteId: Long = 0,
    val insight: String? = null,
    val elaboration: String? = null,
    val updateDateTime: Long = 0,
)
