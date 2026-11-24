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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.auxiliary.classes.Size
import com.wanderer.journal.data.save.store.viewmodels.DiaryAlarmViewModel
import com.wanderer.journal.data.save.store.viewmodels.dsDiaryAlarmViewModel
import com.wanderer.journal.helpers.permission.PermissionRequest
import com.wanderer.journal.helpers.permission.isAutoStartDefined
import com.wanderer.journal.ui.components.LazyLargeTopAppBarScaffold
import com.wanderer.journal.ui.components.dialogs.PermissionExplanationDialog
import com.wanderer.journal.ui.pages.main.settings.SettingCategoryTitle
import com.wanderer.journal.ui.pages.main.settings.SettingClickableItem
import com.wanderer.journal.ui.pages.main.settings.SettingSwitchItem
import com.wanderer.journal.ui.pages.main.settings.TipTitle
import com.wanderer.journal.ui.theme.MaterialAppTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.time.LocalTime

private val snackMessageChannel = Channel<String>(Channel.BUFFERED) // Snackbar 一次性文本频道

@Composable
fun DiaryAlarmScreen(
    onNavigateBack: () -> Unit,
    vm: DiaryAlarmViewModel = dsDiaryAlarmViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 设置项
    val alarmSettings by vm.diaryAlarm.collectAsStateWithLifecycle()

    // Snackbar
    val snackHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        for (message in snackMessageChannel) {
            if (message.isNotEmpty()) {
                snackHostState.showSnackbar(message)
            }
        }
    }

    // 权限解释对话框
    var permissionRequest by remember { mutableStateOf<PermissionRequest?>(null) }

    // 时间选择对话框
    var timePickerVisible by remember { mutableStateOf(false) }

    LazyLargeTopAppBarScaffold(
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.diary_alarm),
        snackHostState = { SnackbarHost(snackHostState) }
    ) {
        // 总开关
        item {
            SettingSwitchItem(
                title = stringResource(R.string.total_switch),
                subtitle = stringResource(R.string.alarm_total_switch_subtitle),
                icon = painterResource(R.drawable.outline_alarm_24),
                normalRadius = Radius.TopArray,
                checked = alarmSettings.switch,
                onCheckedChange = { checked -> vm.setSwitchOpen(checked) },
                shouldCheckedChange = { checked ->
                    !checked || XXPermissions.isGrantedPermission(
                        context,
                        PermissionLists.getScheduleExactAlarmPermission()
                    )
                },
                onCheckedChangeIntercepted = {
                    permissionRequest = PermissionRequest.Common(
                        descriptionFun = { "该功能需要精确闹钟权限，是否前往授权？" },
                        onAllGranted = {
                            vm.setSwitchOpen(true)
                        },
                        onRequested = { grantedList, _ ->
                            if (grantedList.isNotEmpty()) {
                                vm.setSwitchOpen(true)
                                scope.launch { snackMessageChannel.send("权限已授予，已开启日记提醒功能。") }
                            }
                        },
                        permissions = listOf(PermissionLists.getScheduleExactAlarmPermission())
                    )
                }
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

    // 权限请求解释对话框
    permissionRequest?.let {
        PermissionExplanationDialog(
            permissionRequest = it,
            onDismiss = { permissionRequest = null }
        )
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