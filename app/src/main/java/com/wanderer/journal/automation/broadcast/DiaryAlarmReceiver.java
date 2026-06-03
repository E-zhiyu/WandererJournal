package com.wanderer.journal.automation.broadcast;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.enums.ChannelInfo;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.auxiliary.enums.NotificationID;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.NotificationHelper;
import com.wanderer.journal.helpers.time.AlarmHelper;

import java.time.LocalDate;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DiaryAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LogTags.DIARY_ALARM_RECEIVER.n(), "日记提醒接收器已触发");
        //TODO:检查定时任务无法触发的BUG

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

        String channelID = ChannelInfo.DIARY_ALARM.getId();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("日记提醒")
                .setContentText("今天还未写日记，别忘了哦~");

        NotificationHelper.sendNotification(
                NotificationID.DIARY_ALARM.ordinal(),
                builder,
                context
        );
    }
}
