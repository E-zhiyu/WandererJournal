package com.wanderer.journal.data.io.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class ParagraphPojo {
    long paragraphId;   //段落ID
    long parentDiaryId; //父日记ID
    long createTime;    //创建时间戳
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

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
