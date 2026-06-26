package com.wanderer.journal.auxiliary.enums.text;

public enum EmotionType {
    POSITIVE("积极的"),
    NEUTRAL("中性的"),
    NEGATIVE("消极的");
    private final String title;

    EmotionType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
