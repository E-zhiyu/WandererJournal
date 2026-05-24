package com.wanderer.journal.data.save.db.entities.composite;

import java.time.LocalDate;

public class DiaryParagraphCountModel {
    private LocalDate diaryDate;    //日记日期
    private int paragraphCount;     //段落数量

    public DiaryParagraphCountModel(LocalDate diaryDate, int paragraphCount) {
        this.diaryDate = diaryDate;
        this.paragraphCount = paragraphCount;
    }

    public LocalDate getDiaryDate() {
        return diaryDate;
    }

    public void setDiaryDate(LocalDate diaryDate) {
        this.diaryDate = diaryDate;
    }

    public int getParagraphCount() {
        return paragraphCount;
    }

    public void setParagraphCount(int paragraphCount) {
        this.paragraphCount = paragraphCount;
    }
}
