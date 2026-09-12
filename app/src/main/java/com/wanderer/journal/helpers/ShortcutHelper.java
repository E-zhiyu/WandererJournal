package com.wanderer.journal.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;

import androidx.annotation.NonNull;
import androidx.core.app.TaskStackBuilder;

import com.wanderer.journal.auxiliary.enums.ShortcutBuildInfo;

import java.util.ArrayList;
import java.util.List;

public class ShortcutHelper {
    /**
     * 构建在{@link ShortcutBuildInfo}中定义的快捷方式
     *
     * @param context 上下文
     */
    public static void buildShortcuts(@NonNull Context context) {
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

        List<ShortcutInfo> shortcutInfoList = new ArrayList<>();
        for (ShortcutBuildInfo info : ShortcutBuildInfo.values()) {
            //构建基本信息
            ShortcutInfo.Builder builder = new ShortcutInfo.Builder(context, info.getId())
                    .setShortLabel(context.getString(info.getShortLabelRes()))
                    .setIcon(Icon.createWithResource(context, info.getIconRes()));

            //填入 Intent
            if (info.isIncludeParent()) {
                //获取包含父界面的 Intent 数组
                Intent intent = info.getIntent(context);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntentWithParentStack(intent);
                Intent[] intents = stackBuilder.getIntents();

                builder.setIntents(intents);
            } else {
                builder.setIntent(info.getIntent(context));
            }

            shortcutInfoList.add(builder.build());
        }

        shortcutManager.addDynamicShortcuts(shortcutInfoList);
    }
}
