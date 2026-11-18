package com.wanderer.journal.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberFabVisible(listState: LazyListState): State<Boolean> {
    // 这两个状态只用于记录"上一次"的滚动位置
    var previousIndex by remember { mutableIntStateOf(listState.firstVisibleItemIndex) }
    var previousOffset by remember { mutableIntStateOf(listState.firstVisibleItemScrollOffset) }

    return remember(listState) {
        derivedStateOf {
            val currentIndex = listState.firstVisibleItemIndex
            val currentOffset = listState.firstVisibleItemScrollOffset

            val isUp = if (previousIndex != currentIndex) {
                previousIndex > currentIndex
            } else {
                previousOffset >= currentOffset
            }

            // 更新记录，供下一次计算
            previousIndex = currentIndex
            previousOffset = currentOffset

            // 在顶部，或向上滚动时显示
            currentIndex == 0 || isUp
        }
    }
}