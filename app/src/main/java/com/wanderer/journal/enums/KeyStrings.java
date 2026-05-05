package com.wanderer.journal.enums;

public enum KeyStrings {
    WRITE_DIARY_DATE("write_diary_date");   //写界面传递的日期数据
    private final String s;

    KeyStrings(String s) {
        this.s = s;
    }

    public String getS() {
        return s;
    }
}
