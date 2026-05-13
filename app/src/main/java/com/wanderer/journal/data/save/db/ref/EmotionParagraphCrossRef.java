package com.wanderer.journal.data.save.db.ref;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

@Entity(
        tableName = "emotionParagraphCrossRef",
        primaryKeys = {"paragraphId", "emotionId"},
        foreignKeys = {
                @ForeignKey(
                        entity = ParagraphEntity.class,
                        parentColumns = "paragraphId",
                        childColumns = "paragraphId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = EmotionTagEntity.class,
                        parentColumns = "emotionId",
                        childColumns = "emotionId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("paragraphId"),
                @Index("emotionId")
        }
)
public class EmotionParagraphCrossRef {
    long paragraphId;
    long emotionId;

    public EmotionParagraphCrossRef(long emotionId, long paragraphId) {
        this.emotionId = emotionId;
        this.paragraphId = paragraphId;
    }

    public long getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(long paragraphId) {
        this.paragraphId = paragraphId;
    }

    public long getEmotionId() {
        return emotionId;
    }

    public void setEmotionId(long emotionId) {
        this.emotionId = emotionId;
    }
}
