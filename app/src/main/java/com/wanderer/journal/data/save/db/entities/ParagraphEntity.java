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
        indices = {
                @Index("parentDiaryId"),
                @Index("createTime")
        }
)
@TypeConverters({DateTimeConverter.class})
public class ParagraphEntity {
    @PrimaryKey(autoGenerate = true)
    private long paragraphId;            //自增主键
    private long parentDiaryId;          //所属的日记的编号
    private String content;              //段落内容
    private LocalDateTime createTime;    //写下该段落的具体时间

    /**
     * 段落实体类的构造方法
     *
     * @param parentDiaryId 所属的日记的编号
     * @param content       段落内容
     * @param createTime    写下该段落的具体时间
     */
    public ParagraphEntity(long parentDiaryId, String content, LocalDateTime createTime) {
        this.parentDiaryId = parentDiaryId;
        this.content = content;
        this.createTime = createTime;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
