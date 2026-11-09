package com.wanderer.journal.ui.pages.emotion;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;

public class EmotionTagInputViewModel extends ViewModel {
    private final MutableLiveData<EmotionTagEntity> emotionTagLiveData = new MutableLiveData<>();   //缓存的数据

    public MutableLiveData<EmotionTagEntity> getEmotionTagLiveData() {
        return emotionTagLiveData;
    }

    @Nullable
    public EmotionTagEntity getEmotionTag() {
        return emotionTagLiveData.getValue();
    }

    public void setEmotionTag(EmotionTagEntity emotionTag) {
        emotionTagLiveData.setValue(emotionTag);
    }
}
