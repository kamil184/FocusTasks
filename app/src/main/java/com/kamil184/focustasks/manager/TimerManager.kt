package com.kamil184.focustasks.manager

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.kamil184.focustasks.TimerPreferences
import com.kamil184.focustasks.model.Timer
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

private val Context.timerProtoDataStore: DataStore<TimerPreferences> by dataStore(
    fileName = "timer.proto",
    serializer = TimerSerializer
)

class TimerManager(private val context: Context) {
    val timerFlow = context.timerProtoDataStore.data.map {
        Timer(
            length = it.length,
            state = it.state,
            timeRemaining = it.timeRemaining
        )
    }

    suspend fun updateTimerState(timer: Timer) {
        context.timerProtoDataStore.updateData {
            it.toBuilder()
                .setLength(timer.length.value!!)
                .setState(TimerPreferences.TimerState.values()[timer.state.value!!.ordinal])
                .setTimeRemaining(timer.timeRemaining.value!!)
                .build()
        }
    }
}

object TimerSerializer : Serializer<TimerPreferences> {
    override val defaultValue: TimerPreferences = TimerPreferences.getDefaultInstance().toBuilder()
        .setLength(1 * 60_000)
        .setState(TimerPreferences.TimerState.Stopped)
        .setTimeRemaining(1 * 60_000)
        .build()

    override suspend fun readFrom(input: InputStream): TimerPreferences {
        try {
            return TimerPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: TimerPreferences, output: OutputStream) = t.writeTo(output)
}