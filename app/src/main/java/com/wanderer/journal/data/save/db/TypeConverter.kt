package com.wanderer.journal.data.save.db

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

object TypeConverter {
    /**
     * 将 [Uri] 转换为字符串。
     *
     * @param uri 需要转换为字符串的 [Uri] 对象
     * @return 由 [Uri] 对象转换得到的字符串；入参为 null 时返回 null
     */
    @TypeConverter
    @JvmStatic
    fun fromUri(uri: Uri?): String? = uri?.toString()

    /**
     * 将字符串转换为 [Uri] 对象。
     *
     * @param uriStr 需要转换为 [Uri] 的字符串
     * @return 转换得到的 [Uri] 对象；入参为 null 时返回 null
     */
    @TypeConverter
    @JvmStatic
    fun toUri(uriStr: String?): Uri? = uriStr?.toUri()

    /**
     * 将 [LocalDateTime] 类型转换为 [Long] 时间戳类型。
     *
     * @param dateTime 需要转换的 [LocalDateTime] 对象
     * @return UTC 时区的毫秒级时间戳；入参为 null 时返回 null
     */
    @TypeConverter
    @JvmStatic
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? =
        dateTime?.toInstant(ZoneOffset.UTC)?.toEpochMilli()

    /**
     * 将 UTC 时间戳转换为 [LocalDateTime] 类型。
     *
     * @param timestamp UTC 毫秒级时间戳
     * @return 转换后的 [LocalDateTime] 对象；入参为 null 时返回 null
     */
    @TypeConverter
    @JvmStatic
    fun toLocalDateTime(timestamp: Long?): LocalDateTime? =
        timestamp?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) }

    /**
     * 将 [LocalDate] 类型转换为时间戳。
     *
     * @param date 需要转换为时间戳的 [LocalDate] 对象
     * @return 转换后的 UTC 毫秒级时间戳；入参为 null 时返回 null
     */
    @TypeConverter
    @JvmStatic
    fun fromLocalDate(date: LocalDate?): Long? =
        date?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli()

    /**
     * 将 UTC 时间戳转换为 [LocalDate] 类型。
     *
     * @param timestamp 需要转换的 UTC 毫秒级时间戳
     * @return 转换后的 [LocalDate] 对象；入参为 null 时返回 null
     */
    @TypeConverter
    @JvmStatic
    fun toLocalDate(timestamp: Long?): LocalDate? =
        timestamp?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
}