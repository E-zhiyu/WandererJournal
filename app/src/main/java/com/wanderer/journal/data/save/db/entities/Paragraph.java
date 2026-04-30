package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.wanderer.journal.data.save.db.converters.DateTimeConverter;
import com.wanderer.journal.data.save.db.Tables;

import java.time.LocalDateTime;

@Entity(
        tableName = Tables.PARAGRAPH,
        foreignKeys = @ForeignKey(
                entity = Diary.class,
                parentColumns = "diaryId",
                childColumns = "parentDiaryId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("parentDiaryId")}
)
@TypeConverters({DateTimeConverter.class})
public class Paragraph {
    @PrimaryKey(autoGenerate = true)
    public long paragraphId;            //自增主键
    public long parentDiaryId;          //所属的日记的编号
    public String content;              //段落内容
    public LocalDateTime createTime;    //写下该段落的具体时间
    public int orderIndex;              //在同一个日记中的排序下标

    /**
     * 段落实体类的构造方法
     *
     * @param parentDiaryId 所属的日记的编号
     * @param content       段落内容
     * @param createTime    写下该段落的具体时间
     * @param orderIndex    在同一个日记中的排序下标
     */
    public Paragraph(long parentDiaryId, String content, LocalDateTime createTime, int orderIndex) {
        this.parentDiaryId = parentDiaryId;
        this.content = content;
        this.createTime = createTime;
        this.orderIndex = orderIndex;
    }
}
