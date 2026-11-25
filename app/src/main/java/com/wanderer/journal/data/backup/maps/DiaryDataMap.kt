package com.wanderer.journal.data.backup.maps

import com.wanderer.journal.data.backup.pojo.DiaryPojo
import com.wanderer.journal.data.backup.pojo.EmotionParagraphRefPojo
import com.wanderer.journal.data.backup.pojo.EmotionTagPojo
import com.wanderer.journal.data.backup.pojo.LocationPojo
import com.wanderer.journal.data.backup.pojo.MediaPojo
import com.wanderer.journal.data.backup.pojo.ParagraphPojo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * 一次日记数据备份中包含的所有数据集合。
 */
@Serializable
@JsonIgnoreUnknownKeys
data class DiaryDataMap(
    val diaryList: List<DiaryPojo>? = null,                             // 日记列表
    val paragraphList: List<ParagraphPojo>? = null,                     // 段落列表
    val mediaList: List<MediaPojo>? = null,                             // 媒体文件列表
    val locationList: List<LocationPojo>? = null,                       // 段落位置信息
    val emotionTagList: List<EmotionTagPojo>? = null,                   // 情绪标签列表
    val emotionParagraphRefList: List<EmotionParagraphRefPojo>? = null, // 情绪标签与段落关系列表
)
