package com.wanderer.journal.data.backup.maps;

import com.wanderer.journal.data.backup.pojo.DiaryPojo;
import com.wanderer.journal.data.backup.pojo.MediaPojo;
import com.wanderer.journal.data.backup.pojo.ParagraphPojo;

import java.util.List;

public class DiaryDataMap {
    private List<DiaryPojo> diaryList;          //日记列表
    private List<ParagraphPojo> paragraphList;  //段落列表
    private List<MediaPojo> mediaList;          //媒体文件列表

    public DiaryDataMap() {
    }

    public List<DiaryPojo> getDiaryList() {
        return diaryList;
    }

    public void setDiaryList(List<DiaryPojo> diaryList) {
        this.diaryList = diaryList;
    }

    public List<ParagraphPojo> getParagraphList() {
        return paragraphList;
    }

    public void setParagraphList(List<ParagraphPojo> paragraphList) {
        this.paragraphList = paragraphList;
    }

    public List<MediaPojo> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<MediaPojo> mediaList) {
        this.mediaList = mediaList;
    }
}
