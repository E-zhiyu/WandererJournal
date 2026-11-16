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
import com.wanderer.journal.MediaSettings
import com.wanderer.journal.SearchHistory
import com.wanderer.journal.SecuritySettings
import com.wanderer.journal.Update
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

object AppSettingSerializer : ProtoSerializer<AppSettings>(AppSettings.getDefaultInstance()) {
    override fun parseFrom(input: InputStream): AppSettings = AppSettings.parseFrom(input)
}

object AutoBackupSerializer : ProtoSerializer<AutoBackup>(AutoBackup.getDefaultInstance()) {
    override fun parseFrom(input: InputStream): AutoBackup = AutoBackup.parseFrom(input)
}

object DiaryAlarmSerializer : ProtoSerializer<DiaryAlarm>(DiaryAlarm.getDefaultInstance()) {
    override fun parseFrom(input: InputStream): DiaryAlarm = DiaryAlarm.parseFrom(input)
}

object DiaryDraftSerializer : ProtoSerializer<DiaryDraft>(DiaryDraft.getDefaultInstance()) {
    override fun parseFrom(input: InputStream): DiaryDraft = DiaryDraft.parseFrom(input)
}

object SearchHistorySerializer :
    ProtoSerializer<SearchHistory>(SearchHistory.getDefaultInstance()) {
    override fun parseFrom(input: InputStream): SearchHistory = SearchHistory.parseFrom(input)
}

object SecuritySerializer :
    ProtoSerializer<SecuritySettings>(SecuritySettings.getDefaultInstance()) {
    override fun parseFrom(input: InputStream): SecuritySettings = SecuritySettings.parseFrom(input)
}

object DiaryShareSerializer : ProtoSerializer<DiaryShare>(DiaryShare.getDefaultInstance()) {
    override fun parseFrom(input: InputStream): DiaryShare = DiaryShare.parseFrom(input)
}

object MediaSerializer : ProtoSerializer<MediaSettings>(MediaSettings.getDefaultInstance()) {
    override fun parseFrom(input: InputStream): MediaSettings = MediaSettings.parseFrom(input)
}

object UpdateSerializer : ProtoSerializer<Update>(Update.getDefaultInstance()) {
    override fun parseFrom(input: InputStream): Update = Update.parseFrom(input)
}