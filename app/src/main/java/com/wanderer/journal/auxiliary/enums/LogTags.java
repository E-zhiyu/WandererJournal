package com.wanderer.journal.auxiliary.enums;

public enum LogTags {
    WANDERER_JOURNAL("WandererJournal"),
    DIARY_FRAGMENT("DiaryFragment"),
    WRITE_ACTIVITY("WriteActivity"),
    DIARY_READ_ACTIVITY("DiaryReadActivity"),
    ROLE_SELECT_BOTTOM_SHEET("RoleSelectBottomSheet"),
    SAF_HELPER("SAFHelper"),
    AUTH_ACTIVITY("AuthActivity"),
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
    PARAGRAPH_INNER_MEDIA_ADAPTER("ParagraphInnerMediaAdapter"),
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
