package com.example.recipehub.utils

import androidx.room.TypeConverter
import com.example.recipehub.model.Rating
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.util.Date

class Converters {
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
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

    @TypeConverter
    fun fromRatingList(ratingList: List<Rating>?): String {
        val gson = Gson()
        val type = object : TypeToken<List<Rating>>() {}.type
        return gson.toJson(ratingList ?: emptyList<Rating>(), type)
    }

    @TypeConverter
    fun toRatingList(ratingString: String?): List<Rating> {
        val gson = Gson()
        val listType = object : TypeToken<List<Rating>>() {}.type
        return gson.fromJson(ratingString, listType) ?: emptyList()
    }
}
