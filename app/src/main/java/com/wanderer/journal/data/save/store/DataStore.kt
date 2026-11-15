package com.wanderer.journal.data.save.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import com.wanderer.journal.AppSettings
import com.wanderer.journal.AutoBackup
import com.wanderer.journal.DiaryAlarm
import com.wanderer.journal.DiaryDraft
import com.wanderer.journal.DiaryShare
import com.wanderer.journal.SearchHistory
import com.wanderer.journal.SecuritySettings
import com.wanderer.journal.Update

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
    corruptionHandler = ReplaceFileCorruptionHandler { AppSettings.getDefaultInstance() }
)
val Context.autoBackupStore: DataStore<AutoBackup> by dataStore(
    fileName = "auto_backup.pb",
    serializer = AutoBackupSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { AutoBackup.getDefaultInstance() }
)
val Context.diaryAlarmStore: DataStore<DiaryAlarm> by dataStore(
    fileName = "diary_alarm.pb",
    serializer = DiaryAlarmSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { DiaryAlarm.getDefaultInstance() }
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