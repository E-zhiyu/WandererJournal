package com.wanderer.journal.data.save.store

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.InvalidProtocolBufferException
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