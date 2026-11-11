package com.wanderer.journal.ui.others.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

public class EmotionTagSelectViewModel extends ViewModel {
    @Nullable
    private Long paragraphId;   //正在选择情绪标签的段落编号
    private final MutableLiveData<Map<Long, Integer>> emotionTagSelectLiveData = new MutableLiveData<>();   //情绪标签选择状况

    @Nullable
    public Long getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(@Nullable Long paragraphId) {
        this.paragraphId = paragraphId;
    }

    public MutableLiveData<Map<Long, Integer>> getEmotionTagSelectLiveData() {
        return emotionTagSelectLiveData;
    }

    @NonNull
    public Map<Long, Integer> getEmotionTagSelectMap() {
        Map<Long, Integer> value = emotionTagSelectLiveData.getValue();
        //TODO:呼出选择菜单时填充LiveData，要注意处理填充时的观察者回调误触发BUG
        return value == null ? new HashMap<>() : value;
    }

    public void setEmotionTagSelectMap(Map<Long, Integer> map) {
        emotionTagSelectLiveData.setValue(map);
    }
}
