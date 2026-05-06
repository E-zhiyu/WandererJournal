package com.wanderer.journal.enums;

public enum TagStrings {
    PARAGRAPH_CONTENT_MODIFY_SHEET("paragraph_content_modify_sheet"),
    DATE_PICKER("date_picker"),
    TIME_PICKER("time_picker");
    private final String tag;

    TagStrings(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
