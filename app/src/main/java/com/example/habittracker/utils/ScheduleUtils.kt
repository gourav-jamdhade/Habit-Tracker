package com.example.habittracker.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek

object ScheduleUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayBit(dayOfWeek: DayOfWeek): Int {

        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 4
            DayOfWeek.THURSDAY -> 8
            DayOfWeek.FRIDAY -> 16
            DayOfWeek.SATURDAY -> 32
            DayOfWeek.SUNDAY -> 64
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isDayScheduled(schedule: Int, dayOfWeek: DayOfWeek): Boolean {
        val dayBit = getDayBit(dayOfWeek)
        return (schedule and dayBit) > 0
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createSchedule(selectedDays : Set<DayOfWeek>):Int{
        return selectedDays.sumOf { getDayBit(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getScheduledDays(schedule:Int):Set<DayOfWeek>{
        return DayOfWeek.values().filter {
            isDayScheduled(schedule, it)
        }.toSet()
    }

    const val DAILY_SCHEDULE = 127

    const val WEEKDAYS = 31
}