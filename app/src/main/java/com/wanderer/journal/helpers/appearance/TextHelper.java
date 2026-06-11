package com.wanderer.journal.helpers.appearance;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.color.MaterialColors;
import com.wanderer.journal.auxiliary.interfaces.ClickableSpanListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextHelper {
    /**
     * 将原始字符串渲染为可点击的字符串
     *
     * @param pattern  正则表达式，用于捕获可点击的字符串部分
     * @param raw      原始字符串
     * @param context  上下文
     * @param listener 匹配的字符串的处理监听
     * @param <T>      富文本点击事件需要传递的数据类型
     * @return 渲染后的文本
     */
    @NonNull
    public static <T> CharSequence renderClickableText(
            Pattern pattern,
            String raw,
            Context context,
            ClickableSpanListener<T> listener
    ) {
        //判空
        if (raw == null) return "";

        SpannableStringBuilder builder = new SpannableStringBuilder();
        int clickableColor = MaterialColors.getColor(
                context,
                android.R.attr.colorPrimary,
                Color.parseColor("#FFFFFF")
        );

        //循环渲染富文本
        int lastIndex = 0;  //上次找到的匹配字符串的位置
        Matcher matcher = pattern.matcher(raw);
        while (matcher.find()) {
            //处理匹配串前方的普通文本
            String normalText = raw.substring(lastIndex, matcher.start());
            if (!normalText.isEmpty()) {
                builder.append(normalText);
            }

            //添加匹配的文本
            String parsedText = listener.parseString(matcher);
            int startParsed = builder.length();
            builder.append(parsedText);
            int endParsed = builder.length();

            //添加点击监听
            T clickData = listener.getClickData(matcher);
            builder.setSpan(
                    new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View view) {
                            listener.onClick(clickData);
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(clickableColor);    //设置颜色
                            ds.setUnderlineText(false);     //无需下划线
                            ds.setFakeBoldText(true);       //加粗
                        }
                    },
                    startParsed,
                    endParsed,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            //更新最后的下标
            lastIndex = matcher.end();
        }

        //处理剩下的文本
        if (lastIndex < raw.length()) {
            String tailText = raw.substring(lastIndex);
            builder.append(tailText);
        }

        return builder;
    }

    /**
     * 渲染高亮文本
     *
     * @param keyword 需要高亮的关键词
     * @param raw     原始文本
     * @param context 上下文
     * @return 高亮后的文本
     */
    @NonNull
    public static CharSequence renderHighLightedText(
            String keyword,
            String raw,
            Context context
    ) {
        SpannableStringBuilder builder = new SpannableStringBuilder(raw);

        int startIndex = raw.indexOf(keyword);
        int heightLightedColor = MaterialColors.getColor(
                context,
                android.R.attr.colorFocusedHighlight,
                Color.parseColor("#FF5722")
        );

        //循环高亮文本
        while (startIndex >= 0) {
            int endIndex = startIndex + keyword.length();
            // 设置文字颜色为橘红色
            builder.setSpan(
                    new ForegroundColorSpan(heightLightedColor),
                    startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // 循环查找，防止一句话里有多个相同的关键词
            startIndex = raw.indexOf(keyword, endIndex);
        }

        return builder;
    }

    /**
     * 获取字符串中出现的关键词个数
     * @param str 原始字符串
     * @param keyword 需要查找次数的关键词
     * @return 关键词出现次数
     */
    public static int getKeywordCount(String str,String keyword) {
        if (str == null || keyword == null || keyword.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;

        while ((index = str.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length(); // 跳过已匹配的子串
        }

        return count;
    }
}
