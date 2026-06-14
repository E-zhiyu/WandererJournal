package com.wanderer.journal.auxiliary.enums.dropdown;

public enum RoleRelationship {
    BAD("恶劣"),
    NOT_GET_ALONG("不对付"),
    NORMAL("普通"),
    NOT_BAD("还不赖"),
    FRIEND("好朋友");
    private final String title;

    RoleRelationship(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
