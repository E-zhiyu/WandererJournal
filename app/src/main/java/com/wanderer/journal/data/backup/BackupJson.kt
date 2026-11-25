package com.wanderer.journal.data.backup

import kotlinx.serialization.json.Json

/**
 * 备份数据专用的 JSON 配置。
 *
 * - `ignoreUnknownKeys`：旧版本备份文件中可能存在当前 Kotlin 数据类没有定义的字段，
 *   这些字段无法映射为 Kotlin 对象，反序列化时直接忽略。
 * - `encodeDefaults`：保留 Jackson 的序列化行为，即使字段值与默认值相同也写入 JSON，
 *   方便旧版本应用读取新版本导出的备份文件。
 * - `coerceInputValues`：JSON 中为 `null` 的非空字段回退到 Kotlin 默认值，
 *   避免个别无法映射的字段导致整个备份文件解析失败。
 */
internal val BackupJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}
