package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(
        tableName = "lifeNotes",
        indices = {
                @Index(value = "noteId"),
                @Index(value = "insight"),
                @Index(value = "elaboration"),
                @Index(value = "dateTime")
        }
)
public class LifeNoteEntity {
    @PrimaryKey(autoGenerate = true)
    private long noteId;            //主键
    private String insight;         //洞见
    private String elaboration;     //阐述
    private LocalDateTime dateTime; //更新时间

    public LifeNoteEntity(String insight, String elaboration, LocalDateTime dateTime) {
        this.insight = insight;
        this.elaboration = elaboration;
        this.dateTime = dateTime;
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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
