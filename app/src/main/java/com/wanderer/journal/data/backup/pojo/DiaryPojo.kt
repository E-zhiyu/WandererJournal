package com.wanderer.journal.data.backup.pojo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 日记数据的备份 POJO。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class DiaryPojo(
    val diaryId: Long = 0,      // 日记编号
    val diaryDate: Long = 0,    // 时间戳
)
