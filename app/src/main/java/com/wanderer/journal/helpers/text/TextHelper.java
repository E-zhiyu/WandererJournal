package com.wanderer.journal.helpers.text;

import android.content.ClipData;
import android.content.ClipboardManager;
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
import com.google.android.material.textfield.TextInputEditText;
import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.interfaces.ClickableSpanListener;
import com.wanderer.journal.auxiliary.interfaces.EditableFlattenListener;
import com.wanderer.journal.auxiliary.interfaces.RichTextRule;

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
        if (raw == null || raw.isEmpty()) return "";

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
     * 生成文本块
     *
     * @param context 上下文
     * @param display 文本块显示的文本
     * @param key     文本块保存数据的关键字
     * @param value   文本块保存的数据
     */
    @NonNull
    public static SpannableString createTextTag(Context context, String display, String key, String value) {
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
        if (editable == null || editable.isEmpty()) return "";

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

    /**
     * 将普通文本立体化
     *
     * @param context 上下文
     * @param raw     原始文本
     * @param rules   用于立体化的转换规则
     * @return 能够直接显示在{@link TextInputEditText}中的富文本，已将特定格式的文本转换为文本块
     */
    @NonNull
    public static CharSequence hierarchicInEditable(Context context, String raw, RichTextRule<?>... rules) {
        if (raw == null || raw.isEmpty() || rules == null || rules.length == 0) {
            return raw == null ? "" : raw;
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        int cursor = 0; // 当前流水线扫描到的字符指针位置
        int textLength = raw.length();

        while (cursor < textLength) {
            MatchResultSnapshot closestMatch = null;

            // 遍历所有规则，看看在当前光标往后的文本里，谁留在最前面（start最小）
            for (RichTextRule<?> rule : rules) {
                Matcher matcher = rule.getPattern().matcher(raw);

                // 从当前指针位置往后探测
                if (matcher.find(cursor)) {
                    // 如果这是第一个找到的，或者它比之前其他正则找到的更靠前
                    if (closestMatch == null || matcher.start() < closestMatch.start) {
                        String key = rule.getKey();
                        closestMatch = new MatchResultSnapshot(matcher.start(), matcher.end(), rule, key, matcher);
                    }
                }
            }

            // 情况 A：后面再也没有任何规则能匹配上了，把剩下的文本作为普通文本追加，直接结束
            if (closestMatch == null) {
                builder.append(raw.substring(cursor));
                break;
            }

            // 情况 B：在后面找到了匹配项，但匹配项前面有一段普通文本（如 "今天和"）
            if (closestMatch.start > cursor) {
                builder.append(raw.substring(cursor, closestMatch.start));
            }

            // 情况 C：精准命中规则，开始调用该规则的自定义立体化包装
            RichTextRule<?> matchedRule = closestMatch.rule;
            Matcher matchedMatcher = closestMatch.matcher;

            // 提取 UI 显示文案和绑定的数据
            String displayText = matchedRule.getDisplayText(matchedMatcher);
            String clickData = String.valueOf(matchedRule.getTextTagData(matchedMatcher));

            // 立体化组装：生成带有输入框专用的 Annotation 和 ReplacementSpan 的标签
            // 这里我们需要动态识别不同的 Key，确保保存时能够区分出谁是谁
            String key = closestMatch.key;
            SpannableString richTag = createTextTag(context, displayText, key, clickData);
            builder.append(richTag);

            // 将光标指针强行推进到当前匹配完的暗号末尾，准备下一轮轮巡
            cursor = closestMatch.end;
        }

        return builder;
    }

    //内部类：用于记录某一次精准匹配的结果快照
    private static class MatchResultSnapshot {
        int start;              //匹配成功的起始下标
        int end;                //匹配成功的结束下标
        RichTextRule<?> rule;   //富文本转换规则
        String key;             //文本块保存数据的关键字
        Matcher matcher;        //正则表达式匹配对象

        public MatchResultSnapshot(int start, int end, RichTextRule<?> rule, String key, Matcher matcher) {
            this.start = start;
            this.end = end;
            this.rule = rule;
            this.key = key;
            this.matcher = matcher;
        }
    }

    /**
     * 复制文本到剪贴板
     *
     * @param context 上下文
     * @param label   复制内容的标题
     * @param value   复制的内容
     */
    public static void copyToClipBoard(@NonNull Context context, String label, String value) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label, value);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clipData);
        }
    }
}
