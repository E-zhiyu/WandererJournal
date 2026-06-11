package com.wanderer.journal.auxiliary.interfaces;

import java.util.regex.Matcher;

/**
 * @param <T> 富文本点击事件需要传递的数据类型
 */
public interface ClickableSpanListener<T> {
    /**
     * 匹配的字符串的显示方法，用于将原始字符串转换为不同的字符串
     *
     * @param matcher 正则表达式匹配对象
     * @return 转换后得到的字符串
     */
    String parseString(Matcher matcher);

    /**
     * 获取点击事件需要的数据
     *
     * @param matcher 正则表达式匹配对象
     * @return 点击事件需要的数据
     */
    T getClickData(Matcher matcher);

    /**
     * 文本被点击的回调
     *
     * @param clickData 点击事件需要的数据
     */
    void onClick(T clickData);
}
