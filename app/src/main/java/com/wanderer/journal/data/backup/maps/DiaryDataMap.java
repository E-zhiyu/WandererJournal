package com.wanderer.journal.data.backup.maps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wanderer.journal.data.backup.pojo.DiaryPojo;
import com.wanderer.journal.data.backup.pojo.EmotionTagPojo;
import com.wanderer.journal.data.backup.pojo.MediaPojo;
import com.wanderer.journal.data.backup.pojo.ParagraphPojo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class DiaryDataMap {
    private List<DiaryPojo> diaryList;              //日记列表
    private List<ParagraphPojo> paragraphList;      //段落列表
    private List<MediaPojo> mediaList;              //媒体文件列表
    private List<EmotionTagPojo> emotionTagList;    //情绪标签列表

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

    public List<EmotionTagPojo> getEmotionTagList() {
        return emotionTagList;
    }

    public void setEmotionTagList(List<EmotionTagPojo> emotionTagList) {
        this.emotionTagList = emotionTagList;
    }
}
