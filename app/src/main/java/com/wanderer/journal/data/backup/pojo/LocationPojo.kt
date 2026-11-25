package com.wanderer.journal.data.backup.pojo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 段落位置信息的备份 POJO。
 *
 * 原先的 Java Bean 使用 `isFineLocation()` 作为布尔字段的 getter，
 * Jackson 会将其序列化为 `fineLocation`，因此这里通过 [SerialName] 保持与原备份文件兼容。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class LocationPojo(
    val paragraphId: Long = 0,          // 段落编号（外键）
    val latitude: Double = 0.0,         // 纬度
    val longitude: Double = 0.0,        // 经度
    val altitude: Double = 0.0,         // 海拔
    @SerialName("fineLocation")
    val isFineLocation: Boolean = true, // 是否为精确位置
    val provider: String = "",          // 位置提供者
)
