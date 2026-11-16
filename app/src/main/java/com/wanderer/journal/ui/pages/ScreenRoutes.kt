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
}