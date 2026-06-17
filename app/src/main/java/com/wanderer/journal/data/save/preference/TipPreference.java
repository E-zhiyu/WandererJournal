package com.wanderer.journal.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class TipPreference {
    private static final String PREF_NAME = "TipPreference";
    public static final String KEY_ROLE_REF_METHOD = "role_ref_method";    //在角色列表界面提示引用角色的方法

    /**
     * 保存是否提示过的数据
     *
     * @param context 上下文
     * @param key     该数据的关键字
     * @param value   是否提醒过
     */
    public static void setValue(@NonNull Context context, String key, boolean value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(key, value).apply();
    }

    /**
     * 获取是否提醒过的数据
     *
     * @param context 上下文
     * @param key     关键字
     * @return 是否提醒过
     */
    public static boolean getValue(@NonNull Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(key, false);
    }
}
