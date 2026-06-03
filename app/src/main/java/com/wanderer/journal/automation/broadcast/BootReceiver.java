package com.wanderer.journal.automation.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.data.save.preference.DiaryAlarmPreference;
import com.wanderer.journal.helpers.time.AlarmHelper;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(LogTags.BOOT_RECEIVER.n(), "成功监听开机广播");

            //设置日记提醒闹钟
            if (DiaryAlarmPreference.getSwitchStat(context)) {
                AlarmHelper.setDiaryAlarm(context);
            }
        }
    }
}
