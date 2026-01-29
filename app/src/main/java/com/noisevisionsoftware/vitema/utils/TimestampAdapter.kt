package com.noisevisionsoftware.vitema.utils

import com.google.firebase.Timestamp
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class TimestampAdapter : TypeAdapter<Timestamp>() {
    override fun write(out: JsonWriter, value: Timestamp?) {
        value?.let {
            out.value(it.seconds * 1000 + it.nanoseconds / 1000000)
        } ?: out.nullValue()
    }

    override fun read(input: JsonReader): Timestamp? {
        val value = input.nextLong()
        return Timestamp(value / 1000, ((value % 1000) * 1000000).toInt())
    }
}