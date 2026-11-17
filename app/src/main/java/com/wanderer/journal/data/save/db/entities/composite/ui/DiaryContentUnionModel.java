package com.wanderer.journal.data.save.db.entities.composite.ui;

import androidx.room.Embedded;

import com.wanderer.journal.data.save.db.entities.DiaryEntity;

public class DiaryContentUnionModel {
    @Embedded
    private final DiaryEntity diary;        //日记实体
    private final String paragraphFragment; //首段摘要
    private final int charCount;            //日记字数


    public DiaryContentUnionModel(DiaryEntity diary, String paragraphFragment, int charCount) {
        this.diary = diary;
        this.paragraphFragment = paragraphFragment;
        this.charCount = charCount;
    }

    public DiaryEntity getDiary() {
        return diary;
    }

    public String getParagraphFragment() {
        return paragraphFragment;
    }

    public int getCharCount() {
        return charCount;
    }
}
