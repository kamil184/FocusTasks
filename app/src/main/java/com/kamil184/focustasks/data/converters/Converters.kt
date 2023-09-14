package com.kamil184.focustasks.data.converters

import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Calendar? {
        return value?.let {
            val calendar = Calendar.getInstance()
            calendar.time = Date(it)
            calendar
        }
    }

    @TypeConverter
    fun fromCalendar(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun booleansFromInt(value: Int?): BooleanArray? {
        value?.let {
            val booleans = BooleanArray(7)
            for (i in booleans.indices) {
                booleans[i] = (it shr i) and 1 == 1
            }
            return booleans
        }
        return null
    }

    @TypeConverter
    fun intFromBooleans(booleans: BooleanArray?): Int? {
        booleans?.let {
            var value = 0
            var factor = 1
            for (i in it.indices) {
                if (it[i]) value += factor
                factor *= 2
            }
            return value
        }
        return null
    }

    @TypeConverter
    fun fromIntPair(pair: Pair<Int, Int>?): Long? {
        pair?.let {
            return it.first.toLong() shl 32 or (it.second.toLong() and 0xffffffffL)
        }
        return null
    }

    @TypeConverter
    fun toIntPair(l: Long?): Pair<Int, Int>? {
        l?.let {
            val x = (l shr 32).toInt()
            val y = l.toInt()
            return Pair(x, y)
        }
        return null
    }
}