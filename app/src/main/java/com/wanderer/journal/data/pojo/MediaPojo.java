package com.wanderer.journal.data.pojo;

public class MediaPojo {
    long mediaId;           //媒体文件ID
    long parentParagraphId; //所属段落ID
    String uriStr;          //文件路径Uri字符串

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

    public String getUriStr() {
        return uriStr;
    }

    public void setUriStr(String uriStr) {
        this.uriStr = uriStr;
    }
}
