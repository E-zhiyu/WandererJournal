package com.wanderer.journal.data.save.db.entities.composite.ui;

import androidx.room.Embedded;

import com.wanderer.journal.data.save.db.entities.DiaryEntity;

public class DiaryListUiModel {
    @Embedded
    private DiaryEntity diary;          //日记实体
    private String paragraphFragment;   //首段摘要
    private int charCount;              //日记字数

    public DiaryListUiModel() {
    }

    public DiaryEntity getDiary() {
        return diary;
    }

    public void setDiary(DiaryEntity diary) {
        this.diary = diary;
    }

    public String getParagraphFragment() {
        return paragraphFragment;
    }

    public void setParagraphFragment(String paragraphFragment) {
        this.paragraphFragment = paragraphFragment;
    }

    public int getCharCount() {
        return charCount;
    }

    public void setCharCount(int charCount) {
        this.charCount = charCount;
    }
}
