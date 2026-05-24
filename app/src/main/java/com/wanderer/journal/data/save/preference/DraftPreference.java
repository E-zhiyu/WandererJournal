package com.wanderer.journal.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class DraftPreference {
    private static final String PREF_NAME = "DraftPreference";
    private static final String KEY_DRAFT = "draft";

    /**
     * 保存草稿
     *
     * @param context 上下文
     * @param draft   草稿内容
     */
    public static void setDraft(@NonNull Context context, String draft) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_DRAFT, draft).apply();
    }

    /**
     * 读取草稿
     *
     * @param context 上下文
     * @return 保存的草稿内容
     */
    public static String getDraft(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_DRAFT, "");
    }
}
