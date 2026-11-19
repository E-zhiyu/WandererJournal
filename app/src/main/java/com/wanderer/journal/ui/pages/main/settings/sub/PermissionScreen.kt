package com.wanderer.journal.ui.pages.main.settings.sub

import android.content.res.Configuration
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
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.ui.pages.main.settings.SettingClickableItem
import com.wanderer.journal.ui.theme.MaterialAppTheme

@Composable
fun PermissionScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(id = R.string.permissions_settings)) },
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
            // 相机权限
            item {
                SettingClickableItem(
                    title = stringResource(R.string.camera_permission),
                    subtitle = stringResource(R.string.camera_permission_subtitle),
                    onClick = {
                        //TODO:相机权限
                    },
                    icon = painterResource(R.drawable.outline_photo_camera_24),
                    normalRadius = Radius.TopArray
                )
            }

            // 通知权限
            item {
                Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
                SettingClickableItem(
                    title = stringResource(R.string.notification_permission),
                    subtitle = stringResource(R.string.notification_permission_subtitle),
                    onClick = {
                        //TODO:通知权限
                    },
                    icon = painterResource(R.drawable.outline_notification_settings_24),
                    normalRadius = Radius.MiddleArray
                )
            }

            // 自启动权限
            item {
                Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
                SettingClickableItem(
                    title = stringResource(R.string.auto_start_permission),
                    subtitle = stringResource(R.string.auto_start_permission_subtitle),
                    onClick = {
                        //TODO:
                    },
                    icon = painterResource(R.drawable.outline_autorenew_24),
                    normalRadius = Radius.MiddleArray
                )
            }

            // 电池优化策略
            item {
                Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
                SettingClickableItem(
                    title = stringResource(R.string.ignore_battery_optimization),
                    subtitle = stringResource(R.string.ignore_battery_optimization_subtitle),
                    onClick = {
                        //TODO:
                    },
                    icon = painterResource(R.drawable.outline_battery_android_frame_3_24),
                    normalRadius = Radius.MiddleArray
                )
            }

            // 精确闹钟权限
            item {
                Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
                SettingClickableItem(
                    title = stringResource(R.string.alarm_permission),
                    subtitle = stringResource(R.string.alarm_permission_subtitle),
                    onClick = {
                        //TODO:
                    },
                    icon = painterResource(R.drawable.outline_alarm_24),
                    normalRadius = Radius.MiddleArray
                )
            }

            // 定位权限
            item {
                Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
                SettingClickableItem(
                    title = stringResource(R.string.location_permission),
                    subtitle = stringResource(R.string.location_permission_subtitle),
                    onClick = {
                        //TODO:
                    },
                    icon = painterResource(R.drawable.outline_location_on_24),
                    normalRadius = Radius.BottomArray
                )
            }
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
private fun Preview() {
    MaterialAppTheme {
        PermissionScreen(
            onNavigateBack = {}
        )
    }
}