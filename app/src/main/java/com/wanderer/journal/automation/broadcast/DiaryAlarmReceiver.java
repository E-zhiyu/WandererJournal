package com.wanderer.journal.automation.broadcast;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.enums.ChannelInfo;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.auxiliary.enums.intent.NotificationID;
import com.wanderer.journal.auxiliary.enums.intent.PendingRequestCode;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.preference.DiaryAlarmPreference;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.NotificationHelper;
import com.wanderer.journal.helpers.time.AlarmHelper;
import com.wanderer.journal.ui.pages.WriteActivity;

import java.time.LocalDate;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DiaryAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LogTags.DIARY_ALARM_RECEIVER.n(), "日记提醒接收器已触发");

        //没有打开开关直接结束
        if (!DiaryAlarmPreference.getSwitchStat(context)) {
            return;
        }

        // 建立一个临时性的 CompositeDisposable
        final PendingResult pendingResult = goAsync();
        final CompositeDisposable disposable = new CompositeDisposable();

        //检查日记并决定是否发送通知
        LocalDate now = LocalDate.now();
        LocalDate tomorrow = now.plusDays(1);
        ParagraphDao paragraphDao = DiaryDatabase.getInstance(context).paragraphDao();
        disposable.add(paragraphDao.getParagraphCountByDateRange(now, tomorrow)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    disposable.dispose(); // 释放订阅关系
                    pendingResult.finish(); // 释放广播生命周期，通知系统可以回收了
                })
                .subscribe(
                        count -> {
                            if (count == 0) {
                                Log.d(LogTags.DIARY_ALARM_RECEIVER.n(), "当天没有日记，正在发送通知……");
                                sendNotification(context);
                            }
                        },
                        e -> ExceptionHelper.showExceptionDialog(context, e)
                )
        );

        //安排下一个闹钟
        AlarmHelper.setDiaryAlarm(context);
    }

    /**
     * 发送日记提醒通知
     *
     * @param context 上下文
     */
    private void sendNotification(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //生成点击跳转的 Intent 和 PendingIntent
        Intent skip2Write = new Intent(context, WriteActivity.class);
        skip2Write.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);    //如果任务栈有重复的界面，清理任务栈中重复界面上面的界面
        PendingIntent contentIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(skip2Write)
                .getPendingIntent(
                        PendingRequestCode.TO_WRITE_DIARY.ordinal(),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

        //构建通知
        String channelID = ChannelInfo.DIARY_ALARM.getId();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("日记提醒")
                .setContentText("今天还未写日记，点击跳转写日记界面")
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL);

        //发送通知
        NotificationHelper.sendNotification(
                NotificationID.DIARY_ALARM.ordinal(),
                builder,
                context
        );
    }
}
