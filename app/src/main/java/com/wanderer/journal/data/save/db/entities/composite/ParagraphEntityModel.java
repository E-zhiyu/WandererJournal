package com.wanderer.journal.data.save.db.entities.composite;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

import java.util.List;

public class ParagraphEntityModel {
    @Embedded
    private ParagraphEntity paragraph;                  // 段落实体
    @Relation(
            parentColumn = "paragraphId",
            entityColumn = "paragraphId",
            entity = EmotionParagraphRefEntity.class
    )
    private List<CrossRefWithEmotion> emotionList;      // 情绪标签（带情绪程度）列表
    @Relation(
            parentColumn = "paragraphId",
            entityColumn = "parentParagraphId",
            entity = MediaEntity.class
    )
    private List<MediaEntity> mediaList;                //媒体列表

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

    public List<MediaEntity> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<MediaEntity> mediaList) {
        this.mediaList = mediaList;
    }
}
