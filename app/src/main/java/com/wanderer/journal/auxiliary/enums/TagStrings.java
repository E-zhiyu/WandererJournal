package com.wanderer.journal.auxiliary.enums;

public enum TagStrings {
    EMOTION_SELECT_BOTTOM_SHEET("emotion_select_bottom_sheet"), //情绪标签选择对话框
    EMOTION_FILTER_BOTTOM_SHEET("emotion_filter_bottom_sheet"), //情绪标签过滤对话框
    DATE_PICKER("date_picker"),                                 //日期选择对话框
    MEDIA_ADD_BOTTOM_SHEET("media_add_bottom_sheet"),           //媒体添加对话框
    TIME_PICKER("time_picker"),                                 //时间选择对话框
    PARAGRAPH_SELECTION("paragraph_selection"),                 //段落多选追踪器
    MEDIA_SELECTION("media_selection");                         //媒体多选追踪器
    private final String tag;

    TagStrings(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
