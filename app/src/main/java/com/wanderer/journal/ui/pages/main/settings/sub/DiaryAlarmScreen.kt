package com.wanderer.journal.ui.pages.main.settings.sub

import android.content.res.Configuration
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.auxiliary.classes.Size
import com.wanderer.journal.data.save.store.viewmodels.DiaryAlarmViewModel
import com.wanderer.journal.data.save.store.viewmodels.dsDiaryAlarmViewModel
import com.wanderer.journal.helpers.permission.isAutoStartDefined
import com.wanderer.journal.ui.components.LazyLargeTopAppBarScaffold
import com.wanderer.journal.ui.pages.main.settings.SettingCategoryTitle
import com.wanderer.journal.ui.pages.main.settings.SettingClickableItem
import com.wanderer.journal.ui.pages.main.settings.SettingSwitchItem
import com.wanderer.journal.ui.pages.main.settings.TipTitle
import com.wanderer.journal.ui.theme.MaterialAppTheme
import java.time.LocalTime

@Composable
fun DiaryAlarmScreen(
    onNavigateBack: () -> Unit,
    vm: DiaryAlarmViewModel = dsDiaryAlarmViewModel()
) {
    // 设置项
    val alarmSettings by vm.diaryAlarm.collectAsStateWithLifecycle()

    // 时间选择对话框
    var timePickerVisible by remember { mutableStateOf(false) }

    LazyLargeTopAppBarScaffold(
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.diary_alarm)
    ) {
        // 总开关
        item {
            SettingSwitchItem(
                title = stringResource(R.string.total_switch),
                subtitle = stringResource(R.string.alarm_total_switch_subtitle),
                icon = painterResource(R.drawable.outline_alarm_24),
                normalRadius = Radius.TopArray,
                checked = alarmSettings.switch,
                onCheckedChange = { checked -> vm.setSwitchOpen(checked) }
            )
        }

        // 添加提醒时间
        item {
            Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
            SettingClickableItem(
                title = stringResource(R.string.add_alarm_time),
                subtitle = stringResource(R.string.add_alarm_time_subtitle),
                icon = painterResource(R.drawable.baseline_add_alarm_24),
                normalRadius = Radius.BottomArray,
                onClick = { timePickerVisible = true }
            )
        }

        // 提醒时间
        if (alarmSettings.alarmTimeList.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(Interval.DefaultPadding))
                SettingCategoryTitle(stringResource(R.string.alarm_time))
                FlowRow {
                    val sortedTime = alarmSettings.alarmTimeList.toMutableList()
                    sortedTime.sort()
                    sortedTime.forEach { timeMillis ->
                        InputChip(
                            modifier = Modifier.padding(horizontal = Interval.ChipPadding),
                            selected = false,
                            onClick = {},
                            label = {
                                val hour = timeMillis / 3600
                                val minute = (timeMillis / 60) % 60
                                Text(
                                    String.format(
                                        LocalLocale.current.platformLocale,
                                        "%02d:%02d",
                                        hour,
                                        minute
                                    )
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    modifier = Modifier.size(Size.bodySmallIcon),
                                    onClick = { vm.removeAlarmTime(timeMillis) }
                                ) {
                                    Icon(
                                        modifier = Modifier.size(Size.bodySmallIcon),
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.delete_alarm_time)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // 底部权限提醒
        if (isAutoStartDefined()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TipTitle(title = stringResource(R.string.tip_alarm_need_autostart))
            }
        }
    }

    // 时间选择对话框
    if (timePickerVisible) {
        val currentTime = LocalTime.now()
        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.hour,
            initialMinute = currentTime.minute
        )

        TimePickerDialog(
            onDismissRequest = { timePickerVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    val seconds = timePickerState.hour * 3600 + timePickerState.minute * 60
                    vm.addAlarmTime(seconds)
                    timePickerVisible = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    timePickerVisible = false
                }) { Text("取消") }
            },
            title = {
                Text(stringResource(R.string.select_alarm_time))
            },
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DiaryAlarmScreenPreview() {
    MaterialAppTheme {
        DiaryAlarmScreen({})
    }
}