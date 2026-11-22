package com.wanderer.journal.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wanderer.journal.auxiliary.classes.Radius

/**
 * 大圆角下拉框
 *
 * @param modifier DropdownMenu 的 Box 容器的修改器
 * @param expanded 是否展开
 * @param onDismissRequest 消失回调
 * @param shape 下拉框容器的形状
 * @param containerColor 下拉框容器的背景色
 */
@Composable
fun LargeRadiusDropdownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    shape: RoundedCornerShape = RoundedCornerShape(Radius.Large),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            shape = shape,
            containerColor = containerColor
        ) {
            content()
        }
    }
}