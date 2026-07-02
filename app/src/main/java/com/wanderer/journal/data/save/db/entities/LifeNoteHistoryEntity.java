package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(
        tableName = "lifeNoteHistories",
        foreignKeys = @ForeignKey(
                entity = LifeNoteEntity.class,
                parentColumns = "noteId",
                childColumns = "noteId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "historyId"),
                @Index(value = "noteId"),
                @Index(value = "updateDateTime")
        }
)
public class LifeNoteHistoryEntity {
    @PrimaryKey(autoGenerate = true)
    private long historyId;                 //自增主键
    private long noteId;                    //人生笔记编号
    private String insight;                 //洞见
    private String elaboration;             //阐述
    private LocalDateTime updateDateTime;   //更新时间

    public LifeNoteHistoryEntity() {
    }

    public long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(long historyId) {
        this.historyId = historyId;
    }

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    public String getInsight() {
        return insight;
    }

    public void setInsight(String insight) {
        this.insight = insight;
    }

    public String getElaboration() {
        return elaboration;
    }

    public void setElaboration(String elaboration) {
        this.elaboration = elaboration;
    }

    public LocalDateTime getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(LocalDateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }
}
