package com.wanderer.journal.data.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class ParagraphPojo {
    long paragraphId;   //段落ID
    long parentDiaryId; //父日记ID
    long timeMillis;    //时间戳
    String content;     //内容

    public ParagraphPojo() {
    }

    public long getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(long paragraphId) {
        this.paragraphId = paragraphId;
    }

    public long getParentDiaryId() {
        return parentDiaryId;
    }

    public void setParentDiaryId(long parentDiaryId) {
        this.parentDiaryId = parentDiaryId;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
