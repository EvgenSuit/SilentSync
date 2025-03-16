package com.suit.dndCalendar.impl.data.criteriaDb

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class CriteriaConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object: TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
    @TypeConverter
    fun fromList(list: List<String>): String =
        gson.toJson(list)
}