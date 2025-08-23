package com.example.habittracker.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.habittracker.data.dao.EntryDao
import com.example.habittracker.data.dao.HabitDao
import com.example.habittracker.data.entities.Entry
import com.example.habittracker.data.entities.Habit
import com.example.habittracker.data.entities.UnitType
import com.example.habittracker.notifications.AlarmScheduler
import com.example.habittracker.utils.ScheduleUtils
import com.example.habittracker.utils.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val entryDao: EntryDao,
    private val alarmScheduler: AlarmScheduler,
) {


    //Habit ops
    fun getAllActiveHabits(): Flow<List<Habit>> = habitDao.getAllActiveHabits()

    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)
    suspend fun getArchivedHabits(): Flow<List<Habit>> {
        return habitDao.getArchivedHabits()
    }

    suspend fun deleteHabit(id: Long) {
        habitDao.deleteHabit(id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insertHabit(habit: Habit): Long {
        val habitId = habitDao.insertHabit(habit)
        val savedHabit = habit.copy(id = habitId)
        // Schedule reminder if time exists
        savedHabit.reminderTime?.let {
            alarmScheduler.scheduleReminder(
                savedHabit.id,
                savedHabit.title,
                it
            )
        }
        return habitId
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
        alarmScheduler.cancelReminder(habit.id)
        habit.reminderTime?.let {
            alarmScheduler.scheduleReminder(habit.id, habit.title, it)
        }

    }

    suspend fun archiveHabit(id: Long) {
        habitDao.setHabitArchived(id, true)
        alarmScheduler.cancelReminder(id)

    }

    //Today's habits with their entries
    @RequiresApi(Build.VERSION_CODES.O)
    fun getHabitsForToday(): Flow<List<HabitWithEntry>> {
        val today = LocalDate.now()
        val todayBit = ScheduleUtils.getDayBit(today.dayOfWeek)

        return combine(
            habitDao.getHabitsForDay(todayBit), entryDao.getEntriesForDate(today)
        ) { habits, entries ->
            habits.map { habit ->
                val entry = entries.find { it.habitId == habit.id }
                HabitWithEntry(habit, entry)
            }

        }
    }


    //Entry ops
    suspend fun getEntry(habitId: Long, date: LocalDate): Entry? = entryDao.getEntry(habitId, date)

    suspend fun insertOrUpdateEntry(entry: Entry) = entryDao.insertEntry(entry)

    suspend fun deleteEntry(entry: Entry) = entryDao.deleteEntry(entry)

    fun getEntriesForHabit(habitId: Long): Flow<List<Entry>> = entryDao.getEntriesForHabit(habitId)

    //Streak Calc
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getStreakForHabit(habitId: Long): Int {
        val habit = getHabitById(habitId) ?: return 0
        val today = LocalDate.now()
        val entries = entryDao.getEntriesInRange(
            habitId = habitId, startDate = today.minusDays(365), endDate = today
        )

        return StreakCalculator.calculateCurrentStreak(habit, entries, today)
    }

    suspend fun unarchiveHabit(id: Long) {
        habitDao.setHabitArchived(id, false)
        // (Optionally) reschedule reminders if needed
    }


}

data class HabitWithEntry(
    val habit: Habit, val entry: Entry?
) {
    val isCompleted: Boolean
        get() = when (habit.unitType) {
            UnitType.BOOLEAN -> entry?.valueBool == true
            UnitType.COUNT -> {
                val count = entry?.valueCount ?: 0
                if (habit.target != null) {
                    count >= habit.target
                } else {
                    count > 0
                }
            }
        }
}