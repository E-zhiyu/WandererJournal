package com.wanderer.journal.ui.pages.main.settings.sub

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.auxiliary.enums.settings.BackupFrequency
import com.wanderer.journal.auxiliary.enums.types.BackupDataType
import com.wanderer.journal.data.save.store.viewmodels.AutoBackupViewModel
import com.wanderer.journal.data.save.store.viewmodels.dsAutoBackupViewModel
import com.wanderer.journal.helpers.file.getReadablePathFromSafUri
import com.wanderer.journal.helpers.file.rememberCreateDocumentLauncherManual
import com.wanderer.journal.helpers.file.rememberDocumentLauncher
import com.wanderer.journal.helpers.file.rememberDocumentTreeLauncher
import com.wanderer.journal.ui.components.ComposeCheckedTextView
import com.wanderer.journal.ui.components.LazyLargeTopAppBarScaffold
import com.wanderer.journal.ui.components.dialogs.MultiChoiceDialog
import com.wanderer.journal.ui.pages.main.settings.SettingCategoryTitle
import com.wanderer.journal.ui.pages.main.settings.SettingClickableItem
import com.wanderer.journal.ui.pages.main.settings.SettingSpinnerItem
import com.wanderer.journal.ui.pages.main.settings.SettingSwitchItem
import com.wanderer.journal.ui.theme.MaterialAppTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

// 多选对话框请求
private sealed class ChoiceDialogRequest {
    // 数据导出
    data object Export : ChoiceDialogRequest()

    // 数据导入
    data object Import : ChoiceDialogRequest()
}

private val snackMessageChannel = Channel<String>(Channel.BUFFERED) // Snackbar 一次性文本频道

@Composable
fun DataManageScreen(
    onNavigateBack: () -> Unit,
    vm: AutoBackupViewModel = dsAutoBackupViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 设置项
    val backupSettings by vm.autoBackup.collectAsStateWithLifecycle()

    // Snackbar
    val snackHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        for (message in snackMessageChannel) {
            if (message.isNotEmpty()) {
                snackHostState.showSnackbar(message)
            }
        }
    }

    // 打开 SAF 并选择目录的函数（备份目录选择）
    val openDocumentTree = rememberDocumentTreeLauncher { uri ->
        uri?.let {
            vm.setBackupDir(it.toString())
        }
    }

    // 多选对话框请求
    var choiceDialogRequest by remember { mutableStateOf<ChoiceDialogRequest?>(null) }

    // 打开 SAF 并选择文件的函数（导入数据）
    val openDocument = rememberDocumentLauncher { uri ->
        uri?.let {
            // TODO:启动数据导入流程
        }
    }

    // 打开 SAF 并创建文件的函数（备份数据）
    val createDocument = rememberCreateDocumentLauncherManual { uri ->
        // TODO:启动数据备份流程
    }

    // 备份频率下拉框
    val backupFrequencies = BackupFrequency.entries
    var frequencyExpanded by remember { mutableStateOf(false) }

    LazyLargeTopAppBarScaffold(
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.data_manage),
        useDefaultTopPadding = false,
        snackHostState = { SnackbarHost(snackHostState) }
    ) {
        // 手动导入和导出标题
        item {
            SettingCategoryTitle(stringResource(R.string.backup_and_restore))
        }

        // 导出数据
        item {
            SettingClickableItem(
                title = stringResource(R.string.export_data),
                subtitle = stringResource(R.string.export_data_subtitle),
                icon = painterResource(R.drawable.outline_file_export_24),
                onClick = {
                    choiceDialogRequest = ChoiceDialogRequest.Export
                },
                normalRadius = Radius.TopArray
            )
        }

        // 导入数据
        item {
            Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
            SettingClickableItem(
                title = stringResource(R.string.import_data),
                subtitle = stringResource(R.string.import_data_subtitle),
                icon = painterResource(R.drawable.outline_download_24),
                onClick = {
                    choiceDialogRequest = ChoiceDialogRequest.Import
                },
                normalRadius = Radius.BottomArray
            )
        }

        // 自动备份标题
        item {
            SettingCategoryTitle(stringResource(R.string.auto_backup))
        }

        // 总开关
        item {
            SettingSwitchItem(
                title = stringResource(R.string.total_switch),
                subtitle = stringResource(R.string.auto_backup_switch_subtitle),
                icon = painterResource(R.drawable.outline_settings_backup_restore_24),
                onCheckedChange = { checked ->
                    vm.setSwitchOpen(checked)
                },
                normalRadius = Radius.TopArray,
                checked = backupSettings.switchStat,
                shouldCheckedChange = { checked ->
                    !checked || backupSettings.backupDirectory.isNotEmpty()
                },
                onCheckedChangeIntercepted = {
                    scope.launch { snackMessageChannel.send("请先设置备份目录再启用自动备份") }
                }
            )
        }

        // 备份频率
        item {
            Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
            SettingSpinnerItem(
                title = stringResource(id = R.string.backup_frequency),
                subtitle = stringResource(R.string.backup_frequency_subtitle),
                icon = painterResource(R.drawable.outline_timer_24),
                normalRadius = Radius.MiddleArray,
                currentValueText = backupFrequencies[backupSettings.backupFrequency].title,
                onExpandedChange = { frequencyExpanded = it },
                expanded = frequencyExpanded,
            ) {
                backupFrequencies.forEach { frequency ->
                    SelectableDropdownMenuItem(
                        selected = frequency.ordinal == backupSettings.backupFrequency,
                        text = { Text(frequency.title) },
                        onClick = {
                            frequencyExpanded = false
                            vm.setBackupFrequency(frequency.ordinal)
                        },
                        shapes = MenuDefaults.itemShape(
                            frequency.ordinal,
                            backupFrequencies.size
                        )
                    )
                }
            }
        }

        // 备份目录
        item {
            Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
            SettingClickableItem(
                title = stringResource(R.string.backup_directory),
                subtitle = if (backupSettings.backupDirectory.isEmpty())
                    stringResource(R.string.backup_directory_subtitle)
                else getReadablePathFromSafUri(
                    context,
                    backupSettings.backupDirectory.toUri()
                ),
                icon = painterResource(R.drawable.outline_folder_data_24),
                onClick = {
                    openDocumentTree(backupSettings.backupDirectory)
                },
                normalRadius = Radius.BottomArray
            )
        }

        // 其他标题
        item {
            SettingCategoryTitle(stringResource(R.string.others))
        }

        // 段落追加
        item {
            SettingClickableItem(
                title = stringResource(R.string.append_paragraph),
                subtitle = stringResource(R.string.append_paragraph_subtitle),
                icon = painterResource(R.drawable.outline_convert_to_text_24),
                onClick = {
                    // TODO:完成回调
                },
                normalRadius = Radius.TopArray
            )
        }

        // 日记导入
        item {
            SettingClickableItem(
                title = stringResource(R.string.import_diary),
                subtitle = stringResource(R.string.import_diary_subtitle),
                icon = painterResource(R.drawable.outline_note_stack_add_24),
                onClick = {
                    // TODO:完成回调
                },
                normalRadius = Radius.BottomArray
            )
        }
    }

    // 多选对话框
    choiceDialogRequest?.let {
        val dataTypes = BackupDataType.entries
        var selectedTypes by remember { mutableStateOf(dataTypes.toSet()) }

        val onConfirm = {
            when (it) {
                is ChoiceDialogRequest.Export -> {
                    // TODO:导出确认回调
                }

                is ChoiceDialogRequest.Import -> {
                    // TODO:导入确认回调
                }
            }
        }

        MultiChoiceDialog(
            onDismiss = { choiceDialogRequest = null },
            onConfirm = onConfirm,
            title = stringResource(R.string.export_data)
        ) {
            dataTypes.forEach { type ->
                ComposeCheckedTextView(
                    checked = selectedTypes.contains(type),
                    onCheckedChange = { checked ->
                        // 3. 不可变集合的操作非常简洁
                        selectedTypes = if (checked) {
                            selectedTypes + type // 添加
                        } else {
                            selectedTypes - type // 移除
                        }
                    },
                    text = type.title
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
private fun DataManageScreenPreview() {
    MaterialAppTheme {
        DataManageScreen(
            {}
        )
    }
}