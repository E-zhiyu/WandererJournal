package com.wanderer.journal.data.backup.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class MediaPojo {
    long mediaId;           //媒体文件ID
    long parentParagraphId; //所属段落ID
    String fileUri;         //文件路径Uri字符串

    public MediaPojo() {
    }

    public long getMediaId() {
        return mediaId;
    }

    public void setMediaId(long mediaId) {
        this.mediaId = mediaId;
    }

    public long getParentParagraphId() {
        return parentParagraphId;
    }

    public void setParentParagraphId(long parentParagraphId) {
        this.parentParagraphId = parentParagraphId;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }
}
