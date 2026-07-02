package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
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
    private final long historyId;
    private final long noteId;
    private final String insight;
    private final String elaboration;
    private final LocalDateTime updateDateTime;

    public LifeNoteHistoryEntity(long historyId, long noteId, String insight, String elaboration, LocalDateTime updateDateTime) {
        this.historyId = historyId;
        this.noteId = noteId;
        this.insight = insight;
        this.elaboration = elaboration;
        this.updateDateTime = updateDateTime;
    }

    @Ignore
    public LifeNoteHistoryEntity(long noteId, String insight, String elaboration, LocalDateTime updateDateTime) {
        this(0, noteId, insight, elaboration, updateDateTime);
    }

    public long getHistoryId() {
        return historyId;
    }

    public long getNoteId() {
        return noteId;
    }

    public String getInsight() {
        return insight;
    }

    public String getElaboration() {
        return elaboration;
    }

    public LocalDateTime getUpdateDateTime() {
        return updateDateTime;
    }
}
