package com.example.habittracker.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.habittracker.data.entities.Entry
import com.example.habittracker.data.entities.Habit
import com.example.habittracker.data.entities.UnitType
import java.time.LocalDate

object StreakCalculator {

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateCurrentStreak(
        habit: Habit,
        entries: List<Entry>,
        endDate: LocalDate = LocalDate.now()
    ): Int {

        var streak = 0
        var currentDate = endDate

        //Go backwards day by day
        while (true) {
            //check if day is scheduled
            if (ScheduleUtils.isDayScheduled(habit.schedule, currentDate.dayOfWeek)) {
                val entry = entries.find {
                    it.localDate == currentDate
                }

                if (isEntryCompleted(habit, entry)) {
                    streak++
                } else {
                    break
                }
            }

            //skip unscheduled days
            currentDate = currentDate.minusDays(1)

            // Safety: don't go back more than reasonable time
            if (currentDate.isBefore(endDate.minusDays(1000))) break

        }

        return streak
    }

    private fun isEntryCompleted(habit: Habit, entry: Entry?): Boolean {
        return when (habit.unitType) {
            UnitType.COUNT -> {
                val count = entry?.valueCount ?: 0
                if (habit.target != null) {
                    count >= habit.target
                } else {
                    count > 0
                }
            }

            UnitType.BOOLEAN -> {
                entry?.valueBool == true
            }
        }
    }
}