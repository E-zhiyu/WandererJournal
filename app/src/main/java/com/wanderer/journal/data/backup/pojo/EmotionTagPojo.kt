package com.wanderer.journal.data.backup.pojo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 情绪标签的备份 POJO。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class EmotionTagPojo(
    val emotionId: Long = 0,        // 自增主键
    val name: String? = null,       // 名称
    val description: String? = null, // 描述
    val type: Int = 1,              // 情绪种类
)
