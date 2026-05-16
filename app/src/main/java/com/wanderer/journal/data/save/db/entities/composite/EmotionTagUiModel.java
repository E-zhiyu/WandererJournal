package com.wanderer.journal.data.save.db.entities.composite;

import androidx.room.Embedded;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;

/**
 * 情绪标签选择 Item
 */
public class EmotionTagUiModel {
    @Embedded
    private EmotionTagEntity emotionTag;    //情绪标签实例

    private boolean isChecked;              //是否选中

    private int degree;                     //如果被选中，该情绪的程度是多少

    public EmotionTagUiModel() {
    }

    public EmotionTagEntity getEmotionTag() {
        return emotionTag;
    }

    public void setEmotionTag(EmotionTagEntity emotionTag) {
        this.emotionTag = emotionTag;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }
}
