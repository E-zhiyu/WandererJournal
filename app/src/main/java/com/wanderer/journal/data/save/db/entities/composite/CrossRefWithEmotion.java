package com.wanderer.journal.data.save.db.entities.composite;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.helpers.text.TextHelper;

import java.util.Locale;

public class CrossRefWithEmotion {
    @Embedded
    private final EmotionParagraphRefEntity crossRef;   // 包含 degree, paragraphId, emotionId等信息

    @Relation(
            parentColumn = "emotionId",  // 中间表关联标签的 ID
            entityColumn = "emotionId"   // 标签表的主键 ID
    )
    private final EmotionTagEntity emotionTag;          // 情绪标签的具体信息（名称等）

    public CrossRefWithEmotion(EmotionParagraphRefEntity crossRef, EmotionTagEntity emotionTag) {
        this.crossRef = crossRef;
        this.emotionTag = emotionTag;
    }

    public EmotionTagEntity getEmotionTag() {
        return emotionTag;
    }

    /**
     * 生成显示文本（名称 + 罗马数字）
     *
     * @return 显示文本
     */
    public String generateDisplayText() {
        String name = emotionTag.getName();
        int degree = crossRef.getDegree();
        return String.format(
                Locale.getDefault(),
                "%s %s",
                name, TextHelper.toRoman(degree)
        );
    }
}
