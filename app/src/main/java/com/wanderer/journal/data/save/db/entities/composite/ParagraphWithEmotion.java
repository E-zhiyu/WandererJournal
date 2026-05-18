package com.wanderer.journal.data.save.db.entities.composite;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

import java.util.List;

public class ParagraphWithEmotion {
    @Embedded
    private ParagraphEntity paragraph;                  // 段落实体

    @Relation(
            parentColumn = "paragraphId",               // 段落表主键
            entityColumn = "paragraphId",               // 中间表里的段落 ID
            entity = EmotionParagraphRefEntity.class    // 告诉 Room 核心去查中间表
    )
    private List<CrossRefWithEmotion> emotionList;      // 情绪标签（带情绪程度）列表

    public ParagraphEntity getParagraph() {
        return paragraph;
    }

    public void setParagraph(ParagraphEntity paragraph) {
        this.paragraph = paragraph;
    }

    public List<CrossRefWithEmotion> getEmotionList() {
        return emotionList;
    }

    public void setEmotionList(List<CrossRefWithEmotion> emotionList) {
        this.emotionList = emotionList;
    }
}
