package com.wanderer.journal.data.save.db.entities.composite;

import androidx.annotation.Nullable;
import androidx.room.Embedded;

import com.wanderer.journal.data.save.db.entities.DiaryEntity;

public class DiaryWithSummary {
    @Embedded
    private DiaryEntity diary;          //日记实体
    private String paragraphFragment;   //首段摘要
    private int paragraphCount;         //段落数量

    public DiaryWithSummary() {
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

    public int getParagraphCount() {
        return paragraphCount;
    }

    public void setParagraphCount(int paragraphCount) {
        this.paragraphCount = paragraphCount;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != DiaryWithSummary.class) {
            return false;
        }

        DiaryWithSummary target = (DiaryWithSummary) obj;
        return target.getDiary().getDiaryId() == diary.getDiaryId() &&
                target.getParagraphFragment().equals(paragraphFragment) &&
                target.getParagraphCount() == paragraphCount;
    }
}
