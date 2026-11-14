package com.wanderer.journal.auxiliary.classes

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// 圆角相关静态值
object Radius {
    // 圆角大小值
    val Large = 20.dp
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

    // 不同样式的圆角数组
    val TopArray = arrayOf(Large, Large, Small, Small)
    val MiddleArray = arrayOf(Small, Small, Small, Small)
    val BottomArray = arrayOf(Small, Small, Large, Large)
    val SingleArray = arrayOf(Large, Large, Large, Large)
}

// 边距相关静态值
object Interval {
    val DefaultPadding = 16.dp
    val SidePadding = 16.dp
}