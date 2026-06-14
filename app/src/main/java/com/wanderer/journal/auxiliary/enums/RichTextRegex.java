package com.wanderer.journal.auxiliary.enums;

import androidx.annotation.NonNull;

import java.util.regex.Pattern;

public enum RichTextRegex {
    //角色引用正则表达式：[role_ref:@角色名](角色ID)
    ROLE_REF(
            "\\[role_ref:@([^\\]]+)\\]\\((\\d+)\\)",
            16
    );
    private final String regexStr;  //正则表达式字符串
    private final int minLength;    //最短匹配文本的长度

    RichTextRegex(String regexStr, int minLength) {
        this.regexStr = regexStr;
        this.minLength = minLength;
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

    /**
     * 得到最短的正则表达式长度
     *
     * @return 最短的正则表达式长度
     */
    public static int getShortestPatternLength() {
        int min = Integer.MAX_VALUE;
        for (RichTextRegex regex : RichTextRegex.values()) {
            if (regex.minLength < min) min = regex.minLength;
        }
        return min;
    }
}
