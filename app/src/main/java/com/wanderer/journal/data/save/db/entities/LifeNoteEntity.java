package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
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
    private final long noteId;              //主键
    private final String insight;           //洞见
    private final String elaboration;       //阐述
    private final LocalDateTime dateTime;   //更新时间

    public LifeNoteEntity(long noteId, String insight, String elaboration, LocalDateTime dateTime) {
        this.noteId = noteId;
        this.insight = insight;
        this.elaboration = elaboration;
        this.dateTime = dateTime;
    }

    @Ignore
    public LifeNoteEntity(String insight, String elaboration, LocalDateTime dateTime) {
        this(0, insight, elaboration, dateTime);
    }

    public long getNoteId() {
        return noteId;
    }

    public String getInsight() {
        return insight;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getElaboration() {
        return elaboration;
    }
}
