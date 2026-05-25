package com.wanderer.journal.data.save.db.entities.composite;

import java.time.LocalDate;

public class DiaryLengthModel {
    private LocalDate diaryDate;    //日记日期
    private int diaryLength;        //日记长度

    public DiaryLengthModel(LocalDate diaryDate, int diaryLength) {
        this.diaryDate = diaryDate;
        this.diaryLength = diaryLength;
    }

    public LocalDate getDiaryDate() {
        return diaryDate;
    }

    public void setDiaryDate(LocalDate diaryDate) {
        this.diaryDate = diaryDate;
    }

    public int getDiaryLength() {
        return diaryLength;
    }

    public void setDiaryLength(int diaryLength) {
        this.diaryLength = diaryLength;
    }
}
