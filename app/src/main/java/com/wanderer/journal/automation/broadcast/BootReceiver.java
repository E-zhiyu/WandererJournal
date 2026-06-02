package com.wanderer.journal.automation.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.wanderer.journal.auxiliary.enums.LogTags;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
            Log.i(LogTags.BOOT_RECEIVER.n(), "成功监听开机广播");
            //TODO:补充开机自动执行的逻辑（比如说闹钟安排之类的）
        }
    }
}
