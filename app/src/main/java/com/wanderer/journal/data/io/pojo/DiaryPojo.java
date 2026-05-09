package com.wanderer.journal.data.io.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class DiaryPojo {
    long diaryId;       //日记编号
    long diaryDate;     //时间戳

    public DiaryPojo() {
    }

    public long getDiaryId() {
        return diaryId;
    }

    public void setDiaryId(long diaryId) {
        this.diaryId = diaryId;
    }

    public long getDiaryDate() {
        return diaryDate;
    }

    public void setDiaryDate(long diaryDate) {
        this.diaryDate = diaryDate;
    }
}
