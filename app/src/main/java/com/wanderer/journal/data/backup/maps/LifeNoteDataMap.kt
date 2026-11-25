package com.wanderer.journal.data.backup.maps

import com.wanderer.journal.data.backup.pojo.LifeNoteHistoryPojo
import com.wanderer.journal.data.backup.pojo.LifeNotePojo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 一次人生笔记数据备份中包含的所有数据集合。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class LifeNoteDataMap(
    val lifeNoteList: List<LifeNotePojo>? = null,               // 人生笔记
    val lifeNoteHistoryList: List<LifeNoteHistoryPojo>? = null, // 人生笔记历史
)
