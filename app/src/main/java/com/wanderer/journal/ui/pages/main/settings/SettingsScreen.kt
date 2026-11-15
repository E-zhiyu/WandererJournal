package com.wanderer.journal.ui.pages.main.settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wanderer.journal.R
import com.wanderer.journal.WandererJournal
import com.wanderer.journal.auxiliary.classes.CHANGELOG
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.auxiliary.enums.settings.AuthOpportunity
import com.wanderer.journal.auxiliary.enums.settings.FirstScreen
import com.wanderer.journal.auxiliary.enums.settings.ThemeMode
import com.wanderer.journal.data.save.preference.AppSettingsPreference
import com.wanderer.journal.data.save.preference.MediaPreference
import com.wanderer.journal.data.save.preference.SecurityPreference
import com.wanderer.journal.data.save.preference.VersionPreference
import com.wanderer.journal.helpers.BiometricHelper
import com.wanderer.journal.helpers.UpdateHelper
import com.wanderer.journal.helpers.appearance.ThemeHelper
import com.wanderer.journal.ui.pages.main.settings.sub.AboutActivity
import com.wanderer.journal.ui.pages.main.settings.sub.DataManageActivity
import com.wanderer.journal.ui.pages.main.settings.sub.DiaryAlarmActivity
import com.wanderer.journal.ui.pages.main.settings.sub.PermissionManageActivity
import com.wanderer.journal.ui.pages.main.settings.sub.ShareSettingsActivity
import com.wanderer.journal.ui.pages.main.settings.sub.media.MediaListActivity
import com.wanderer.journal.ui.theme.MaterialAppTheme
import dev.jeziellago.compose.markdowntext.MarkdownText
import io.reactivex.rxjava3.disposables.CompositeDisposable

@Composable
fun SettingsScreen(
    disposable: CompositeDisposable
) {
    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity

    // 各项设置状态
    var dynamicColor by remember { mutableStateOf(AppSettingsPreference.getDynamicColorStat(context)) }
    var autoUpdate by remember { mutableStateOf(VersionPreference.getAutoUpdateCheck(context)) }
    var hdrDisplay by remember { mutableStateOf(MediaPreference.getHdrDisplay(context)) }
    var authSwitch by remember { mutableStateOf(SecurityPreference.getAuthSwitchStat(context)) }
    var hideRecentTask by remember { mutableStateOf(SecurityPreference.getHideRecentTask(context)) }

    // 下拉菜单展开状态
    var firstScreenExpanded by remember { mutableStateOf(false) }
    var authOpportunityExpanded by remember { mutableStateOf(false) }

    // 对话框显示状态
    var themeModeDialogShown by remember { mutableStateOf(false) }
    var changeLogDialogShown by remember { mutableStateOf(false) }

    // 当前选中的文字描述
    var firstScreenText by remember {
        mutableStateOf(FirstScreen.entries[AppSettingsPreference.getFirstScreen(context)].title)
    }
    var authOpportunityText by remember {
        mutableStateOf(AuthOpportunity.entries[SecurityPreference.getAuthOpportunity(context)].title)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings)) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(start = Interval.SidePadding, end = Interval.SidePadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ==================== 1. App 设置 ====================
            item { SettingCategoryTitle(stringResource(id = R.string.app_settings)) }

            // 主题模式
            item {
                SettingClickableItem(
                    title = stringResource(id = R.string.theme_mode),
                    subtitle = "切换深浅色模式",
                    icon = painterResource(id = R.drawable.outline_dark_mode_24),
                    normalRadius = Radius.TopArray,
                    onClick = { themeModeDialogShown = true }
                )
            }

            // 动态配色 (仅 Android 12L+ 支持)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
                item {
                    Spacer(modifier = Modifier.height(1.dp))
                    SettingSwitchItem(
                        title = stringResource(id = R.string.dynamic_color),
                        subtitle = "将壁纸颜色作为APP主题色",
                        icon = painterResource(id = R.drawable.outline_colorize_24),
                        normalRadius = Radius.MiddleArray,
                        checked = dynamicColor,
                        onCheckedChange = { checked ->
                            dynamicColor = checked
                            AppSettingsPreference.setDynamicColorStat(context, checked)
                            activity?.let {
                                ThemeHelper.switchDynamicColorWithAnimation(
                                    it,
                                    checked
                                )
                            }
                        }
                    )
                }
            }

            // 启动界面 (Spinner)
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingSpinnerItem(
                    title = stringResource(id = R.string.select_first_screen),
                    subtitle = "选择启动的第一屏",
                    normalRadius = Radius.MiddleArray,
                    currentValueText = firstScreenText,
                    icon = painterResource(id = R.drawable.outline_mobile_24),
                    expanded = firstScreenExpanded,
                    onExpandedChange = { firstScreenExpanded = it }
                ) {
                    FirstScreen.entries.forEach { screen ->
                        DropdownMenuItem(
                            text = { Text(screen.title) },
                            onClick = {
                                firstScreenExpanded = false
                                AppSettingsPreference.setFirstScreen(context, screen.ordinal)
                                firstScreenText = screen.title
                            }
                        )
                    }
                }
            }

            // 自动检测更新
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingSwitchItem(
                    title = stringResource(id = R.string.auto_update_check),
                    subtitle = "应用启动后自动检查更新",
                    icon = painterResource(id = R.drawable.outline_deployed_code_update_24),
                    checked = autoUpdate,
                    normalRadius = Radius.BottomArray,
                    onCheckedChange = { checked ->
                        autoUpdate = checked
                        VersionPreference.setAutoUpdateCheck(context, checked)
                    }
                )
            }

            // ==================== 2. 通用设置 ====================
            item { SettingCategoryTitle(stringResource(id = R.string.common_settings)) }

            item {
                SettingClickableItem(
                    title = stringResource(id = R.string.permissions_setting),
                    subtitle = "跳转至权限管理界面",
                    icon = painterResource(id = R.drawable.outline_admin_panel_settings_24),
                    normalRadius = Radius.TopArray,
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                PermissionManageActivity::class.java
                            )
                        )
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingClickableItem(
                    title = stringResource(id = R.string.data_manage),
                    subtitle = "跳转至数据管理界面",
                    normalRadius = Radius.MiddleArray,
                    icon = painterResource(id = R.drawable.outline_database_24),
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                DataManageActivity::class.java
                            )
                        )
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingClickableItem(
                    title = stringResource(id = R.string.diary_alarm),
                    subtitle = "跳转至日记提醒设置界面",
                    normalRadius = Radius.MiddleArray,
                    icon = painterResource(id = R.drawable.outline_alarm_24),
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                DiaryAlarmActivity::class.java
                            )
                        )
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingClickableItem(
                    title = stringResource(id = R.string.share_settings),
                    subtitle = "跳转至分享设置界面",
                    icon = painterResource(id = R.drawable.outline_share_24),
                    normalRadius = Radius.BottomArray,
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                ShareSettingsActivity::class.java
                            )
                        )
                    }
                )
            }

            // ==================== 3. 媒体设置 ====================
            item { SettingCategoryTitle(stringResource(id = R.string.media_settings)) }

            item {
                SettingSwitchItem(
                    title = stringResource(id = R.string.hdr_display_effect),
                    subtitle = "全屏查看图片时支持HDR显示",
                    icon = painterResource(id = R.drawable.outline_hdr_on_24),
                    checked = hdrDisplay,
                    normalRadius = Radius.TopArray,
                    onCheckedChange = { checked ->
                        hdrDisplay = checked
                        MediaPreference.setHdrDisplay(context, checked)
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingClickableItem(
                    title = stringResource(id = R.string.media_list),
                    subtitle = "点击进入媒体文件列表",
                    icon = painterResource(id = R.drawable.outline_perm_media_24),
                    normalRadius = Radius.BottomArray,
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                MediaListActivity::class.java
                            )
                        )
                    }
                )
            }

            // ==================== 4. 隐私设置 ====================
            item { SettingCategoryTitle(stringResource(id = R.string.privacy_settings)) }

            // 身份验证开关（带生物识别逻辑）
            item {
                SettingSwitchItem(
                    title = stringResource(id = R.string.authentication),
                    subtitle = "进入APP时需要进行身份验证",
                    icon = painterResource(id = R.drawable.outline_security_24),
                    checked = authSwitch,
                    normalRadius = Radius.TopArray,
                    onCheckedChange = { targetChecked ->
                        val title = if (targetChecked) "启用身份验证" else "关闭身份验证"
                        activity?.let {
                            BiometricHelper.showBiometricPrompt(
                                title,
                                "请验证您的身份",
                                it,
                                object : BiometricHelper.AuthCallback {
                                    override fun onSuccess() {
                                        authSwitch = targetChecked
                                        SecurityPreference.setAuthSwitchStat(context, targetChecked)
                                        android.widget.Toast.makeText(
                                            context,
                                            if (targetChecked) "已启用身份验证" else "已关闭身份验证",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onError(errCode: Int, errStr: CharSequence) {
                                        android.widget.Toast.makeText(
                                            context,
                                            errStr,
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        if (errCode == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE || errCode == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                                            authSwitch = false
                                            SecurityPreference.setAuthSwitchStat(context, false)
                                        } else {
                                            authSwitch = !targetChecked
                                        }
                                    }

                                    override fun onFailed() {}
                                }
                            )
                        }
                    }
                )
            }

            // 身份验证时机
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingSpinnerItem(
                    title = stringResource(id = R.string.authentication_opportunity),
                    subtitle = "进行身份验证的时机",
                    currentValueText = authOpportunityText,
                    normalRadius = Radius.MiddleArray,
                    icon = painterResource(id = R.drawable.outline_safety_check_24),
                    expanded = authOpportunityExpanded,
                    onExpandedChange = { authOpportunityExpanded = it }
                ) {
                    AuthOpportunity.entries.forEach { opportunity ->
                        DropdownMenuItem(
                            text = { Text(opportunity.title) },
                            onClick = {
                                authOpportunityExpanded = false
                                SecurityPreference.setAuthOpportunity(context, opportunity.ordinal)
                                authOpportunityText = opportunity.title
                            }
                        )
                    }
                }
            }

            // 最近任务隐藏
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingSwitchItem(
                    title = stringResource(id = R.string.hide_recent_task),
                    subtitle = "在最近任务列表中隐藏",
                    icon = painterResource(id = R.drawable.outline_visibility_off_24),
                    checked = hideRecentTask,
                    normalRadius = Radius.BottomArray,
                    onCheckedChange = { checked ->
                        hideRecentTask = checked
                        SecurityPreference.setHideRecentTask(checked, context)
                        WandererJournal.hideTaskFromRecents(context, checked)
                    }
                )
            }

            // ==================== 5. 关于软件 ====================
            item { SettingCategoryTitle(stringResource(id = R.string.about_software)) }

            item {
                SettingClickableItem(
                    title = stringResource(id = R.string.about_software),
                    icon = painterResource(id = R.drawable.outline_info_24),
                    normalRadius = Radius.TopArray,
                    onClick = { context.startActivity(Intent(context, AboutActivity::class.java)) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingClickableItem(
                    title = stringResource(id = R.string.changelog),
                    normalRadius = Radius.MiddleArray,
                    icon = painterResource(id = R.drawable.outline_lab_profile_24),
                    onClick = { changeLogDialogShown = true }
                )
            }
            item {
                Spacer(modifier = Modifier.height(1.dp))
                SettingClickableItem(
                    title = stringResource(id = R.string.update_check),
                    icon = painterResource(id = R.drawable.outline_update_24),
                    normalRadius = Radius.BottomArray,
                    onClick = {
                        android.widget.Toast.makeText(
                            context,
                            "正在检查更新……",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        disposable.add(UpdateHelper.checkUpdate(context, true))
                    }
                )
            }
        }
    }

    // 主题模式选择对话框
    if (themeModeDialogShown) {
        ThemeModeDialog(
            onDismiss = { themeModeDialogShown = false },
            onSelected = { code ->
                AppSettingsPreference.setThemeMode(context, code)
                ThemeHelper.switchNightModeWithAnimation(code)
            }
        )
    }

    // 更新日志对话框
    if (changeLogDialogShown) {
        ChangeLogDialog(
            onDismiss = { changeLogDialogShown = false }
        )
    }
}

/**
 * 主题模式选择对话框
 */
@Composable
fun ThemeModeDialog(
    onDismiss: () -> Unit,
    onSelected: (Int) -> Unit
) {
    val themeModes = remember { ThemeMode.entries.toTypedArray() }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("主题模式") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                themeModes.forEach { themeMode ->
                    TextButton(
                        onClick = { onSelected(themeMode.ordinal) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = themeMode.title,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 更新日志对话框
 */
@Composable
fun ChangeLogDialog(
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        modifier = Modifier.padding(top = 40.dp, bottom = 40.dp),
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.changelog)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                MarkdownText(CHANGELOG)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showSystemUi = true
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun Preview() {
    MaterialAppTheme {
        SettingsScreen(CompositeDisposable())
    }
}