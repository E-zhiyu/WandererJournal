package com.wanderer.journal.auxiliary.enums;

public enum ViewTags {
    RECYCLER_SCROLL_LISTENER(0x7f000001);
    private final int t;

    ViewTags(int t) {
        this.t = t;
    }

    public int getT() {
        return t;
    }
}
