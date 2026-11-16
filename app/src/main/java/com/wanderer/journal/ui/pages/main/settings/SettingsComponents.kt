package com.wanderer.journal.ui.pages.main.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.ui.components.PressableCard

/**
 * 1. 普通可点击设置项
 */
@Composable
fun SettingClickableItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    icon: Painter,
    normalRadius: Array<Dp>
) {
    PressableCard(
        onClick = onClick,
        modifier = modifier,
        normalRadius = normalRadius,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
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
        }
    }
}

/**
 * 2. 带开关的设置项
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
    PressableCard(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        normalRadius = normalRadius,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
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
                onCheckedChange = onCheckedChange
            )
        }
    }
}

/**
 * 3. 下拉选择型设置项（点击弹出下拉菜单）
 */
@Composable
fun SettingSpinnerItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: Painter,
    normalRadius: Array<Dp>,
    onExpandedChange: (Boolean) -> Unit,
    currentValueText: String,
    expanded: Boolean,
    dropDownContent: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        PressableCard(
            onClick = { onExpandedChange(!expanded) },
            modifier = modifier,
            normalRadius = normalRadius,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
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

        // 下拉菜单
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            content = dropDownContent,
            shape = RoundedCornerShape(Radius.Large),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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