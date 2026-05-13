package com.wanderer.journal.data.backup.pojo;

import java.util.List;

public class EmotionTagPojo {
    private long emotionId;             //自增主键
    private String name;                //名称
    private String description;         //描述
    private List<Long> paragraphIdList; //用了该标签的段落编号列表

    public EmotionTagPojo() {
    }

    public long getEmotionId() {
        return emotionId;
    }

    public void setEmotionId(long emotionId) {
        this.emotionId = emotionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getParagraphIdList() {
        return paragraphIdList;
    }

    public void setParagraphIdList(List<Long> paragraphIdList) {
        this.paragraphIdList = paragraphIdList;
    }
}
