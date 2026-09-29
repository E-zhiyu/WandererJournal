package com.wanderer.journal.auxiliary.enums.types;

public enum ParagraphPrivacyType {
    PUBLIC("公开"),                   //公开
    HIDE_CONTENT("不显示内容"),      //仅隐藏内容，但在列表中可见
    HIDE_FROM_LIST("从列表中隐藏");   //在列表中隐藏，需要验证身份后才能显示

    private final String title;

    ParagraphPrivacyType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
