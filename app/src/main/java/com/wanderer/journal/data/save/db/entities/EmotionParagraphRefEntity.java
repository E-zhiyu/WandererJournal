package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

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
                @Index("emotionId"),
                @Index("degree")
        }
)
public class EmotionParagraphRefEntity {
    private long paragraphId;   //段落 ID
    private long emotionId;     //情绪标签 ID
    private int degree;         //情绪的程度指数

    /**
     * 创建情绪标签与段落关系的构造方法
     *
     * @param emotionId   情绪标签 ID
     * @param paragraphId 段落 ID
     */
    public EmotionParagraphRefEntity(long emotionId, long paragraphId) {
        this.emotionId = emotionId;
        this.paragraphId = paragraphId;
        this.degree = 1;
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

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }
}
