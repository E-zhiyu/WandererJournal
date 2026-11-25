package com.wanderer.journal.data.backup.pojo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 情绪标签与段落关系的备份 POJO。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class EmotionParagraphRefPojo(
    val paragraphId: Long = 0,  // 段落 ID
    val emotionId: Long = 0,    // 情绪标签 ID
    val degree: Int = 0,        // 情绪的程度指数
)
