package com.example.recipehub.utils

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.util.Date

class Converters {

    // Date to Long and vice versa
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    // List<String> to String (JSON) and vice versa
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.let {
            Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
        } ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return Gson().toJson(list ?: emptyList<String>())
    }
}
