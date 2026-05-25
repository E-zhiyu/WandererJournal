package com.wanderer.journal.data.save.db.entities.composite;

import java.time.LocalDate;

public class DiaryParagraphCountModel {
    private LocalDate diaryDate;    //日记日期
    private int paragraphWordCount; //段落字符数量

    public DiaryParagraphCountModel(LocalDate diaryDate, int paragraphWordCount) {
        this.diaryDate = diaryDate;
        this.paragraphWordCount = paragraphWordCount;
    }

    public LocalDate getDiaryDate() {
        return diaryDate;
    }

    public void setDiaryDate(LocalDate diaryDate) {
        this.diaryDate = diaryDate;
    }

    public int getParagraphWordCount() {
        return paragraphWordCount;
    }

    public void setParagraphWordCount(int paragraphWordCount) {
        this.paragraphWordCount = paragraphWordCount;
    }
}
