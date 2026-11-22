package com.wanderer.journal.ui.pages.main.settings.sub

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.auxiliary.enums.settings.BackupFrequency
import com.wanderer.journal.data.save.store.viewmodels.AutoBackupViewModel
import com.wanderer.journal.data.save.store.viewmodels.dsAutoBackupViewModel
import com.wanderer.journal.helpers.file.SAFHelper
import com.wanderer.journal.ui.components.LazyLargeTopAppBarScaffold
import com.wanderer.journal.ui.pages.main.settings.SettingCategoryTitle
import com.wanderer.journal.ui.pages.main.settings.SettingClickableItem
import com.wanderer.journal.ui.pages.main.settings.SettingSpinnerItem
import com.wanderer.journal.ui.theme.MaterialAppTheme

@Composable
fun DataManageScreen(
    onNavigateBack: () -> Unit,
    vm: AutoBackupViewModel = dsAutoBackupViewModel()
) {
    val context = LocalContext.current

    // 设置项
    val backupSettings by vm.autoBackup.collectAsStateWithLifecycle()

    // 备份频率下拉框
    val backupFrequencies = BackupFrequency.entries
    var frequencyExpanded by remember { mutableStateOf(false) }

    LazyLargeTopAppBarScaffold(
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.data_manage),
        useDefaultTopPadding = false
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
                    // TODO:完成回调
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
                    // TODO:完成回调
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
            SettingClickableItem(
                title = stringResource(R.string.total_switch),
                subtitle = stringResource(R.string.auto_backup_switch_subtitle),
                icon = painterResource(R.drawable.outline_settings_backup_restore_24),
                onClick = {
                    // TODO:完成回调
                },
                normalRadius = Radius.TopArray
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
                // TODO:为什么这里会多减少一个卡片高度？
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
                else SAFHelper.getReadablePathFromSafUri(
                    context,
                    Uri.parse(backupSettings.backupDirectory)
                ),
                icon = painterResource(R.drawable.outline_settings_backup_restore_24),
                onClick = {
                    // TODO:完成回调
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