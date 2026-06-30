package com.wanderer.journal.data.save.db.entities.composite;

import androidx.room.Embedded;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;

public class EmotionTagUseCountModel {
    @Embedded
    private EmotionTagEntity emotionTag;
    private final int useCount;

    public EmotionTagUseCountModel(EmotionTagEntity emotionTag, int useCount) {
        this.emotionTag = emotionTag;
        this.useCount = useCount;
    }

    public EmotionTagEntity getEmotionTag() {
        return emotionTag;
    }

    public void setEmotionTag(EmotionTagEntity emotionTag) {
        this.emotionTag = emotionTag;
    }

    public int getUseCount() {
        return useCount;
    }
}
