package com.example.habittracker.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String? {
        return date?.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let {
            LocalDate.parse(it)
        }
    }

    @TypeConverter
    fun fromLocalTime(time: LocalTime): String? {
        return time?.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime?{
        return timeString?.let {
            LocalTime.parse(it)
        }
    }

}