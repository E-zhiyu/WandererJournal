package com.wanderer.journal.data.backup.pojo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 媒体文件的备份 POJO。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class MediaPojo(
    val mediaId: Long = 0,              // 媒体文件 ID
    val parentParagraphId: Long = 0,    // 所属段落 ID
    val fileUri: String? = null,        // 文件路径 Uri 字符串
)
