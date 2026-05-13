package com.wanderer.journal.data.save.db.entities.composite;


import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.ref.EmotionParagraphCrossRef;

import java.util.List;

public class EmotionTagWithParagraph {
    @Embedded
    private EmotionTagEntity emotionTag;    //情绪标签实体
    @Relation(
            parentColumn = "emotionId",
            entityColumn = "paragraphId",
            associateBy = @Junction(
                    value = EmotionParagraphCrossRef.class,
                    parentColumn = "emotionId",
                    entityColumn = "paragraphId"
            ),
            entity = ParagraphEntity.class,
            projection = {"paragraphId"}
    )
    private List<Long> paragraphIdList;     //用了该标签的段落编号列表

    public EmotionTagWithParagraph() {
    }

    public List<Long> getParagraphIdList() {
        return paragraphIdList;
    }

    public void setParagraphIdList(List<Long> paragraphIdList) {
        this.paragraphIdList = paragraphIdList;
    }

    public EmotionTagEntity getEmotionTag() {
        return emotionTag;
    }

    public void setEmotionTag(EmotionTagEntity emotionTag) {
        this.emotionTag = emotionTag;
    }
}
