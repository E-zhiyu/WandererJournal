package com.wanderer.journal.enums;

public enum KeyStrings {
    EMOTION_TAG_ID("emotion_tag_id"),                   //情绪标签的 ID
    EMOTION_TAG_NAME("emotion_tag_name"),               //情绪标签名称
    EMOTION_TAG_DESCRIPTION("emotion_tag_description"), //情绪标签描述
    WRITE_DIARY_DATE("write_diary_date"),               //写界面传递的日期数据
    INIT_DATE("init_date");                             //读日记界面的起始日记日期
    private final String s;

    KeyStrings(String s) {
        this.s = s;
    }

    public String getS() {
        return s;
    }
}
