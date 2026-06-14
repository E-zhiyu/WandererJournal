package com.wanderer.journal.auxiliary.interfaces;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用多规则拦截器接口
 */
public interface RichTextRule {

    //该规则对应的正则表达式（例如角色的正则、话题的正则）
    Pattern getPattern();

    //匹配成功后，在输入框里显示什么文本（例如返回 " @张三 " 或 " #今日心情# "）
    String getDisplayText(Matcher matcher);

    //获取标记 Annotation 类型的关键字
    String getKey();

    //匹配成功后，该标签塞进草稿箱的元数据对象（T 可以是 Long ID，也可以是 String 话题名）
    String getTextTagData(Matcher matcher);

    void onClick(long clickData);

    long transToClickData(String tagData);
}