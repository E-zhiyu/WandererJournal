package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.wanderer.journal.data.save.db.converters.DateTimeConverter;
import com.wanderer.journal.data.save.db.Tables;

import java.time.LocalDate;

@Entity(tableName = Tables.DIARY)
@TypeConverters({DateTimeConverter.class})
public class Diary {
    @PrimaryKey(autoGenerate = true)
    public long diaryId;        //自增主键
    public LocalDate diaryDate; //日记的日期

    /**
     * 日记实体构造方法
     *
     * @param localDate 日记的日期
     */
    public Diary(LocalDate localDate) {
        diaryDate = localDate;
    }
}
