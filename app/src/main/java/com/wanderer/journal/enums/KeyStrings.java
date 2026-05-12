package com.wanderer.journal.enums;

public enum KeyStrings {
    WRITE_DIARY_DATE("write_diary_date"),   //写界面传递的日期数据
    INIT_DATE("init_date");                 //读日记界面的起始日记日期
    private final String s;

    KeyStrings(String s) {
        this.s = s;
    }

    public String getS() {
        return s;
    }
}
