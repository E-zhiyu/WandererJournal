package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(
        tableName = "diaries",
        indices = {@Index(value = "diaryDate", unique = true)}
)
public class DiaryEntity {
    @PrimaryKey(autoGenerate = true)
    private long diaryId;        //自增主键
    private LocalDate diaryDate; //日记的日期

    /**
     * 日记实体构造方法
     *
     * @param diaryDate 日记的日期
     */
    public DiaryEntity(LocalDate diaryDate) {
        this.diaryDate = diaryDate;
    }

    public long getDiaryId() {
        return diaryId;
    }

    public void setDiaryId(long diaryId) {
        this.diaryId = diaryId;
    }

    public LocalDate getDiaryDate() {
        return diaryDate;
    }

    public void setDiaryDate(LocalDate diaryDate) {
        this.diaryDate = diaryDate;
    }
}
