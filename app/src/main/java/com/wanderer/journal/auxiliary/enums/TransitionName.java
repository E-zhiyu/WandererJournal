package com.wanderer.journal.auxiliary.enums;

public enum TransitionName {
    PARAGRAPH_MEDIA("paragraph_media");
    private final String s;

    TransitionName(String s) {
        this.s = s;
    }

    public String getS() {
        return s;
    }
}
