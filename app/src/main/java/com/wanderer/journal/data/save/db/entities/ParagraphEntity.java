package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.wanderer.journal.data.save.db.converters.DateTimeConverter;

import java.time.LocalDateTime;

@Entity(
        tableName = "paragraphs",
        foreignKeys = @ForeignKey(
                entity = DiaryEntity.class,
                parentColumns = "diaryId",
                childColumns = "parentDiaryId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("parentDiaryId")}
)
@TypeConverters({DateTimeConverter.class})
public class ParagraphEntity {
    @PrimaryKey(autoGenerate = true)
    private long paragraphId;            //自增主键
    private long parentDiaryId;          //所属的日记的编号
    private String content;              //段落内容
    private LocalDateTime createTime;    //写下该段落的具体时间
    private int orderIndex;              //在同一个日记中的排序下标

    /**
     * 段落实体类的构造方法
     *
     * @param parentDiaryId 所属的日记的编号
     * @param content       段落内容
     * @param createTime    写下该段落的具体时间
     * @param orderIndex    在同一个日记中的排序下标
     */
    public ParagraphEntity(long parentDiaryId, String content, LocalDateTime createTime, int orderIndex) {
        this.parentDiaryId = parentDiaryId;
        this.content = content;
        this.createTime = createTime;
        this.orderIndex = orderIndex;
    }

    public long getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(long paragraphId) {
        this.paragraphId = paragraphId;
    }

    public long getParentDiaryId() {
        return parentDiaryId;
    }

    public void setParentDiaryId(long parentDiaryId) {
        this.parentDiaryId = parentDiaryId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
