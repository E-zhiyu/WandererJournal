package com.wanderer.journal.helpers.time;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParseHelper {
    private enum DateRule {
        ISO_DASH("(\\d{4}-\\d{1,2}-\\d{1,2})", "y-M-d"),
        UNDERLINE_DATE("(\\d{4}_\\d{1,2}_\\d{1,2})", "y_M_d"),
        CN_DATE("(\\d{4}年\\d{1,2}月\\d{1,2}日)", "y年M月d日"),
        SLASH_DATE("(\\d{4}/\\d{1,2}/\\d{1,2})", "y/M/d"),
        DOT_DATE("(\\d{4}\\.\\d{1,2}\\.\\d{1,2})", "y.M.d"),
        COMPACT_DATE("(\\d{8})", "yyyyMMdd");

        final Pattern pattern;
        final DateTimeFormatter formatter;

        DateRule(String regex, String format) {
            // 使用 Pattern.compile 提高效率
            this.pattern = Pattern.compile(regex);
            this.formatter = DateTimeFormatter.ofPattern(format);
        }
    }

    /**
     * 将可能包含日期的字符串转换为{@link LocalDate}
     *
     * @param line 可能包含日期的字符串
     * @return 转换得到的日期对象，转换失败则返回 null
     */
    public static LocalDate parseFlexible(String line) {
        if (line == null || line.isEmpty()) return null;

        for (DateRule rule : DateRule.values()) {
            Matcher matcher = rule.pattern.matcher(line);
            if (matcher.find()) {
                // 核心逻辑：只取出捕获组中的日期字符串
                String datePart = matcher.group(1);
                try {
                    return LocalDate.parse(datePart, rule.formatter);
                } catch (Exception e) {
                    // 如果正则匹配成功但日期是非法的（如 2026-13-40），继续尝试其他规则
                }
            }
        }
        return null;
    }
}
