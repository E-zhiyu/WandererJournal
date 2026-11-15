package com.wanderer.journal.data.save.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.datastore.migrations.SharedPreferencesMigration
import com.wanderer.journal.AppSettings
import com.wanderer.journal.AutoBackup
import com.wanderer.journal.DiaryAlarm
import com.wanderer.journal.DiaryDraft
import com.wanderer.journal.DiaryShare
import com.wanderer.journal.SearchHistory
import com.wanderer.journal.SecuritySettings
import com.wanderer.journal.Update
import com.wanderer.journal.auxiliary.enums.settings.BackupFrequency
import com.wanderer.journal.auxiliary.enums.settings.FirstScreen
import com.wanderer.journal.auxiliary.enums.settings.ThemeMode

// 草稿
val Context.diaryDraftStore: DataStore<DiaryDraft> by dataStore(
    fileName = "diary_draft.pb",
    serializer = DiaryDraftSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { DiaryDraft.getDefaultInstance() }
)

// 搜索历史
val Context.searchHistoryStore: DataStore<SearchHistory> by dataStore(
    fileName = "search_history.pb",
    serializer = SearchHistorySerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { SearchHistory.getDefaultInstance() }
)

// 其余设置项
val Context.appSettingStore: DataStore<AppSettings> by dataStore(
    fileName = "app_setting.pb",
    serializer = AppSettingSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { AppSettings.getDefaultInstance() },
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "ThemePreference"
            ) { prefs, current ->
                val builder = current.toBuilder()

                // 主题模式
                if (current.themeMode == 0) {
                    builder.setThemeMode(
                        prefs.getInt(
                            "theme_mode",
                            ThemeMode.FOLLOW_SYSTEM.ordinal
                        )
                    )
                }

                // 动态色彩
                if (!current.dynamicColor) {
                    builder.setDynamicColor(prefs.getBoolean("dynamic_color", true))
                }

                // 开屏界面
                if (current.firstScreen == 0) {
                    builder.setFirstScreen(prefs.getInt("first_screen", FirstScreen.HOME.ordinal))
                }

                builder.build()
            }
        )
    }
)
val Context.autoBackupStore: DataStore<AutoBackup> by dataStore(
    fileName = "auto_backup.pb",
    serializer = AutoBackupSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { AutoBackup.getDefaultInstance() },
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "AutoBackupPreference"
            ) { prefs, current ->
                val builder = current.toBuilder()

                // 自动备份开关
                if (!current.switchStat) {
                    builder.setSwitchStat(prefs.getBoolean("switch_stat", false))
                }

                // 备份目录
                if (current.backupDirectory.isEmpty()) {
                    builder.setBackupDirectory(prefs.getString("backup_directory", ""))
                }

                // 备份频率
                if (current.backupFrequency == 0) {
                    builder.setBackupFrequency(
                        prefs.getInt(
                            "backup_frequency",
                            BackupFrequency.DAY.ordinal
                        )
                    )
                }

                builder.build()
            }
        )
    }
)
val Context.diaryAlarmStore: DataStore<DiaryAlarm> by dataStore(
    fileName = "diary_alarm.pb",
    serializer = DiaryAlarmSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { DiaryAlarm.getDefaultInstance() },
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "DiaryAlarmPreference"
            ) { prefs, current ->
                val builder = current.toBuilder()

                // 开关
                if (!current.switch) {
                    builder.setSwitch(prefs.getBoolean("switch", false))
                }

                // 提醒时间
                if (current.alarmTimeList.isEmpty()) {
                    val oldTimeJson = prefs.getString("alarm_time", "[]")
                    oldTimeJson?.let {
                        val timeList: List<String> = Json.decodeFromString(oldTimeJson)
                        builder.addAllAlarmTime(timeList)
                    }
                }

                builder.build()
            }
        )
    }
)
val Context.securityStore: DataStore<SecuritySettings> by dataStore(
    fileName = "security.pb",
    serializer = SecuritySerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { SecuritySettings.getDefaultInstance() }
)
val Context.diaryShareStore: DataStore<DiaryShare> by dataStore(
    fileName = "diary_share.pb",
    serializer = DiaryShareSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { DiaryShare.getDefaultInstance() }
)
val Context.updateStore: DataStore<Update> by dataStore(
    fileName = "update.pb",
    serializer = UpdateSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { Update.getDefaultInstance() }
)