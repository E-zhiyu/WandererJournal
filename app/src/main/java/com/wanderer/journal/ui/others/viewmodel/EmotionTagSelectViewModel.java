package com.wanderer.journal.ui.others.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;

public class EmotionTagSelectViewModel extends ViewModel {
    private long paragraphId;   //正在选择情绪标签的段落编号
    private int degree;         //情绪标签的强烈程度
    private boolean isChecked;  //是否选中
    private final MutableLiveData<EmotionTagEntity> checkedEmotionTag = new MutableLiveData<>();    //当前选中的情绪标签

    public long getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(long paragraphId) {
        this.paragraphId = paragraphId;
    }

    public MutableLiveData<EmotionTagEntity> getCheckedEmotionTag() {
        return checkedEmotionTag;
    }

    /**
     * 设置当前的情绪标签
     *
     * @param checkedEmotionTag 情绪标签实体
     * @param isChecked         是否选中
     */
    public void setCheckedEmotionTag(EmotionTagEntity checkedEmotionTag, boolean isChecked, int degree) {
        this.degree = isChecked ? degree : 1;
        this.isChecked = isChecked;
        this.checkedEmotionTag.setValue(checkedEmotionTag);
    }

    public int getDegree() {
        return degree;
    }

    public boolean isChecked() {
        return isChecked;
    }
}
