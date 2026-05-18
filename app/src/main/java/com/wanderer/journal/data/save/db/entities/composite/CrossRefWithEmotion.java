package com.wanderer.journal.data.save.db.entities.composite;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;

public class CrossRefWithEmotion {
    @Embedded
    public EmotionParagraphRefEntity crossRef; // 包含 degree, paragraphId, emotionId等信息

    @Relation(
            parentColumn = "emotionId",  // 中间表关联标签的 ID
            entityColumn = "emotionId"   // 标签表的主键 ID
    )
    public EmotionTagEntity emotionTag; // 情绪标签的具体信息（名称等）
}
