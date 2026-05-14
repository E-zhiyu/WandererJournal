package com.wanderer.journal.data.backup.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class EmotionParagraphRefPojo {
    private long paragraphId;   //段落 ID
    private long emotionId;     //情绪标签 ID
    private int degree;         //情绪的程度指数

    public EmotionParagraphRefPojo() {
    }

    public long getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(long paragraphId) {
        this.paragraphId = paragraphId;
    }

    public long getEmotionId() {
        return emotionId;
    }

    public void setEmotionId(long emotionId) {
        this.emotionId = emotionId;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }
}
