package com.wanderer.journal.auxiliary.enums;

import android.app.NotificationManager;

public enum ChannelInfo {
    DIARY_ALARM(
            "diary_alarm_channel",
            "日记提醒",
            "忘记记日记时发送提醒通知",
            NotificationManager.IMPORTANCE_HIGH
    );

    private final String id;            //通道标识符
    private final String name;          //通道名称
    private final String description;   //通道描述
    private final int importance;       //通知重要性

    ChannelInfo(String id, String name, String description, int importance) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.importance = importance;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getImportance() {
        return importance;
    }
}
