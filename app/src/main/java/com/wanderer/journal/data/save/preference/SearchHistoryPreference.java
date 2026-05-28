package com.wanderer.journal.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryPreference {
    private static final String PREF_NAME = "SearchHistoryPreference";
    public static final String KEY_DIARY_CONTENT = "diary_content";     //日记内容

    /**
     * 读取搜索历史
     *
     * @param key     需要读取的历史所对应的键值，具体可见{@link SearchHistoryPreference}的静态成员变量
     * @param context 上下文
     * @return 包含已保存的搜索记录的字符串列表
     */
    public static List<String> getHistory(String key, @NonNull Context context) {
        //读取原始JSON字符串
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(key, "[]");

        //将JSON转换为列表
        ObjectMapper mapper = new ObjectMapper();
        List<String> historyList;
        try {
            JavaType javaType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class);
            historyList = mapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            historyList = new ArrayList<>();
        }

        return historyList;
    }

    /**
     * 覆写搜索历史记录
     *
     * @param key         需要读取的历史所对应的键值，具体可见{@link SearchHistoryPreference}的静态成员变量
     * @param historyList 修改后的历史记录列表
     * @param context     上下文
     */
    private static void setHistory(String key, List<String> historyList, Context context) {
        //将列表转换为JSON
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(historyList);
        } catch (JsonProcessingException e) {
            json = "[]";
        }

        //写入Preference
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(key, json).apply();
    }

    /**
     * 添加搜索关键词
     *
     * @param keyword 搜索关键词
     * @param key     保存搜索关键词的键，详见{@link SearchHistoryPreference}的静态字符串
     * @param context 上下文
     */
    public static List<String> addKeyword(String keyword, String key, Context context) {
        List<String> historyList = getHistory(key, context);
        if (keyword == null || keyword.isEmpty()) {
            return historyList;
        }

        historyList.remove(keyword);
        historyList.add(0, keyword);
        setHistory(key, historyList, context);

        return historyList;
    }

    /**
     * 移除搜索历史记录
     *
     * @param keyword 需要移除的搜索关键词
     * @param key     保存搜索关键词的键，详见{@link SearchHistoryPreference}的静态字符串
     * @param context 上下文
     * @return 移除了关键词后的列表
     */
    public static List<String> removeKeyword(String keyword, String key, Context context) {
        List<String> historyList = getHistory(key, context);
        if (keyword == null || keyword.isEmpty()) {
            return historyList;
        }

        historyList.remove(keyword);
        setHistory(key, historyList, context);
        return historyList;
    }

    /**
     * 清空历史搜索记录
     *
     * @param key     需要清空的搜索记录的关键字
     * @param context 上下文
     */
    public static void clearHistory(String key, Context context) {
        setHistory(key, new ArrayList<>(), context);
    }
}
