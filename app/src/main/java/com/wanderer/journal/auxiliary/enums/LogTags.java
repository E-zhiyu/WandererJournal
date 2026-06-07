package com.wanderer.journal.auxiliary.enums;

public enum LogTags {
    DIARY_FRAGMENT("DiaryFragment"),
    WRITE_ACTIVITY("WriteActivity"),
    DIARY_READ_ACTIVITY("DiaryReadActivity"),
    SAF_HELPER("SAFHelper"),
    LIFECYCLE_MANAGER("LifecycleManager"),
    BIOMETRIC_HELPER("BiometricHelper"),
    DATA_IO_HELPER("DataIOHelper"),
    FILE_HELPER("FileHelper"),
    PERMISSION_HELPER("PermissionHelper"),
    APPEARANCE_ANIMATION_HELPER("AppearanceAnimationHelper"),
    BOOT_RECEIVER("BootReceiver"),
    DIARY_ALARM_RECEIVER("DiaryAlarmReceiver"),
    ALARM_HELPER("AlarmHelper"),
    HTML_HELPER("HtmlHelper"),
    MEDIA_HELPER("MediaHelper"),
    SHARE_PREVIEW_ACTIVITY("SharePreviewActivity"),
    SHARE_WEB_VIEW("ShareWebView"),
    ZIP_HELPER("ZipHelper"),
    FULL_SCREEN_MEDIA_ACTIVITY("FullScreenMediaActivity");
    private final String tagName;

    LogTags(String tagName) {
        this.tagName = tagName;
    }

    /**
     * 获取标签名称
     *
     * @return 标签名称字符串
     */
    public String n() {
        return tagName;
    }
}
