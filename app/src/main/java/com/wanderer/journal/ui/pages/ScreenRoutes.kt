package com.wanderer.journal.ui.pages

import kotlinx.serialization.Serializable

@Serializable
sealed interface ScreenRoutes {
    // 关于软件
    @Serializable
    data object About : ScreenRoutes

    // 疑难解答
    @Serializable
    data object Troubleshooting : ScreenRoutes

    // 日记列表
    @Serializable
    data object Diary : ScreenRoutes

    // 主页
    @Serializable
    data object Home: ScreenRoutes

    // 设置界面
    @Serializable
    data object Settings: ScreenRoutes

    // 权限设置界面
    @Serializable
    data object PermissionSettings: ScreenRoutes
    @Serializable data object MainTabs
}