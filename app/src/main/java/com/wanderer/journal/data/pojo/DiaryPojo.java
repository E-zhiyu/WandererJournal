package com.wanderer.journal.data.pojo;

public class DiaryPojo {
    long diaryId;       //日记编号
    long timeMillis;    //时间戳

    public DiaryPojo() {
    }

    public long getDiaryId() {
        return diaryId;
    }

    public void setDiaryId(long diaryId) {
        this.diaryId = diaryId;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }
}
