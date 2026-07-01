package com.wanderer.journal.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;

import com.wanderer.journal.ui.others.popupwindow.TextPopupWindow;

public class TipPreference {
    private static final String PREF_NAME = "TipPreference";
    public static final String KEY_ROLE_REF_METHOD = "role_ref_method";             //在角色列表界面提示引用角色的方法的提示
    public static final String KEY_WRITE_UP_DIARY = "write_up_diary";               //补写日记提示
    public static final String KEY_MEMERY_PIXEL_CHECK = "memery_pixel_check";       //记忆像素可以点击查看日记
    public static final String KEY_SHARE_MULTI_CHOICE = "share_multi_choice";       //日记分享快捷多选提示
    public static final String KEY_READ_MULTI_SEARCH = "read_multi_search";         //读日记界面空格隔开进行多词搜索
    public static final String KEY_CLEAR_ROLE_USE_COUNT = "clear_role_use_count";   //长按清空角色使用次数提示

    /**
     * 保存是否提示过的数据
     *
     * @param context  上下文
     * @param key      该数据的关键字
     * @param tipCount 提醒过的次数
     */
    private static void setValue(@NonNull Context context, String key, int tipCount) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(key, tipCount).apply();
    }

    /**
     * 获取是否提醒过的数据
     *
     * @param context 上下文
     * @param key     关键字
     * @return 是否提醒过
     */
    private static int getValue(@NonNull Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(key, 0);
    }

    /**
     * 显示提示
     *
     * @param anchor      提示浮窗锚点
     * @param gravity     浮窗显示的方位，具体参考{@link Gravity}中的静态常量
     * @param tipMessage  提示文本
     * @param key         关键字，具体参考{@link TipPreference}中的静态常量
     * @param maxTipCount 最大提醒次数
     */
    public static void showTip(@NonNull View anchor, int gravity, String tipMessage, String key, int maxTipCount) {
        Context context = anchor.getContext();
        int currentValue = getValue(context, key);
        if (currentValue < maxTipCount) {
            TextPopupWindow window = new TextPopupWindow(tipMessage, context);
            window.show(anchor, gravity);

            setValue(context, key, currentValue + 1);
        }
    }
}
