package com.wanderer.journal.ui.pages.main.settings.sub

import android.content.Context
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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.wanderer.journal.R
import com.wanderer.journal.automation.worker.WorkerScheduler
import com.wanderer.journal.automation.worker.backup.BackupWorker
import com.wanderer.journal.automation.worker.backup.RestoreWorker
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.auxiliary.enums.settings.BackupFrequency
import com.wanderer.journal.auxiliary.enums.types.BackupDataType
import com.wanderer.journal.auxiliary.enums.unique.KeyStrings
import com.wanderer.journal.data.save.store.viewmodels.AutoBackupViewModel
import com.wanderer.journal.data.save.store.viewmodels.dsAutoBackupViewModel
import com.wanderer.journal.helpers.file.FileHelper
import com.wanderer.journal.helpers.file.generateBackupFileName
import com.wanderer.journal.helpers.file.getReadablePathFromSafUri
import com.wanderer.journal.helpers.file.rememberCreateDocumentLauncherManual
import com.wanderer.journal.helpers.file.rememberDocumentLauncher
import com.wanderer.journal.helpers.file.rememberDocumentTreeLauncher
import com.wanderer.journal.ui.components.ComposeCheckedTextView
import com.wanderer.journal.ui.components.LazyLargeTopAppBarScaffold
import com.wanderer.journal.ui.components.dialogs.DialogScaffold
import com.wanderer.journal.ui.pages.main.settings.SettingCategoryTitle
import com.wanderer.journal.ui.pages.main.settings.SettingClickableItem
import com.wanderer.journal.ui.pages.main.settings.SettingSpinnerItem
import com.wanderer.journal.ui.pages.main.settings.SettingSwitchItem
import com.wanderer.journal.ui.theme.MaterialAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.UUID

// 多选对话框请求
private sealed class SafRequest {
    data object Export : SafRequest()  // 数据导出
    data object Import : SafRequest()  // 数据导入
    data class SelectBackupDir(val initialUri: String) : SafRequest()   //选择自动备份的目录
}

private val snackMessageChannel = Channel<String>(Channel.BUFFERED) // Snackbar 一次性文本频道

@Composable
fun DataManageScreen(
    onNavigateBack: () -> Unit,
    vm: AutoBackupViewModel = dsAutoBackupViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifeOwner = LocalLifecycleOwner.current

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
    val selectBackupDir = rememberDocumentTreeLauncher { uri ->
        uri?.let {
            vm.setBackupDir(it.toString())
            scope.launch {
                snackMessageChannel.send(
                    "已将备份目录设置为${
                        getReadablePathFromSafUri(
                            context,
                            it
                        )
                    }"
                )
            }
        }
    }

    // 多选对话框请求
    var safRequest by remember { mutableStateOf<SafRequest?>(null) }
    val dataTypes = BackupDataType.entries
    var selectedTypes by remember { mutableStateOf(dataTypes.toSet()) }

    // 用于打开文件的 SAF 打开函数
    val tipImportStarted = stringResource(R.string.tip_import_started)
    val openDocumentLauncher = rememberDocumentLauncher { uri ->
        uri?.let {
            val choice = dataTypes.map { type -> selectedTypes.contains(type) }
            val data = Data.Builder()
                .putBooleanArray(KeyStrings.BACKUP_CHOICES.v(), choice.toBooleanArray())
                .putString(KeyStrings.BACKUP_TARGET.v(), it.toString())
                .build()
            val uuid =
                WorkerScheduler.executeWorkOnceNow(context, RestoreWorker::class.java, data)
            observeWorker(uuid, context, scope, lifeOwner)

            scope.launch {
                snackMessageChannel.send(tipImportStarted)
            }
        }
    }

    // 用于创建文件的 SAF 打开函数
    val tipExportStarted = stringResource(R.string.tip_export_started)
    val createDocumentLauncher = rememberCreateDocumentLauncherManual { uri ->
        uri?.let {
            val choice = dataTypes.map { u -> selectedTypes.contains(u) }
            val data = Data.Builder()
                .putBooleanArray(KeyStrings.BACKUP_CHOICES.v(), choice.toBooleanArray())
                .putString(KeyStrings.BACKUP_TARGET.v(), it.toString())
                .build()
            val uuid = WorkerScheduler.executeWorkOnceNow(context, BackupWorker::class.java, data)
            observeWorker(uuid, context, scope, lifeOwner)

            scope.launch {
                snackMessageChannel.send(tipExportStarted)
            }
        }
    }

    // 界面内容
    DataManageContent(
        onNavigateBack = onNavigateBack,
        snackHostState = snackHostState,
        openSaf = { safRequest = it },
        vm = vm
    )

    // 多选对话框
    safRequest?.let {
        when (it) {
            is SafRequest.Export -> {
                DialogScaffold(
                    onDismiss = { safRequest = null },
                    onConfirm = {
                        createDocumentLauncher(
                            "application/zip",
                            generateBackupFileName()
                        )
                    },
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
                            text = type.title,
                        )
                    }
                }
            }

            is SafRequest.Import -> {
                DialogScaffold(
                    onDismiss = { safRequest = null },
                    onConfirm = { openDocumentLauncher(arrayOf("application/zip")) },
                    title = stringResource(R.string.import_data)
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

            is SafRequest.SelectBackupDir -> {
                selectBackupDir(it.initialUri)
                safRequest = null
            }
        }
    }
}

@Composable
private fun DataManageContent(
    onNavigateBack: () -> Unit,
    snackHostState: SnackbarHostState,
    openSaf: (safRequest: SafRequest) -> Unit,
    vm: AutoBackupViewModel = dsAutoBackupViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 设置项
    val backupSettings by vm.autoBackup.collectAsStateWithLifecycle()

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
                    openSaf(SafRequest.Export)
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
                    openSaf(SafRequest.Import)
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
                    openSaf(SafRequest.SelectBackupDir(backupSettings.backupDirectory))
                },
                normalRadius = Radius.BottomArray
            )
        }
    }
}

/**
 * 观察 Worker 的工作状态
 */
private fun observeWorker(
    uuid: UUID,
    context: Context,
    scope: CoroutineScope,
    owner: LifecycleOwner
) {
    WorkManager.getInstance(context)
        .getWorkInfoByIdLiveData(uuid)
        .observe(owner) { workInfo ->
            if (workInfo == null) return@observe

            //判断 Worker 是否运行结束
            if (!workInfo.state.isFinished) return@observe

            // 显示 SnackBar
            val snackMessage = when (workInfo.state) {
                WorkInfo.State.SUCCEEDED -> context.getString(R.string.tip_worker_succeed)
                else -> context.getString(R.string.tip_worker_failed)
            }
            scope.launch { snackMessageChannel.send(snackMessage) }
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