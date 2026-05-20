package com.wanderer.journal.enums;

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
    ZIP_HELPER("ZipHelper");
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
