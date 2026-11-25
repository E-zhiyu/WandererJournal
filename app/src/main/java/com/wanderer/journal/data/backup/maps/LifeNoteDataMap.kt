package com.wanderer.journal.data.backup.maps;

import com.wanderer.journal.data.backup.pojo.LifeNoteHistoryPojo;
import com.wanderer.journal.data.backup.pojo.LifeNotePojo;

import java.util.List;

public class LifeNoteDataMap {
    private List<LifeNotePojo> lifeNoteList;                //人生笔记
    private List<LifeNoteHistoryPojo> lifeNoteHistoryList;  //人生笔记历史

    public LifeNoteDataMap() {
    }

    public LifeNoteDataMap(List<LifeNotePojo> lifeNoteList, List<LifeNoteHistoryPojo> lifeNoteHistoryList) {
        this.lifeNoteList = lifeNoteList;
        this.lifeNoteHistoryList = lifeNoteHistoryList;
    }

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
