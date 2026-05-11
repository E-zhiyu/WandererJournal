package com.wanderer.journal.helpers.classes;

public class TextFileData {
    private final String content;       //文本内容
    private final long lastModifyTime;  //最后编辑时间的时间戳

    public TextFileData(String content, long lastModifyTime) {
        this.content = content;
        this.lastModifyTime = lastModifyTime;
    }

    public String getContent() {
        return content;
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }
}
