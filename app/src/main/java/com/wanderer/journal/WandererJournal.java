package com.wanderer.journal;

import android.app.Application;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.wanderer.journal.data.save.preference.AppSettingsPreference;
import com.wanderer.journal.helpers.NotificationHelper;
import com.wanderer.journal.helpers.appearance.ThemeHelper;

public class WandererJournal extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //注册通知渠道
        NotificationHelper.createNotificationChannels(this);

        if (getProcessName().equals(getPackageName())) {
            //初始化动态配色
            if (AppSettingsPreference.getDynamicColorStat(this)) {
                DynamicColorsOptions options = new DynamicColorsOptions.Builder()
                        .setThemeOverlay(R.style.Theme_WandererJournal_Dynamic)
                        .build();
                DynamicColors.applyToActivitiesIfAvailable(this, options);
            }

            //初始化主题模式
            int themeMode = AppSettingsPreference.getThemeMode(this);
            ThemeHelper.applyTheme(themeMode);

            //注册Activity生命周期监听器
            LifecycleManager.init(this);
        }
    }
}
