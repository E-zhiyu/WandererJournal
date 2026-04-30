package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.wanderer.journal.data.save.db.converters.DateTimeConverter;

import java.time.LocalDate;

@Entity(tableName = "diaries")
public class DiaryEntity {
    @PrimaryKey(autoGenerate = true)
    public long diaryId;        //自增主键
    public LocalDate diaryDate; //日记的日期

    /**
     * 日记实体构造方法
     *
     * @param diaryDate 日记的日期
     */
    public DiaryEntity(LocalDate diaryDate) {
        this.diaryDate = diaryDate;
    }
}
