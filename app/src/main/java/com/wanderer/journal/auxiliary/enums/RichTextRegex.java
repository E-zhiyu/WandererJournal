package com.wanderer.journal.auxiliary.enums;

import androidx.annotation.NonNull;

import java.util.regex.Pattern;

public enum RichTextRegex {
    //角色引用正则表达式：[role_ref:@角色名](角色ID)
    ROLE_REF(
            "\\[role_ref:@([^\\]]+)\\]\\((\\d+)\\)"
    );
    private final String regexStr;

    RichTextRegex(String regexStr) {
        this.regexStr = regexStr;
    }

    /**
     * 获取正则表达式对象
     *
     * @return 正则表达式{@link Pattern}对象
     */
    @NonNull
    public Pattern getPattern() {
        return Pattern.compile(regexStr);
    }
}
