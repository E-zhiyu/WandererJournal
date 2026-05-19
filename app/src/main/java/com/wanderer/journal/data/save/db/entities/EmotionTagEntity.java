package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "emotionTags",
        indices = {@Index(value = "emotionId")}
)
public class EmotionTagEntity {
    @PrimaryKey(autoGenerate = true)
    private long emotionId;     //自增主键
    private String name;        //名称
    private String description; //描述
    private int type = 1;       //情绪类型

    public EmotionTagEntity(String name, String description, int type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public long getEmotionId() {
        return emotionId;
    }

    public void setEmotionId(long emotionId) {
        this.emotionId = emotionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
