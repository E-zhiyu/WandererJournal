package com.wanderer.journal.data.save.db.entities.composite.union;

import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Relation;

import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.LocationEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

import java.util.List;

public class ParagraphUnionModel {
    @Embedded
    private final ParagraphEntity paragraph;                  //段落实体
    @Relation(
            parentColumn = "paragraphId",
            entityColumn = "paragraphId",
            entity = EmotionParagraphRefEntity.class
    )
    private final List<EmotionTagRefUnionModel> emotionList;  //情绪标签（带情绪程度）列表
    @Relation(
            parentColumn = "paragraphId",
            entityColumn = "parentParagraphId",
            entity = MediaEntity.class
    )
    private List<MediaEntity> mediaList;                //媒体列表
    @Nullable
    @Relation(
            parentColumn = "paragraphId",
            entityColumn = "paragraphId",
            entity = LocationEntity.class
    )
    private final LocationEntity location;                    //段落位置信息

    public ParagraphUnionModel(ParagraphEntity paragraph, List<EmotionTagRefUnionModel> emotionList, @Nullable LocationEntity location) {
        this.paragraph = paragraph;
        this.emotionList = emotionList;
        this.location = location;
    }

    public ParagraphEntity getParagraph() {
        return paragraph;
    }

    public List<EmotionTagRefUnionModel> getEmotionList() {
        return emotionList;
    }

    public List<MediaEntity> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<MediaEntity> mediaList) {
        this.mediaList = mediaList;
    }

    @Nullable
    public LocationEntity getLocation() {
        return location;
    }
}
