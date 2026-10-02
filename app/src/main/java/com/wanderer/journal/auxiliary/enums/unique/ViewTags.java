package com.wanderer.journal.auxiliary.enums.unique;

public enum ViewTags {
    RECYCLER_SCROLL_LISTENER(0x7f000001),       // RecyclerView 的滚动监听器
    RECYCLER_SCROLL_RUNNABLE(0x7f000002);       // RecyclerView 的滚动任务

    private final int t;

    ViewTags(int t) {
        this.t = t;
    }

    public int getT() {
        return t;
    }
}
