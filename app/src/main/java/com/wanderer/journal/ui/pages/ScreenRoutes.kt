package com.wanderer.journal.ui.pages

import android.net.Uri
import kotlinx.serialization.Serializable

@Serializable
sealed interface ScreenRoutes {
    // 主界面三个子界面
    @Serializable data object MainTabs

    // 关于软件
    @Serializable
    data object About : ScreenRoutes

    // 疑难解答
    @Serializable
    data object Troubleshooting : ScreenRoutes

    // 权限设置界面
    @Serializable
    data object PermissionSettings: ScreenRoutes

    // 分享设置界面
    @Serializable
    data object ShareSettings: ScreenRoutes

    // 日记提醒设置界面
    @Serializable
    data object DiaryAlarmSettings: ScreenRoutes

    // 数据管理界面
    @Serializable
    data object DataManage: ScreenRoutes

    // 媒体列表界面
    @Serializable
    data object MediaList: ScreenRoutes

    // 全屏大图界面
    @Serializable
    data class FullScreenMedia(val uris: List<String>, val initialPos: Int = 0)

    // 媒体详情界面
    @Serializable
    data class MediaInfo(val uri: String)
}