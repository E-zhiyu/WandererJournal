package com.wanderer.journal.helpers.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class HighLightableReplacementSpan extends ReplacementSpan {
    private final int defaultTextColor;             //气泡默认文字颜色
    private final int highlightColor;               //搜索高亮文字颜色
    private final List<String> highlightKeywords;   //需要高亮的文本

    private static class NearestHighlightedKeyword {
        int start;
        String keyword;

        public NearestHighlightedKeyword(int start, String keyword) {
            this.start = start;
            this.keyword = keyword;
        }
    }

    /**
     * @param defaultTextColor  默认文本颜色
     * @param highlightColor    高亮文本颜色
     * @param highlightKeywords 高亮文本正则表达式
     */
    public HighLightableReplacementSpan(int defaultTextColor, int highlightColor, @Nullable List<String> highlightKeywords) {
        this.defaultTextColor = defaultTextColor;
        this.highlightColor = highlightColor;
        this.highlightKeywords = highlightKeywords;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        // 计算文本的宽度，并额外加上左右的内边距（Padding）
        return Math.round(paint.measureText(text, start, end) + 20);
    }

    @Override
    public void draw(
            @NonNull Canvas canvas,
            @NonNull CharSequence text,
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

        //渲染高亮文本
        boolean isKeywordContained = false;
        if (highlightKeywords != null) {
            for (String keyword : highlightKeywords) {
                if (fullTagName.contains(keyword)) {
                    isKeywordContained = true;
                    break;
                }
            }
        }
        if (highlightKeywords != null && !highlightKeywords.isEmpty() && isKeywordContained) {
            int cursorIndex = 0;
            NearestHighlightedKeyword nearest = getNearestHighlightedKeyword(fullTagName, highlightKeywords, cursorIndex);
            while (nearest != null) {
                //获取最近匹配的文本的数据
                int keywordIndex = nearest.start;
                String currentKeyword = nearest.keyword;

                // 阶段 A：绘制关键词前方的文字
                String leftText = fullTagName.substring(0, keywordIndex);
                paint.setColor(defaultTextColor);
                canvas.drawText(leftText, x, y, paint);
                float leftWidth = paint.measureText(leftText);

                // 阶段 B：绘制命中高亮的关键词
                String matchText = fullTagName.substring(keywordIndex, keywordIndex + currentKeyword.length());
                paint.setColor(highlightColor); // 强行切成你的橘红色！
                canvas.drawText(matchText, x + leftWidth, y, paint);
                float matchWidth = paint.measureText(matchText);

                // 阶段 C：绘制关键词后方的文字
                String rightText = fullTagName.substring(keywordIndex + currentKeyword.length());
                paint.setColor(defaultTextColor);
                canvas.drawText(rightText, x + leftWidth + matchWidth, y, paint);

                //更新游标和最近的高亮文本
                cursorIndex = nearest.start + nearest.keyword.length();
                nearest = getNearestHighlightedKeyword(fullTagName, highlightKeywords, cursorIndex);
            }
        } else {
            // 如果没有命中关键词，直接用默认颜色整串绘制
            paint.setColor(defaultTextColor);
            canvas.drawText(text, start, end, x, y, paint);
        }

        // 4. 恢复画笔的原始配置
        paint.setColor(originalColor);
        paint.setFakeBoldText(originalFakeBold);
    }

    /**
     * 获取最近的高亮文本
     *
     * @param fullTagName       文本块的完整名称
     * @param highlightKeywords 高亮的文本数组
     * @param cursorIndex       当前游标的下标
     * @return 最近的高亮关键词，若没有匹配的关键词则返回 null
     */
    @Nullable
    private NearestHighlightedKeyword getNearestHighlightedKeyword(
            @NonNull String fullTagName,
            @NonNull List<String> highlightKeywords,
            int cursorIndex
    ) {
        String subStr = fullTagName.substring(cursorIndex);
        NearestHighlightedKeyword result = null;
        for (String currentWord : highlightKeywords) {
            int start = subStr.indexOf(currentWord);
            if (start != -1 && (result == null || result.start > start)) {
                result = new NearestHighlightedKeyword(start, currentWord);
            }
        }

        return result;
    }
}
