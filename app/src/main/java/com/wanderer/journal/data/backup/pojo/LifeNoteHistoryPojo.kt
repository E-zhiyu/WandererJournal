package com.wanderer.journal.data.backup.pojo;

public class LifeNoteHistoryPojo {
    private long historyId;
    private long noteId;
    private String insight;
    private String elaboration;
    private long updateDateTime;

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

    public long getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(long updateDateTime) {
        this.updateDateTime = updateDateTime;
    }
}
