package com.wanderer.journal.helpers.appearance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Annotation;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ReplacementSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.color.MaterialColors;
import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.interfaces.ClickableSpanListener;
import com.wanderer.journal.auxiliary.interfaces.EditableFlattenListener;

import org.jetbrains.annotations.Contract;

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
                context.getColor(R.color.color_primary)
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
     * 生成一个在输入框里显示的高亮角色标签块
     */
    @NonNull
    public static SpannableString createRoleTag(Context context, String display, String key, String value) {
        SpannableString spannable = new SpannableString(display);

        //贴纸元数据绑定
        Annotation annotation = new Annotation(key, String.valueOf(value));
        spannable.setSpan(annotation, 0, display.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //绘制展示样式
        ReplacementSpan roundedBackgroundSpan = new ReplacementSpan() {
            @Override
            public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
                // 计算文本的宽度，并额外加上左右的内边距（Padding）
                return Math.round(paint.measureText(text, start, end) + 20);
            }

            @Override
            public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
                int originalColor = paint.getColor();

                //绘制文字
                int clickableColor = MaterialColors.getColor(
                        context,
                        android.R.attr.colorPrimary,
                        context.getColor(R.color.color_primary)
                );
                paint.setColor(clickableColor);
                paint.setFakeBoldText(true); //加粗
                canvas.drawText(text, start, end, x, y, paint);

                //还原 Paint 的颜色，避免污染后续绘制
                paint.setColor(originalColor);
                paint.setFakeBoldText(false);
            }
        };

        spannable.setSpan(roundedBackgroundSpan, 0, display.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    /**
     * 获取字符串中出现的关键词个数
     *
     * @param str     原始字符串
     * @param end     结束计数的下标
     * @param keyword 需要查找次数的关键词
     * @return 关键词出现次数
     */
    public static int getKeywordCount(String str, int end, String keyword) {
        if (str == null || keyword == null || keyword.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;

        while ((index = str.indexOf(keyword, index)) != -1 && index <= end) {
            count++;
            index += keyword.length(); // 跳过已匹配的子串
        }

        return count;
    }

    /**
     * 将{@link Editable}中的{@link Annotation}对象扁平化
     *
     * @param editable {@link Editable}对象
     * @return 扁平化后的字符串
     */
    @NonNull
    @Contract(pure = true)
    public static String flattenEditable(@Nullable Editable editable, EditableFlattenListener listener) {
        if (editable == null) return "";

        SpannableString spannableString = new SpannableString(editable);

        //找出所有Annotation
        Annotation[] annotations = spannableString.getSpans(0, spannableString.length(), Annotation.class);

        //倒序扁平化，防止下标变化
        SpannableStringBuilder builder = new SpannableStringBuilder(editable);
        for (int i = annotations.length - 1; i >= 0; i--) {
            Annotation annotation = annotations[i];

            //获取扁平化后的字符串
            int start = spannableString.getSpanStart(annotation);
            int end = spannableString.getSpanEnd(annotation);
            String raw = editable.subSequence(start, end).toString();
            String flattenedText = listener.getFlattenedText(annotation, raw);

            //添加到builder中
            builder.replace(start, end, flattenedText);
        }

        return builder.toString();
    }
}
