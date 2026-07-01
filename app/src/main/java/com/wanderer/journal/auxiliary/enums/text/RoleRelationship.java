package com.wanderer.journal.auxiliary.enums.text;

public enum RoleRelationship {
    POOR("关系恶劣"),
    NOT_GET_ALONG("处不来"),
    NORMAL("普通"),
    NOT_BAD("还不赖"),
    CLOSE("关系紧密");
    private final String title;

    RoleRelationship(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
