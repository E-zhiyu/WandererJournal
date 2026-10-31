package com.wanderer.journal;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.wanderer.journal.automation.worker.WorkerScheduler;
import com.wanderer.journal.automation.worker.backup.BackupWorker;
import com.wanderer.journal.auxiliary.enums.unique.LogTags;
import com.wanderer.journal.auxiliary.enums.unique.TagStrings;
import com.wanderer.journal.auxiliary.enums.settings.AuthOpportunity;
import com.wanderer.journal.auxiliary.enums.settings.BackupFrequency;
import com.wanderer.journal.data.save.preference.AppSettingsPreference;
import com.wanderer.journal.data.save.preference.AutoBackupPreference;
import com.wanderer.journal.data.save.preference.SecurityPreference;
import com.wanderer.journal.data.save.preference.VersionPreference;
import com.wanderer.journal.helpers.NotificationHelper;
import com.wanderer.journal.helpers.ShortcutHelper;
import com.wanderer.journal.helpers.appearance.ThemeHelper;
import com.wanderer.journal.helpers.file.FileHelper;
import com.wanderer.journal.ui.pages.AuthActivity;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class WandererJournal extends Application {
    private static boolean isLifecycleObserverLocked = false;   //生命周期观察者是否被锁定

    @Override
    public void onCreate() {
        super.onCreate();

        //是否需要隐藏最近任务
        hideTaskFromRecents(this, SecurityPreference.getHideRecentTask(this));

        //动态注册（通知渠道、快捷方式）
        NotificationHelper.createNotificationChannels(this);
        ShortcutHelper.buildShortcuts(this);

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

            //安排自动备份任务
            if (AutoBackupPreference.getSwitchStat(this)) {
                int frequency = AutoBackupPreference.getBackupFrequency(this);
                long intervalMillis = BackupFrequency.values()[frequency].getIntervalMillis();
                WorkerScheduler.schedulePeriodicTask(this, intervalMillis, TagStrings.BACKUP_WORKER.t(), BackupWorker.class);

                //打印任务状态日志
                try {
                    WorkInfo info = WorkManager.getInstance(this).getWorkInfosForUniqueWork(TagStrings.BACKUP_WORKER.t()).get().get(0);
                    Log.d(LogTags.WORK_STATS.n(), "State: " + info.getState());
                } catch (ExecutionException | InterruptedException e) {
                    Log.d(LogTags.WORK_STATS.n(), "State: " + BackupWorker.class + "未正常工作");
                }
            }

            //注册应用级的生命周期观察者
            ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onStart(@NonNull LifecycleOwner owner) {
                    Log.d(LogTags.APPLICATION.n(), "触发全局生命周期观察者的onStart()");
                    if (isLifecycleObserverLocked) {
                        Log.d(LogTags.APPLICATION.n(), "消费掉生命周期观察者的锁");
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

            //启动时检测是否有需要删除的安装包
            String apkUri = VersionPreference.getApkUri(this);
            if (!apkUri.isEmpty()) {
                Uri contentUri = Uri.parse(apkUri);
                FileHelper.deleteFile(contentUri, this);
                VersionPreference.setApkUri(this, "");
            }
        }
    }

    /**
     * 设置是否在最近任务中隐藏任务卡片
     *
     * @param context  上下文
     * @param isHidden 是否隐藏
     */
    public static void hideTaskFromRecents(@NonNull Context context, boolean isHidden) {
        Log.d(LogTags.APPLICATION.n(), "触发最近任务隐藏");
        ActivityManager am = context.getSystemService(ActivityManager.class);
        if (am == null) {
            return;
        }

        //获取任务列表
        List<ActivityManager.AppTask> taskList = am.getAppTasks();
        if (taskList == null) {
            return;
        }

        //将所有任务都标记为排除
        for (ActivityManager.AppTask task : taskList) {
            task.setExcludeFromRecents(isHidden);
        }
    }

    /**
     * 锁定生命周期观察者
     */
    public static void lockLifecycleObserver(boolean isLocked) {
        isLifecycleObserverLocked = isLocked;
    }
}
