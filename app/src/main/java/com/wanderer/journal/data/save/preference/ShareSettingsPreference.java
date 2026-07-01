package com.wanderer.journal.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class ShareSettingsPreference {
    private static final String PREF_NAME = "ShareSettingsPreference";
    public static final String KEY_MEDIA = "media";         //媒体开关
    public static final String KEY_EMOTION = "emotion";     //情绪标签开关
    public static final String KEY_TIME = "time";           //时间开关
    private static final String KEY_PICTURE_BOTTOM_TEXT = "picture_bottom_text";    //分享图片底部文本

    /**
     * 设置开关状态
     *
     * @param context 上下文
     * @param key     开关关键字
     * @param stat    开关状态
     */
    public static void setSwitchStat(@NonNull Context context, String key, boolean stat) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(key, stat).apply();
    }

    /**
     * 获取开关状态
     *
     * @param context 上下文
     * @param key     开关关键字
     * @return 开关状态
     */
    public static boolean getSwitchStat(@NonNull Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(key, true);
    }

    /**
     * 设置分享图片底部文本
     *
     * @param context 上下文
     * @param text    底部文本
     */
    public static void setPictureBottomText(@NonNull Context context, String text) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_PICTURE_BOTTOM_TEXT, text).apply();
    }

    /**
     * 获取分享图片底部文本
     *
     * @param context 上下文
     * @return 保存的图片底部文本
     */
    public static String getPictureBottomText(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_PICTURE_BOTTOM_TEXT, "");
    }
}
