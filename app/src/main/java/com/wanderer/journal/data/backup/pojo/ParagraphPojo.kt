package com.wanderer.journal.data.backup.pojo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 日记段落的备份 POJO。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class ParagraphPojo(
    val paragraphId: Long = 0,      // 段落 ID
    val parentDiaryId: Long = 0,    // 父日记 ID
    val createTime: Long = 0,       // 创建时间戳
    val content: String? = null,    // 内容
    val privacyType: Int = 0,       // 隐私种类
)
