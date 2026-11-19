package com.wanderer.journal.data.save.store

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.InvalidProtocolBufferException
import com.wanderer.journal.AppSettings
import com.wanderer.journal.AutoBackup
import com.wanderer.journal.DiaryAlarm
import com.wanderer.journal.DiaryDraft
import com.wanderer.journal.DiaryShare
import com.wanderer.journal.SearchHistory
import com.wanderer.journal.SecuritySettings
import com.wanderer.journal.Update
import com.wanderer.journal.auxiliary.enums.settings.AuthOpportunity
import com.wanderer.journal.auxiliary.enums.settings.BackupFrequency
import com.wanderer.journal.auxiliary.enums.settings.FirstScreen
import com.wanderer.journal.auxiliary.enums.settings.ThemeMode
import java.io.InputStream
import java.io.OutputStream

abstract class ProtoSerializer<T : GeneratedMessageLite<T, *>>(
    override val defaultValue: T
) : Serializer<T> {
    override suspend fun readFrom(input: InputStream): T {
        try {
            return parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        t.writeTo(output)
    }

    protected abstract fun parseFrom(input: InputStream): T
}

object AppSettingSerializer :
    ProtoSerializer<AppSettings>(
        defaultValue = AppSettings.newBuilder()
            .setDynamicColor(true)
            .setFirstScreen(FirstScreen.HOME.ordinal)
            .setThemeMode(ThemeMode.FOLLOW_SYSTEM.ordinal)
            .setShowHdr(true)
            .build()
    ) {
    override fun parseFrom(input: InputStream): AppSettings = AppSettings.parseFrom(input)
}

object AutoBackupSerializer :
    ProtoSerializer<AutoBackup>(
        defaultValue = AutoBackup.newBuilder()
            .setBackupDirectory("")
            .setSwitchStat(false)
            .setBackupFrequency(BackupFrequency.WEEK.ordinal)
            .build()
    ) {
    override fun parseFrom(input: InputStream): AutoBackup = AutoBackup.parseFrom(input)
}

object DiaryAlarmSerializer :
    ProtoSerializer<DiaryAlarm>(defaultValue = DiaryAlarm.getDefaultInstance()) {
    override fun parseFrom(input: InputStream): DiaryAlarm = DiaryAlarm.parseFrom(input)
}

object DiaryDraftSerializer :
    ProtoSerializer<DiaryDraft>(defaultValue = DiaryDraft.getDefaultInstance()) {
    override fun parseFrom(input: InputStream): DiaryDraft = DiaryDraft.parseFrom(input)
}

object SearchHistorySerializer :
    ProtoSerializer<SearchHistory>(defaultValue = SearchHistory.getDefaultInstance()) {
    override fun parseFrom(input: InputStream): SearchHistory = SearchHistory.parseFrom(input)
}

object SecuritySerializer :
    ProtoSerializer<SecuritySettings>(
        defaultValue = SecuritySettings.newBuilder()
            .setAuthSwitch(false)
            .setExcludeRecentTask(false)
            .setAuthOpportunity(AuthOpportunity.EVERY_TIME.ordinal)
            .build()
    ) {
    override fun parseFrom(input: InputStream): SecuritySettings = SecuritySettings.parseFrom(input)
}

object DiaryShareSerializer :
    ProtoSerializer<DiaryShare>(
        defaultValue = DiaryShare.newBuilder()
            .setTime(true)
            .setMedia(true)
            .setEmotion(true)
            .setLocation(true)
            .setBottomText("")
            .build()
    ) {
    override fun parseFrom(input: InputStream): DiaryShare = DiaryShare.parseFrom(input)
}

object UpdateSerializer : ProtoSerializer<Update>(
    defaultValue = Update.newBuilder()
        .setAutoUpdateCheck(true)
        .setApkUri("")
        .setSkipVersionCode(0)
        .build()
) {
    override fun parseFrom(input: InputStream): Update = Update.parseFrom(input)
}