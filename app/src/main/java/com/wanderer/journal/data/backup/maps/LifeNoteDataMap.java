package com.wanderer.journal.data.backup.maps;

import com.wanderer.journal.data.backup.pojo.LifeNoteHistoryPojo;
import com.wanderer.journal.data.backup.pojo.LifeNotePojo;

import java.util.List;

public class LifeNoteDataMap {
    private List<LifeNotePojo> lifeNoteList;
    private List<LifeNoteHistoryPojo> lifeNoteHistoryList;

    public List<LifeNotePojo> getLifeNoteList() {
        return lifeNoteList;
    }

    public void setLifeNoteList(List<LifeNotePojo> lifeNoteList) {
        this.lifeNoteList = lifeNoteList;
    }

    public List<LifeNoteHistoryPojo> getLifeNoteHistoryList() {
        return lifeNoteHistoryList;
    }

    public void setLifeNoteHistoryList(List<LifeNoteHistoryPojo> lifeNoteHistoryList) {
        this.lifeNoteHistoryList = lifeNoteHistoryList;
    }
}
