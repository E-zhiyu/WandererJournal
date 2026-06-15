package com.wanderer.journal.helpers.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HighLightableReplacementSpan extends ReplacementSpan {
    private final int defaultTextColor; // 气泡默认文字颜色
    private final int highlightColor;   // 搜索高亮文字颜色
    private final Pattern highlightPattern; // 高亮正则表达式

    /**
     * @param defaultTextColor 默认文本颜色
     * @param highlightColor   高亮文本颜色
     * @param highlightPattern 高亮文本正则表达式
     */
    public HighLightableReplacementSpan(int defaultTextColor, int highlightColor, @Nullable Pattern highlightPattern) {
        this.defaultTextColor = defaultTextColor;
        this.highlightColor = highlightColor;
        this.highlightPattern = highlightPattern;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        // 计算文本的宽度，并额外加上左右的内边距（Padding）
        return Math.round(paint.measureText(text, start, end) + 20);
    }

    @Override
    public void draw(
            @NonNull Canvas canvas,
            CharSequence text,
            int start,
            int end,
            float x,
            int top,
            int y,
            int bottom,
            @NonNull Paint paint
    ) {
        // 1. 备份原本的 Paint 状态，防止污染后续整行其他文本的绘制
        int originalColor = paint.getColor();
        boolean originalFakeBold = paint.isFakeBoldText();

        // 3. 【核心高亮算法】：利用正则 Matcher 进行流式分段绘制
        String fullTagName = text.subSequence(start, end).toString(); // 例如 " @张三12 "
        paint.setFakeBoldText(true); // 气泡内文字统一加粗

        // 初始绘制起点偏移量
        float currentXOffset = x;

        //渲染高亮文本
        if (highlightPattern != null) {
            Matcher matcher = highlightPattern.matcher(fullTagName);
            int lastDrawIndex = 0; // 记录上一次绘制截断的字符下标

            // 流式扫描：只要正则能匹配到下一段，就切开绘制
            while (matcher.find()) {
                int matchStart = matcher.start();
                int matchEnd = matcher.end();

                // 阶段 A：绘制“当前命中正则”前方的普通文字
                if (matchStart > lastDrawIndex) {
                    String leftText = fullTagName.substring(lastDrawIndex, matchStart);
                    paint.setColor(defaultTextColor);
                    canvas.drawText(leftText, currentXOffset, y, paint);
                    currentXOffset += paint.measureText(leftText); // 绘制完后，横坐标向右递增
                }

                // 阶段 B：绘制“精准命中正则”的高亮文字
                String matchText = fullTagName.substring(matchStart, matchEnd);
                paint.setColor(highlightColor); // 切换为高亮色（如橘红色）
                canvas.drawText(matchText, currentXOffset, y, paint);
                currentXOffset += paint.measureText(matchText);

                // 更新下一次绘制的起点
                lastDrawIndex = matchEnd;
            }

            // 阶段 C：收尾，绘制整串文本最后剩下的普通文字
            if (lastDrawIndex < fullTagName.length()) {
                String tailText = fullTagName.substring(lastDrawIndex);
                paint.setColor(defaultTextColor);
                canvas.drawText(tailText, currentXOffset, y, paint);
            }

        } else {
            // 如果压根没有传入高亮正则，直接用默认颜色整串一口气绘制
            paint.setColor(defaultTextColor);
            canvas.drawText(text, start, end, x, y, paint);
        }

        // 4. 恢复画笔的原始配置
        paint.setColor(originalColor);
        paint.setFakeBoldText(originalFakeBold);
    }
}
