package com.wanderer.journal.ui.pages.main.settings.sub

import android.content.res.Configuration
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.data.save.store.viewmodels.DiaryAlarmViewModel
import com.wanderer.journal.data.save.store.viewmodels.dsDiaryAlarmViewModel
import com.wanderer.journal.ui.pages.main.settings.SettingCategoryTitle
import com.wanderer.journal.ui.pages.main.settings.SettingClickableItem
import com.wanderer.journal.ui.pages.main.settings.SettingSwitchItem
import com.wanderer.journal.ui.theme.MaterialAppTheme

@Composable
fun DiaryAlarmScreen(
    onNavigateBack: () -> Unit,
    vm: DiaryAlarmViewModel = dsDiaryAlarmViewModel()
) {
    val scrollState = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    // 设置项
    val alarmSettings by vm.diaryAlarm.collectAsStateWithLifecycle()

    // 时间选择对话框
    var timePickerVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(id = R.string.diary_alarm)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollState
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .nestedScroll(scrollState.nestedScrollConnection)
                .padding(horizontal = Interval.SidePadding),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
        ) {
            // 总开关
            item {
                SettingSwitchItem(
                    title = stringResource(R.string.alarm_total_switch),
                    subtitle = stringResource(R.string.alarm_total_switch_subtitle),
                    icon = painterResource(R.drawable.outline_perm_media_24),
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
                    icon = painterResource(R.drawable.outline_text_ad_24),
                    normalRadius = Radius.SingleArray,
                    onClick = { timePickerVisible = true }
                )
            }

            // 提醒时间
            if (alarmSettings.alarmTimeList.isNotEmpty()) {
                item {
                    SettingCategoryTitle(stringResource(R.string.alarm_time))
                    FlowRow {
                        alarmSettings.alarmTimeList.forEach { timeStr ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = {
                                    Text(timeStr)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // TODO:时间选择对话框
    if (timePickerVisible) {

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