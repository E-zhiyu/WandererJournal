package com.wanderer.journal.data.backup.maps

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 备份文件信息。
 *
 * [fileNames] 与 [com.wanderer.journal.auxiliary.enums.types.BackupDataType] 的枚举序数一一对应，
 * 其中的 `null` 表示对应数据类型没有导出。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class DataInfoMap(
    val appVersionCode: Long = 0,               // 备份时的应用版本号
    val fileNames: List<String?> = emptyList(), // 备份数据的文件名，null 表示该位置的数据类型没有导出
)
