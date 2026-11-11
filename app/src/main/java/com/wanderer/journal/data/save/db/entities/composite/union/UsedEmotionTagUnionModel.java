package com.wanderer.journal.data.save.db.entities.composite.union;

import androidx.room.Embedded;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;

public class UsedEmotionTagUnionModel {
    @Embedded
    private final EmotionTagEntity emotionTag;
    private final int useCount;

    public UsedEmotionTagUnionModel(EmotionTagEntity emotionTag, int useCount) {
        this.emotionTag = emotionTag;
        this.useCount = useCount;
    }

    public EmotionTagEntity getEmotionTag() {
        return emotionTag;
    }

    public int getUseCount() {
        return useCount;
    }
}
