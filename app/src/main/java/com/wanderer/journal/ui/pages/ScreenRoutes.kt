package com.wanderer.journal.ui.pages

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
}