package com.wanderer.journal.ui.pages.main.settings.sub.permission

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import com.wanderer.journal.R
import com.wanderer.journal.WandererJournal
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.helpers.permission.isAutoStartDefined
import com.wanderer.journal.helpers.permission.requestAutoStartPermission
import com.wanderer.journal.helpers.permission.requestLocationPermission
import com.wanderer.journal.helpers.permission.requestPermission
import com.wanderer.journal.ui.pages.main.settings.SettingClickableItem
import com.wanderer.journal.ui.theme.MaterialAppTheme
import dev.jeziellago.compose.markdowntext.MarkdownText

class PermissionEvent(
    val message: String,
    val permissionList: List<IPermission>? = null,
    val onConfirm: (() -> Unit)? = null
)

@Composable
fun PermissionScreen(
    onNavigateBack: () -> Unit,
    vm: PermissionScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val scrollState = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    // Snackbar
    val snackHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        for (message in vm.getMessageChannel()) {
            if (message.isNotEmpty()) {
                snackHostState.showSnackbar(message)
            }
        }
    }

    // 权限申请被拒绝的对话框
    var permissionEvent: PermissionEvent? by remember { mutableStateOf(null) }

    // 权限申请通用回调
    val allGrantedCallback: (List<IPermission>) -> Unit = { vm.sendSnackMessage("权限已授予") }
    val requestedCallback: (List<IPermission>, List<IPermission>) -> Unit = { grantedList, _ ->
        if (grantedList.isNotEmpty()) {
            vm.sendSnackMessage("权限已授予")
        } else {
            vm.sendSnackMessage("权限申请被拒绝")
        }
    }

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
        },
        snackbarHost = { SnackbarHost(snackHostState) }
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
                Spacer(modifier = Modifier.height(Interval.DefaultPadding))
                SettingClickableItem(
                    title = stringResource(R.string.camera_permission),
                    subtitle = stringResource(R.string.camera_permission_subtitle),
                    onClick = {
                        requestPermission(
                            context,
                            activity,
                            descriptionFun = { "该权限允许应用调用摄像头，应用范围如下：\n- 写日记时使用系统相机添加媒体文件。\n" },
                            onAllGranted = allGrantedCallback,
                            onDoNotAskAgain = { permissionList ->
                                permissionEvent = PermissionEvent(
                                    message = "权限申请被拒绝，请前往权限设置界面授权。",
                                    permissionList = permissionList
                                )
                            },
                            onRequested = requestedCallback,
                            PermissionLists.getCameraPermission()
                        )
                    },
                    onLongClick = {
                        WandererJournal.lockLifecycleObserver(true)
                        XXPermissions.startPermissionActivity(
                            context,
                            PermissionLists.getCameraPermission()
                        )
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
                        requestPermission(
                            context,
                            activity,
                            descriptionFun = {
                                "该权限允许应用发送通知，应用范围如下：\n- 没写日记时发送提醒通知；\n- 进行数据备份时发送反馈通知。\n"
                            },
                            onAllGranted = allGrantedCallback,
                            onDoNotAskAgain = { permissionList ->
                                permissionEvent = PermissionEvent(
                                    message = "权限申请被拒绝，请前往权限设置界面授权。",
                                    permissionList = permissionList
                                )
                            },
                            onRequested = requestedCallback,
                            PermissionLists.getPostNotificationsPermission()
                        )
                    },
                    onLongClick = {
                        WandererJournal.lockLifecycleObserver(true)
                        XXPermissions.startPermissionActivity(
                            context,
                            PermissionLists.getCameraPermission()
                        )
                    },
                    icon = painterResource(R.drawable.outline_notification_settings_24),
                    normalRadius = Radius.MiddleArray
                )
            }

            // 自启动权限
            if (isAutoStartDefined()) {
                item {
                    Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
                    SettingClickableItem(
                        title = stringResource(R.string.auto_start_permission),
                        subtitle = stringResource(R.string.auto_start_permission_subtitle),
                        onClick = {
                            permissionEvent = PermissionEvent(
                                message = "该权限允许应用在后台退出后自动启动，若关闭该权限会导致某些自动化任务无法正常运行，影响范围如下：\n" +
                                        "- 日记提醒是否准时触发。\n",
                                onConfirm = {
                                    requestAutoStartPermission(context)
                                }
                            )
                        },
                        onLongClick = {
                            WandererJournal.lockLifecycleObserver(true)
                            requestAutoStartPermission(context)
                        },
                        icon = painterResource(R.drawable.outline_autorenew_24),
                        normalRadius = Radius.MiddleArray
                    )
                }
            }

            // 电池优化策略
            item {
                Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
                SettingClickableItem(
                    title = stringResource(R.string.ignore_battery_optimization),
                    subtitle = stringResource(R.string.ignore_battery_optimization_subtitle),
                    onClick = {
                        requestPermission(
                            context,
                            activity,
                            descriptionFun = {
                                "该设置项可以调整应用的省电策略。" +
                                        "如果需要确保应用在后台能够运行，请在电池优化设置界面找到对应应用，点击开关左侧的文本并勾选“无限制”。" +
                                        "影响范围如下：\n" +
                                        "- 日记提醒是否能够准点发送通知。\n"
                            },
                            onAllGranted = allGrantedCallback,
                            onDoNotAskAgain = { permissionList ->
                                permissionEvent = PermissionEvent(
                                    message = "权限申请被拒绝，请前往权限设置界面授权。",
                                    permissionList = permissionList
                                )
                            },
                            onRequested = requestedCallback,
                            PermissionLists.getRequestIgnoreBatteryOptimizationsPermission()
                        )
                    },
                    onLongClick = {
                        WandererJournal.lockLifecycleObserver(true)
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        context.startActivity(intent)
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
                        requestPermission(
                            context,
                            activity,
                            descriptionFun = {
                                "该权限允许应用执行某些定时任务，以实现一些自动化功能，应用范围如下：\n" +
                                        "- 根据用户设置自动检测当天是否有日记并发送通知提醒。\n"
                            },
                            onAllGranted = allGrantedCallback,
                            onDoNotAskAgain = { permissionList ->
                                permissionEvent = PermissionEvent(
                                    message = "权限申请被拒绝，请前往权限设置界面授权。",
                                    permissionList = permissionList
                                )
                            },
                            onRequested = requestedCallback,
                            PermissionLists.getScheduleExactAlarmPermission()
                        )
                    },
                    onLongClick = {
                        WandererJournal.lockLifecycleObserver(true)
                        XXPermissions.startPermissionActivity(
                            context,
                            PermissionLists.getScheduleExactAlarmPermission()
                        )
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
                        requestLocationPermission(
                            context,
                            activity,
                            descriptionFun = {
                                "该权限允许应用获取设备所处的位置，应用范围如下：\n" +
                                        "- 写日记时将位置信息保存至日记段落中。\n"
                            },
                            hasFineLocation = { vm.sendSnackMessage("已授予精确位置权限") },
                            onDoNotAskAgain = { permissionList ->
                                permissionEvent = PermissionEvent(
                                    message = "权限申请被拒绝，请前往权限设置界面授权。",
                                    permissionList = permissionList
                                )
                            },
                            onRequested = { grantedList, deniedList ->
                                val noDenied = deniedList.isEmpty()
                                val noGranted = grantedList.isEmpty()
                                if (noGranted && !noDenied) {
                                    vm.sendSnackMessage("权限申请被拒绝")
                                } else if (noDenied && !noGranted) {
                                    vm.sendSnackMessage("已授予精确位置权限")
                                } else {
                                    vm.sendSnackMessage("已授予粗略位置权限，若需要更改精确程度，可长按定位权限选项进入设置界面手动调节")
                                }
                            }
                        )
                    },
                    onLongClick = {
                        WandererJournal.lockLifecycleObserver(true)
                        XXPermissions.startPermissionActivity(
                            context,
                            PermissionLists.getAccessFineLocationPermission()
                        )
                    },
                    icon = painterResource(R.drawable.outline_location_on_24),
                    normalRadius = Radius.BottomArray
                )
            }
        }
    }

    // 权限申请信息对话框
    permissionEvent?.let {
        PermissionRequestDialog(
            message = it.message,
            onDismissRequest = { permissionEvent = null },
            onConfirm = {
                if (it.onConfirm == null) {
                    it.permissionList?.let { permissions ->
                        XXPermissions.startPermissionActivity(
                            context,
                            permissions
                        )
                    }
                } else {
                    it.onConfirm()
                }
                permissionEvent = null
            }
        )
    }
}

@Composable
private fun PermissionRequestDialog(
    message: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        modifier = Modifier.padding(top = 40.dp, bottom = 40.dp),
        onDismissRequest = onDismissRequest,
        title = {
            Text(stringResource(R.string.request_permission))
        },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                MarkdownText(message)
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.to_settings))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
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