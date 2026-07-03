package com.wanderer.journal.data.save.db.entities.composite;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.wanderer.journal.data.save.db.entities.LifeNoteEntity;
import com.wanderer.journal.data.save.db.entities.LifeNoteHistoryEntity;

import java.util.List;

public class LifeNoteWithHistoryModel {
    @Embedded
    private LifeNoteEntity lifeNote;
    @Relation(
            entity = LifeNoteHistoryEntity.class,
            parentColumn = "noteId",
            entityColumn = "noteId"
    )
    private List<LifeNoteHistoryEntity> historyList;

    public LifeNoteEntity getLifeNote() {
        return lifeNote;
    }

    public void setLifeNote(LifeNoteEntity lifeNote) {
        this.lifeNote = lifeNote;
    }

    public List<LifeNoteHistoryEntity> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<LifeNoteHistoryEntity> historyList) {
        this.historyList = historyList;
    }
}
