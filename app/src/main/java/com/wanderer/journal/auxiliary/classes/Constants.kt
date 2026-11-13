package com.wanderer.journal.auxiliary.classes

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// 圆角相关静态值
object Radius {
    // 圆角大小值
    val Large = 16.dp
    val Small = 4.dp

    // 圆角样式
    val Top =
        RoundedCornerShape(topStart = Large, topEnd = Large, bottomStart = Small, bottomEnd = Small)
    val Middle =
        RoundedCornerShape(topStart = Small, topEnd = Small, bottomStart = Small, bottomEnd = Small)
    val Bottom =
        RoundedCornerShape(topStart = Small, topEnd = Small, bottomStart = Large, bottomEnd = Large)
    val Single =
        RoundedCornerShape(topStart = Large, topEnd = Large, bottomStart = Large, bottomEnd = Large)
}

// 边距相关静态值
object Interval {
    val DefaultPadding = 16.dp
    val SidePadding = 16.dp
}