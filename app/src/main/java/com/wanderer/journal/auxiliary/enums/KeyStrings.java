package com.wanderer.journal.auxiliary.enums;

public enum KeyStrings {
    KEY_ROLE_GROUP("key_role_group"),                   //角色分组的关键字
    ROLE_ID("role_id"),                                 //角色 ID
    ROLE_NAME("role_name"),                             //角色名称
    ROLE_DISPLAY_NAME("role_display_name"),             //角色显示名称
    ROLE_IDENTITY("role_identity"),                     //角色身份
    ROLE_IMPRESSION("role_impression"),                 //角色印象
    ROLE_RELATIONSHIP("role_relationship"),             //角色关系
    ROLE_ALIAS("role_alias"),                           //角色别名
    EMOTION_TAG_ID("emotion_tag_id"),                   //情绪标签的 ID
    EMOTION_TAG_TYPE("emotion_tag_type"),               //情绪标签种类
    EMOTION_TAG_NAME("emotion_tag_name"),               //情绪标签名称
    EMOTION_TAG_DESCRIPTION("emotion_tag_description"), //情绪标签描述
    WRITE_MODIFY_PARAGRAPH_ID("write_modify_paragraph_id"), //写日记界面初始化进入编辑模式时的段落 ID
    VIEW_HOLDER_POSITION("view_holder_position"),       // ViewHolder 的位置
    FILE_URIS("file_uris"),                             //文件 Uri
    SHARED_PARAGRAPH_ID("shared_paragraph_id"),         //分享的段落 ID
    INIT_DATE("init_date");                             //读日记界面的起始日记日期
    private final String s;

    KeyStrings(String s) {
        this.s = s;
    }

    public String getS() {
        return s;
    }
}
