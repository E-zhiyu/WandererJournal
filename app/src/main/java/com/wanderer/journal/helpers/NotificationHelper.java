package com.wanderer.journal.helpers;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.wanderer.journal.auxiliary.enums.ChannelInfo;

public class NotificationHelper {
    /**
     * 创建通知渠道
     *
     * @param context 上下文
     */
    public static void createNotificationChannels(@NonNull Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        for (ChannelInfo info : ChannelInfo.values()) {
            NotificationChannel channel = info.getNotificationChannel();
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 发送通知
     *
     * @param notificationId 该通知的唯一标识符，如果有相同标识符的通知，则会直接覆盖更新。可以使用{@link NotificationHelper}中的枚举常数作为标识符
     * @param builder        已经设置好的通知构建器
     * @param context        上下文
     */
    public static void sendNotification(
            int notificationId,
            @NonNull NotificationCompat.Builder builder,
            @NonNull Context context
    ) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        }
    }

    /**
     * 取消通知
     *
     * @param notificationId 需要取消的通知的唯一标识符
     */
    public static void cancelNotification(int notificationId, Context context) {
        NotificationManagerCompat.from(context).cancel(notificationId);
    }
}
