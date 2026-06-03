package com.wanderer.journal.helpers.time;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wanderer.journal.automation.broadcast.DiaryAlarmReceiver;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.auxiliary.enums.intent.RequestResultCode;
import com.wanderer.journal.data.save.preference.DiaryAlarmPreference;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

public class AlarmHelper {
    /**
     * 安排精确闹钟
     *
     * @param dateTime 闹钟触发时间
     * @param intent   闹钟触发后执行的意图
     * @param context  上下文
     */
    public static void setAlarm(@NonNull LocalDateTime dateTime, int requestCode, Intent intent, @NonNull Context context) {
        //转换为时间戳
        long timeMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();   //使用当前时区转换为时间戳
        long systemMillis = System.currentTimeMillis();
        Log.d(LogTags.ALARM_HELPER.n(), "已安排定时任务，时间：" + dateTime);
        Log.d(LogTags.ALARM_HELPER.n(), "安排的任务的时间戳：" + timeMillis);
        Log.d(LogTags.ALARM_HELPER.n(), "系统时间戳：" + systemMillis);

        //获取闹钟管理器
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //设置单一的闹钟
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExact(
                        AlarmManager.RTC_WAKEUP,
                        timeMillis,
                        pi
                );
                Log.i(LogTags.ALARM_HELPER.n(), "已设置定时任务");
            } else {
                Log.e(LogTags.ALARM_HELPER.n(), "无法设置定时任务");
            }
        } else {
            am.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeMillis,
                    pi
            );
            Log.i(LogTags.ALARM_HELPER.n(), "已设置定时任务（旧版本）");
        }
    }

    /**
     * 取消已设置的定时任务
     *
     * @param requestCode 已设置的定时任务的请求代码
     * @param intent      已设置的定时任务的意图对象
     * @param context     上下文
     */
    public static void cancelAlarm(int requestCode, Intent intent, Context context) {
        // 重新构建 PendingIntent（注意：这里的 Flag 可以是 FLAG_NO_CREATE 或 FLAG_UPDATE_CURRENT）
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE // FLAG_NO_CREATE 表示如果不存在就不创建
        );

        // 如果 PendingIntent 存在，调用 cancel
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);

            // 彻底释放这个 PendingIntent
            pendingIntent.cancel();
            Log.d(LogTags.ALARM_HELPER.n(), "定时任务已成功取消");
        } else {
            Log.d(LogTags.ALARM_HELPER.n(), "没有找到匹配的定时任务，无需取消");
        }
    }

    /**
     * 设置日记提醒闹钟
     *
     * @param context 上下文
     */
    public static void setDiaryAlarm(Context context) {
        //获取时间数据
        List<LocalTime> savedTimeList = DiaryAlarmPreference.getAlarmTime(context);
        LocalDateTime now = LocalDateTime.now();
        LocalTime nowTime = now.toLocalTime();

        //取出离当前最近的 LocalTime
        LocalTime nextTime;
        if (savedTimeList.isEmpty()) {
            nextTime = LocalTime.of(21, 0); //如果没有设置时间，默认九点钟触发提醒
            Toast.makeText(context, "未设置提醒时间，默认21点进行提醒", Toast.LENGTH_SHORT).show();
        } else {
            nextTime = savedTimeList.get(0);
            for (LocalTime savedTime : savedTimeList) {
                if (nowTime.isBefore(savedTime)) {
                    nextTime = savedTime;
                    break;
                }
            }
        }

        //将 LocalTime 组合为 LocalDateTime
        LocalDateTime todayAlarmDateTime = now.toLocalDate().atTime(nextTime);

        //根据现在的时间安排下一个闹钟
        Intent toDiaryAlarmReceiver = new Intent(context, DiaryAlarmReceiver.class);
        if (now.isBefore(todayAlarmDateTime)) {
            setAlarm(
                    todayAlarmDateTime,
                    RequestResultCode.REQUEST_SET_DIARY_ALARM.ordinal(),
                    toDiaryAlarmReceiver,
                    context
            );
        } else {
            //获取第二天提醒的时间
            LocalTime tomorrowTime;
            if (savedTimeList.isEmpty()) {
                tomorrowTime = LocalTime.of(21, 0);
            } else {
                tomorrowTime = savedTimeList.get(0);
            }

            //设置第二天的提醒闹钟
            setAlarm(
                    now.plusDays(1).toLocalDate().atTime(tomorrowTime),
                    RequestResultCode.REQUEST_SET_DIARY_ALARM.ordinal(),
                    toDiaryAlarmReceiver,
                    context
            );
        }
    }

    /**
     * 取消日记提醒定时任务
     *
     * @param context 上下文
     */
    public static void cancelDiaryAlarm(Context context) {
        Intent toDiaryAlarmReceiver = new Intent(context, DiaryAlarmReceiver.class);

        cancelAlarm(RequestResultCode.REQUEST_SET_DIARY_ALARM.ordinal(), toDiaryAlarmReceiver, context);
    }
}
