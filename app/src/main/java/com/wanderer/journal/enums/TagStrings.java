package com.wanderer.journal.enums;

public enum TagStrings {
    PARAGRAPH_CONTENT_MODIFY_SHEET("paragraph_content_modify_sheet"),
    EMOTION_TAG_SELECT_BOTTOM_SHEET("emotion_tag_select_bottom_sheet"),
    DATE_PICKER("date_picker"),
    MEDIA_ADD_BOTTOM_SHEET("media_add_bottom_sheet"),
    TIME_PICKER("time_picker"),
    MEDIA_SELECTION("media_selection");
    private final String tag;

    TagStrings(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
