package com.wanderer.journal.enums.menus;

public enum ParagraphEdit {
    EDIT_CONTENT(0,0,0,"编辑内容"),
    EDIT_TIME(0,1,1,"修改时间");
    private final int groupId;
    private final int itemId;
    private final int order;
    private final String title;

    ParagraphEdit(int groupId, int itemId, int order, String title) {
        this.groupId = groupId;
        this.itemId = itemId;
        this.order = order;
        this.title = title;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getOrder() {
        return order;
    }

    public String getTitle() {
        return title;
    }
}
