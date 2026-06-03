package com.wanderer.journal.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DiaryAlarmPreference {
    private static final String PREF_NAME = "DiaryAlarmPreference";
    private static final String KEY_SWITCH = "switch";          //功能开关状态
    private static final String KEY_ALARM_TIME = "alarm_time";  //提醒时间

    /**
     * 获取功能开关状态
     *
     * @param context 上下文
     * @return 开关状态（默认为关闭状态）
     */
    public static boolean getSwitchStat(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_SWITCH, false);
    }

    /**
     * 设置开关状态
     *
     * @param context  上下文
     * @param isOpened 开关是否打开
     */
    public static void setSwitchStat(@NonNull Context context, boolean isOpened) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_SWITCH, isOpened).apply();
    }

    /**
     * 获取所有提醒时间
     *
     * @param context 上下文
     * @return 包含所有提醒时间的列表，按照时间升序排序
     */
    public static List<LocalTime> getAlarmTime(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_ALARM_TIME, "[]");

        //将JSON转换为列表
        ObjectMapper mapper = new ObjectMapper();
        List<String> timeStrList;
        try {
            JavaType javaType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class);
            timeStrList = mapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            timeStrList = new ArrayList<>();
        }

        //转换为 LocalTime 类型并返回
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return timeStrList.stream()
                .map(str -> LocalTime.parse(str, formatter)).sorted()
                .collect(Collectors.toList());
    }

    /**
     * 保存提醒时间
     *
     * @param context  上下文
     * @param timeList 时间列表
     */
    public static void setAlarmTime(Context context, @NonNull List<LocalTime> timeList) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<String> timeStrList = timeList.stream()
                .map(localTime -> localTime.format(formatter))
                .collect(Collectors.toList());

        //将列表转换为JSON
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(timeStrList);
        } catch (JsonProcessingException e) {
            json = "[]";
        }

        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_ALARM_TIME, json).apply();
    }
}
