package com.wanderer.journal.helpers.text;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
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
import com.wanderer.journal.auxiliary.interfaces.EditableFlattenListener;
import com.wanderer.journal.auxiliary.interfaces.RichTextRule;

import org.jetbrains.annotations.Contract;

import java.util.regex.Matcher;

public class TextHelper {
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
     * 通过split方法统计文本的行数
     *
     * @param text 待统计行数的文本
     * @return 行数
     */
    public static int countLinesSplit(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        // 处理空字符串
        String[] lines = text.split("\\s+", -1); // -1 保留尾部空字符串

        // 如果最后一个是空字符串且原字符串以换行结尾，不算作一行
        int count = lines.length;
        if (text.endsWith("\n") || text.endsWith("\r")) {
            count--;
        }

        return count;
    }

    /**
     * 为特定区域的文本添加点击事件
     *
     * @param context   上下文
     * @param start     开始下标
     * @param end       结束下标
     * @param clickData 点击后需要传递的字符串数据
     * @param builder   富文本构建器
     * @param rule      文本立体化规则
     */
    public static void addClickListener(
            Context context,
            int start,
            int end,
            String clickData,
            @NonNull SpannableStringBuilder builder,
            @NonNull RichTextRule rule
    ) {
        if (start < 0) start = 0;
        if (end > builder.length()) end = builder.length();

        int clickableColor = MaterialColors.getColor(
                context,
                android.R.attr.colorPrimary,
                context.getColor(R.color.color_primary)
        );

        builder.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View view) {
                        rule.onClick(clickData);
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(clickableColor);    //设置颜色
                        ds.setUnderlineText(false);     //无需下划线
                        ds.setFakeBoldText(true);       //加粗
                    }
                },
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    /**
     * 渲染高亮文本
     *
     * @param highlightedKeywords 高亮文本的正则表达式
     * @param raw                 原始文本（支持富文本）
     * @param context             上下文
     * @return 高亮后的文本
     */
    @NonNull
    public static CharSequence renderHighLightedText(
            String[] highlightedKeywords,
            CharSequence raw,
            Context context
    ) {
        if (raw == null || raw.length() == 0) return "";
        if (highlightedKeywords == null || highlightedKeywords.length == 0) return raw;

        SpannableStringBuilder builder = new SpannableStringBuilder(raw);
        String rawStr = String.valueOf(raw);

        int heightLightedColor = MaterialColors.getColor(
                context,
                android.R.attr.colorFocusedHighlight,
                Color.parseColor("#FF5722")
        );

        //循环高亮文本
        for (String currentKeyWord : highlightedKeywords) {
            int startIndex = rawStr.indexOf(currentKeyWord);
            while (startIndex >= 0) {
                int endIndex = startIndex + currentKeyWord.length();
                // 设置文字颜色为橘红色
                builder.setSpan(
                        new ForegroundColorSpan(heightLightedColor),
                        startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                // 循环查找，防止一句话里有多个相同的关键词
                startIndex = rawStr.indexOf(currentKeyWord, endIndex);
            }
        }

        return builder;
    }

    /**
     * 生成文本块
     *
     * @param context             上下文
     * @param highlightedKeywords 需要高亮的文本
     * @param display             文本块显示的文本
     * @param key                 文本块保存数据的关键字
     * @param value               文本块保存的数据
     */
    @NonNull
    public static SpannableString createTextTag(Context context, String[] highlightedKeywords, String display, String key, String value) {
        SpannableString spannable = new SpannableString(display);

        //贴纸元数据绑定
        Annotation annotation = new Annotation(key, String.valueOf(value));
        spannable.setSpan(annotation, 0, display.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //绘制展示样式
        int defaultColor = MaterialColors.getColor(
                context,
                android.R.attr.colorPrimary,
                context.getColor(R.color.color_primary)
        );
        int highlightedColor = MaterialColors.getColor(
                context,
                android.R.attr.colorFocusedHighlight,
                Color.parseColor("#FF5722")
        );
        ReplacementSpan roundedBackgroundSpan = new HighLightableReplacementSpan(defaultColor, highlightedColor, highlightedKeywords);

        spannable.setSpan(roundedBackgroundSpan, 0, display.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
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
        if (editable == null || editable.toString().isEmpty()) return "";

        //获取 Annotation 并扁平化
        SpannableStringBuilder builder = new SpannableStringBuilder(editable);
        Annotation[] annotations = builder.getSpans(0, builder.length(), Annotation.class);
        for (Annotation annotation : annotations) {
            //获取 annotation 的起止位置
            int start = builder.getSpanStart(annotation);
            int end = builder.getSpanEnd(annotation);
            if (start == -1 || end == -1) continue;

            //生成扁平化文本
            String raw = builder.subSequence(start, end).toString();
            String flattenedText = listener.getFlattenedText(annotation, raw);

            //添加到 builder 中
            builder.replace(start, end, flattenedText);
        }

        return builder.toString();
    }

    /**
     * 将普通文本立体化
     *
     * @param context             上下文
     * @param highlightedKeywords 高亮文本
     * @param raw                 原始文本
     * @param rules               用于立体化的转换规则
     * @return 能够直接显示在{@link TextInputEditText}中的富文本，已将特定格式的文本转换为文本块
     */
    @NonNull
    public static CharSequence hierarchicFromString(Context context, @Nullable String[] highlightedKeywords, String raw, RichTextRule... rules) {
        if (raw == null || raw.isEmpty() || rules == null || rules.length == 0)
            return raw == null ? "" : raw;

        SpannableStringBuilder builder = new SpannableStringBuilder();
        int cursor = 0; // 当前流水线扫描到的字符指针位置

        while (cursor < raw.length()) {
            MatchResultSnapshot closestMatch = null;

            // 遍历所有规则，看看在当前光标往后的文本里，谁留在最前面（start最小）
            for (RichTextRule rule : rules) {
                Matcher matcher = rule.getPattern().matcher(raw);

                // 从当前指针位置往后探测
                if (matcher.find(cursor)) {
                    // 如果这是第一个找到的，或者它比之前其他正则找到的更靠前
                    if (closestMatch == null || matcher.start() < closestMatch.start) {
                        closestMatch = new MatchResultSnapshot(matcher.start(), matcher.end(), rule, matcher);
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
            RichTextRule matchedRule = closestMatch.rule;
            Matcher matchedMatcher = closestMatch.matcher;

            // 提取 UI 显示文案和绑定的数据
            String displayText = matchedRule.getDisplayText(matchedMatcher);
            String tagData = matchedRule.getTextTagData(matchedMatcher);

            // 立体化组装：生成带有输入框专用的 Annotation 和 ReplacementSpan 的标签
            // 这里我们需要动态识别不同的 Key，确保保存时能够区分出谁是谁
            String key = matchedRule.getKey();
            SpannableString richTag = createTextTag(context, highlightedKeywords, displayText, key, tagData);
            builder.append(richTag);

            //添加点击事件
            addClickListener(
                    context,
                    builder.length() - richTag.length(),
                    builder.length(),
                    tagData,
                    builder,
                    matchedRule
            );

            // 将光标指针强行推进到当前匹配完的暗号末尾，准备下一轮轮巡
            cursor = closestMatch.end;
        }

        return renderHighLightedText(highlightedKeywords, builder, context);
    }

    /**
     * 直接将{@link Editable}中的内容富文本化，并且保持原有的富文本不被冲刷掉
     *
     * @param context  上下文
     * @param editable 需要富文本化的{@link Editable}对象
     * @param rules    富文本化的规则
     */
    @NonNull
    public static CharSequence hierarchicFromEditable(Context context, Editable editable, RichTextRule... rules) {
        if (editable == null || rules == null || rules.length == 0) return "";

        int cursor = 0;
        SpannableStringBuilder result = new SpannableStringBuilder(editable);
        while (cursor < result.length()) {
            String currentText = result.toString();
            MatchResultSnapshot closestMatch = null;

            // 遍历所有注册的规则，寻找当前光标后最先出现的暗号
            for (RichTextRule rule : rules) {
                Matcher matcher = rule.getPattern().matcher(currentText);
                if (matcher.find(cursor)) {
                    if (closestMatch == null || matcher.start() < closestMatch.start) {
                        closestMatch = new MatchResultSnapshot(matcher.start(), matcher.end(), rule, matcher);
                    }
                }
            }

            // 如果后面没有暗号了，直接收工
            if (closestMatch == null) {
                break;
            }

            // 命中规则，开始原位金蝉脱壳
            RichTextRule matchedRule = closestMatch.rule;
            Matcher matchedMatcher = closestMatch.matcher;

            String displayText = matchedRule.getDisplayText(matchedMatcher);
            String clickData = String.valueOf(matchedRule.getTextTagData(matchedMatcher));

            // 生成带圆角和 Annotation 的 SpannableString
            SpannableString richTag = createTextTag(
                    context,
                    null,
                    displayText,
                    matchedRule.getKey(),
                    clickData
            );

            // 直接在输入框的缓冲区里，用立体标签把扁平暗号给“顶替”掉
            result.replace(closestMatch.start, closestMatch.end, richTag);

            // 光标强行推进到新插入的富文本标签末尾
            cursor = closestMatch.start + richTag.length();
        }

        return result;
    }

    /**
     * 将扁平化的文本立体化，但是仍然返回普通文本
     *
     * @param raw   原始文本
     * @param rules 立体化的规则
     * @return 立体化后的普通文本
     */
    @NonNull
    public static String hierarchicButNormalText(String raw, RichTextRule... rules) {
        if (raw == null || raw.isEmpty() || rules == null || rules.length == 0) return "";

        StringBuilder builder = new StringBuilder();
        int cursor = 0; // 当前流水线扫描到的字符指针位置
        while (cursor < raw.length()) {
            MatchResultSnapshot closestMatch = null;

            // 遍历所有规则，看看在当前光标往后的文本里，谁留在最前面（start最小）
            for (RichTextRule rule : rules) {
                Matcher matcher = rule.getPattern().matcher(raw);

                // 从当前指针位置往后探测
                if (matcher.find(cursor)) {
                    // 如果这是第一个找到的，或者它比之前其他正则找到的更靠前
                    if (closestMatch == null || matcher.start() < closestMatch.start) {
                        closestMatch = new MatchResultSnapshot(matcher.start(), matcher.end(), rule, matcher);
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
            RichTextRule matchedRule = closestMatch.rule;
            Matcher matchedMatcher = closestMatch.matcher;

            // 提取显示文案
            String displayText = matchedRule.getDisplayText(matchedMatcher);
            builder.append(displayText);

            // 将光标指针强行推进到当前匹配完的暗号末尾，准备下一轮轮巡
            cursor = closestMatch.end;
        }

        return builder.toString();
    }

    //内部类：用于记录某一次精准匹配的结果快照
    private static class MatchResultSnapshot {
        int start;              //匹配成功的起始下标
        int end;                //匹配成功的结束下标
        RichTextRule rule;   //富文本转换规则
        Matcher matcher;        //正则表达式匹配对象

        public MatchResultSnapshot(int start, int end, RichTextRule rule, Matcher matcher) {
            this.start = start;
            this.end = end;
            this.rule = rule;
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
