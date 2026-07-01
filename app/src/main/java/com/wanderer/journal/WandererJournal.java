package com.wanderer.journal;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.auxiliary.enums.settings.AuthOpportunity;
import com.wanderer.journal.data.save.preference.AppSettingsPreference;
import com.wanderer.journal.data.save.preference.SecurityPreference;
import com.wanderer.journal.helpers.NotificationHelper;
import com.wanderer.journal.helpers.appearance.ThemeHelper;
import com.wanderer.journal.ui.pages.AuthActivity;

import java.util.List;

public class WandererJournal extends Application {
    private static boolean isLifecycleObserverLocked = false;   //生命周期观察者是否被锁定
    private int startedActivityCount = 0;                       //在前台的活动数量

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

            //注册应用级的生命周期观察者
            ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onStart(@NonNull LifecycleOwner owner) {
                    Log.d(LogTags.WANDERER_JOURNAL.n(), "触发全局生命周期观察者的onStart()");
                    if (isLifecycleObserverLocked) {
                        Log.d(LogTags.WANDERER_JOURNAL.n(), "消费掉生命周期观察者的锁");
                        isLifecycleObserverLocked = false;  //重新启动时消费掉锁定
                        return;
                    }

                    // 应用进入前台，检查是否需要解锁
                    long currentTimeMillis = System.currentTimeMillis();
                    long lastSuccessTimeMillis = AuthActivity.getLastSuccessTimeMillis();
                    long minDifference = AuthOpportunity.values()[
                            SecurityPreference.getAuthOpportunity(WandererJournal.this)
                            ].getTimeMilli();
                    if (
                            SecurityPreference.getAuthSwitchStat(WandererJournal.this) &&
                                    currentTimeMillis - lastSuccessTimeMillis >= minDifference
                    ) {
                        Intent intent = new Intent(WandererJournal.this, AuthActivity.class);
                        // FLAG_ACTIVITY_NEW_TASK 是从 Application 启动 Activity 必须带的
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            });

            registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                @Override
                public void onActivityStarted(@NonNull Activity activity) {
                    startedActivityCount++;
                }

                @Override
                public void onActivityStopped(@NonNull Activity activity) {
                    startedActivityCount--;

                    if (isLifecycleObserverLocked) return;  //如果生命周期观察者被锁定，不执行任何操作

                    // 当计数器归零，说明【此时此刻】没有任何 Activity 在前台了，用户刚按了 Home 键或锁屏
                    if (startedActivityCount == 0) {
                        if (SecurityPreference.getHideRecentTask(WandererJournal.this)) {
                            // 这里的 onStop 是跟随 Activity 的，会立刻执行，不会拖延到下次启动
                            removeTaskFromRecents();
                        }
                    }
                }

                // 其他生命周期方法留空...
                @Override
                public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                }

                @Override
                public void onActivityResumed(@NonNull Activity activity) {
                }

                @Override
                public void onActivityPaused(@NonNull Activity activity) {
                }

                @Override
                public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
                }

                @Override
                public void onActivityDestroyed(@NonNull Activity activity) {
                }
            });
        }
    }

    /**
     * 从最近任务中隐藏
     */
    private void removeTaskFromRecents() {
        Log.d(LogTags.WANDERER_JOURNAL.n(), "触发最近任务隐藏");
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.AppTask> taskList = am.getAppTasks();
            if (taskList != null) {
                for (ActivityManager.AppTask task : taskList) {
                    // 立刻移除
                    task.finishAndRemoveTask();
                }
            }
        }
    }

    /**
     * 锁定生命周期观察者
     */
    public static void lockLifecycleObserver() {
        isLifecycleObserverLocked = true;
    }
}
