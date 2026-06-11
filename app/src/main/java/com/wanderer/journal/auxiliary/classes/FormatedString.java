package com.wanderer.journal.auxiliary.classes;

public class FormatedString {
    //数据库段落内容中表示角色引用的字符串的正则表达式[role_ref:@角色名称](角色ID)
    //捕获组1：角色名称
    //捕获组2：角色 ID
    public static final String ROLE_REF_PATTERN = "\\[role_ref:@([^\\]]+)\\]\\((\\d+)\\)";
}
