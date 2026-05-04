package com.wanderer.journal.enums;

public enum LogTags {
    DIARY_FRAGMENT("DiaryFragment"),
    WRITE_ACTIVITY("WriteActivity");
    private final String tagName;

    LogTags(String tagName) {
        this.tagName = tagName;
    }

    /**
     * 获取标签名称
     *
     * @return 标签名称字符串
     */
    public String n() {
        return tagName;
    }
}
