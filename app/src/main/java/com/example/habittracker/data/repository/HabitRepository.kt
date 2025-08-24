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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

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

        println("DEBUG: === INSERTING HABIT ===")
        println("DEBUG: Habit: ${savedHabit.title}")
        println("DEBUG: Unit Type: ${savedHabit.unitType}")
        println("DEBUG: Target: ${savedHabit.target}")
        println("DEBUG: Reminder Time: ${savedHabit.reminderTime}")

        // Schedule appropriate reminder type based on habit characteristics
        savedHabit.reminderTime?.let { reminderTime ->
            if (savedHabit.unitType == UnitType.COUNT && savedHabit.target != null && savedHabit.target > 1) {
                println("DEBUG: Using SMART REMINDERS for count habit")
                // Use smart reminders for count habits with targets > 1
                alarmScheduler.scheduleSmartReminders(
                    habitId = savedHabit.id,
                    habitTitle = savedHabit.title,
                    target = savedHabit.target,
                    baseReminderTime = reminderTime
                )
            } else {
                println("DEBUG: Using SINGLE REMINDER for boolean/simple habit")
                // Use single reminder for boolean habits or count habits with target = 1
                alarmScheduler.scheduleReminder(
                    savedHabit.id,
                    savedHabit.title,
                    reminderTime
                )
            }
        } ?: println("DEBUG: No reminder time set, skipping reminder scheduling")

        return habitId
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)

        println("DEBUG: === UPDATING HABIT ===")
        println("DEBUG: Habit: ${habit.title}")
        println("DEBUG: Unit Type: ${habit.unitType}")
        println("DEBUG: Target: ${habit.target}")
        println("DEBUG: Reminder Time: ${habit.reminderTime}")

        // Cancel existing reminders first
        alarmScheduler.cancelReminder(habit.id)

        // Schedule new reminders based on updated habit
        habit.reminderTime?.let { reminderTime ->
            if (habit.unitType == UnitType.COUNT && habit.target != null && habit.target > 1) {
                println("DEBUG: Using SMART REMINDERS for updated count habit")
                alarmScheduler.scheduleSmartReminders(
                    habitId = habit.id,
                    habitTitle = habit.title,
                    target = habit.target,
                    baseReminderTime = reminderTime
                )
            } else {
                println("DEBUG: Using SINGLE REMINDER for updated boolean/simple habit")
                alarmScheduler.scheduleReminder(habit.id, habit.title, reminderTime)
            }
        } ?: println("DEBUG: No reminder time set for updated habit")
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

     suspend fun getNextScheduledReminders(habitId: Long): List<String> {
        val habit = getHabitById(habitId) ?: return emptyList()

        if (habit.reminderTime == null) return emptyList()

        if (habit.unitType != UnitType.COUNT || habit.target == null || habit.target <= 1) {
            // Single reminder for boolean habits
            val nextReminder = getNextOccurrence(habit.reminderTime)
            return listOf("Next: ${formatDateTime(nextReminder)}")
        }

        // Smart reminders for count habits
        val reminderTimes = calculateSmartReminderTimes(habit.target, habit.reminderTime)
        val now = LocalDateTime.now()

        return reminderTimes
            .map { getNextOccurrence(it) }
            .filter { it.isAfter(now) }
            .take(4)
            .mapIndexed { index, dateTime ->
                when (index) {
                    0 -> "Next: ${formatDateTime(dateTime)}"
                    else -> "Then: ${formatDateTime(dateTime)}"
                }
            }
    }

    private fun getNextOccurrence(time: LocalTime): LocalDateTime {
        val now = LocalDateTime.now()
        var scheduledTime = LocalDateTime.of(now.toLocalDate(), time)

        if (scheduledTime.isBefore(now) || scheduledTime.isEqual(now)) {
            scheduledTime = scheduledTime.plusDays(1)
        }

        return scheduledTime
    }


    private fun formatDateTime(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val timeStr = dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))

        return when {
            dateTime.toLocalDate() == now.toLocalDate() -> "Today $timeStr"
            dateTime.toLocalDate() == now.toLocalDate().plusDays(1) -> "Tomorrow $timeStr"
            else -> dateTime.format(DateTimeFormatter.ofPattern("MMM d 'at' h:mm a"))
        }
    }

    private fun calculateSmartReminderTimes(target: Int, baseReminderTime: LocalTime?): List<LocalTime> {
        // Copy the same logic from AlarmScheduler for consistency
        val reminderTimes = mutableListOf<LocalTime>()

        // Development mode: Short intervals
        val isDevelopmentMode = false // Match AlarmScheduler setting

        if (isDevelopmentMode) {
            val now = LocalTime.now()
            val reminderCount = when {
                target <= 2 -> 2
                target <= 4 -> 3
                target <= 6 -> 4
                else -> 5
            }

            repeat(reminderCount) { index ->
                val testTime = now.plusMinutes((index + 1) * 2L)
                reminderTimes.add(testTime)
            }

            return reminderTimes
        }

        // Production mode logic (same as AlarmScheduler)
        when {
            target <= 2 -> {
                reminderTimes.add(LocalTime.of(9, 0))
                reminderTimes.add(LocalTime.of(19, 0))
            }
            target <= 4 -> {
                reminderTimes.add(LocalTime.of(9, 0))
                reminderTimes.add(LocalTime.of(14, 0))
                reminderTimes.add(LocalTime.of(20, 0))
            }
            target <= 6 -> {
                reminderTimes.add(LocalTime.of(9, 0))
                reminderTimes.add(LocalTime.of(12, 30))
                reminderTimes.add(LocalTime.of(16, 0))
                reminderTimes.add(LocalTime.of(20, 0))
            }
            else -> {
                reminderTimes.add(LocalTime.of(8, 0))
                reminderTimes.add(LocalTime.of(11, 30))
                reminderTimes.add(LocalTime.of(15, 0))
                reminderTimes.add(LocalTime.of(18, 0))
                reminderTimes.add(LocalTime.of(21, 0))
            }
        }

        // Replace closest with user's preferred time
        baseReminderTime?.let { userTime ->
            val userHour = userTime.hour
            val closestIndex = reminderTimes.mapIndexed { index, time ->
                index to kotlin.math.abs(time.hour - userHour)
            }.minByOrNull { it.second }?.first ?: 0

            reminderTimes[closestIndex] = userTime
        }

        return reminderTimes.sorted()
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