package com.wanderer.journal.ui.pages.main.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.auxiliary.classes.Size
import com.wanderer.journal.ui.components.LargeRadiusDropdownMenu
import com.wanderer.journal.ui.components.PressableCard

/**
 * 可点击设置项
 * @param title 设置项标题
 * @param subtitle 设置项副标题，用于描述设置项功能
 * @param arrowText 右侧箭头旁边的文本
 * @param icon 设置项图标
 * @param normalRadius 未按下时的圆角值数组
 */
@Composable
fun SettingClickableItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    arrowText: String? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    icon: Painter,
    normalRadius: Array<Dp>
) {
    PressableCard(
        onClick = { _, _ -> onClick() },
        onLongClick = if (onLongClick == null) null else { _, _ -> onLongClick() },
        modifier = modifier,
        normalRadius = normalRadius,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                subtitle?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            arrowText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 带开关的设置项
 * @param title 设置项标题
 * @param subtitle 设置项副标题，用于描述设置项功能
 * @param normalRadius 未按下时的圆角值数组
 * @param icon 设置项图标
 * @param checked 开关是否打开
 * @param onCheckedChange 开关状态变更回调
 */
@Composable
fun SettingSwitchItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: Painter,
    normalRadius: Array<Dp>,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var switchState = checked   // 保存开关状态

    PressableCard(
        onClick = { _, _ ->
            val newState = !switchState
            onCheckedChange(newState)
            switchState = newState
        },
        modifier = modifier,
        normalRadius = normalRadius,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                if (!subtitle.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = checked,
                onCheckedChange = { checked ->
                    onCheckedChange(checked)
                    switchState = checked
                }
            )
        }
    }
}

/**
 * 下拉选择型设置项（点击弹出下拉菜单）
 * @param title 设置项标题
 * @param subtitle 设置项副标题，用于描述设置项功能
 * @param normalRadius 未按下时的圆角值数组
 * @param currentValueText 当前所选的选项的文本值
 * @param icon 设置项图标
 * @param expanded 下拉框是否展开
 * @param dropDownContent 下拉框的内容
 */
@Composable
fun SettingSpinnerItem(
    title: String,
    subtitle: String? = null,
    icon: Painter,
    normalRadius: Array<Dp>,
    onExpandedChange: (Boolean) -> Unit,
    currentValueText: String,
    expanded: Boolean,
    dropDownContent: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var menuOffset by remember { mutableStateOf(Offset.Zero) }          // 长按点相对父容器的偏移

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        PressableCard(
            onClick = { offset, cardSize ->
                menuOffset = Offset(
                    x = offset.x,
                    y = offset.y - cardSize.height
                )
                onExpandedChange(!expanded)
            },
            normalRadius = normalRadius,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.bodyLarge)
                    if (!subtitle.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                // 显示当前选中的值文本
                Text(
                    text = currentValueText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    painter = painterResource(R.drawable.outline_unfold_more_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        LargeRadiusDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            content = dropDownContent,
            shape = RoundedCornerShape(Radius.Large),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            offset = with(density) {
                DpOffset(
                    x = menuOffset.x.toDp(),
                    y = menuOffset.y.toDp()
                )
            }
        )
    }
}

/**
 * 设置分组标题
 */
@Composable
fun SettingCategoryTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
    )
}

/**
 * 提示文本
 */
@Composable
fun TipTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(Size.bodySmallIcon),
            painter = painterResource(R.drawable.outline_info_24),
            tint = MaterialTheme.colorScheme.outline,
            contentDescription = null
        )
        Text(
            text = title,
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.bodySmall
        )
    }
}