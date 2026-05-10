package com.wanderer.journal.helpers;

public class StringHelper {
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
        String[] lines = text.split("\r\n|\n|\r", -1); // -1 保留尾部空字符串

        // 如果最后一个是空字符串且原字符串以换行结尾，不算作一行
        int count = lines.length;
        if (text.endsWith("\n") || text.endsWith("\r")) {
            count--;
        }

        return count;
    }
}
