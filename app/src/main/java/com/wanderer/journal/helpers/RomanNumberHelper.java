package com.wanderer.journal.helpers;

public class RomanNumberHelper {
    /**
     * 将整型转换为罗马数字
     *
     * @param number 整型数据
     * @return 罗马数字字符串
     */
    public static String toRoman(int number) {
        if (number < 1 || number > 3999) return String.valueOf(number);

        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] romanLetters = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                number -= values[i];
                roman.append(romanLetters[i]);
            }
        }
        return roman.toString();
    }
}
