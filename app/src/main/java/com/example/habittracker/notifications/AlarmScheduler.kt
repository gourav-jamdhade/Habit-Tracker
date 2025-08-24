package com.example.habittracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleReminder(habitId: Long, habitTitle: String, reminderTime: LocalTime) {
        println("DEBUG: Scheduling reminder for $habitTitle at $reminderTime")

        // Cancel any existing reminders first
        cancelReminder(habitId)

        val now = LocalDateTime.now()
        var scheduledTime = LocalDateTime.of(now.toLocalDate(), reminderTime)

        // If time has passed today, schedule for tomorrow
        if (scheduledTime.isBefore(now) || scheduledTime.isEqual(now)) {
            scheduledTime = scheduledTime.plusDays(1)
            println("DEBUG: Time has passed, scheduling for tomorrow: $scheduledTime")
        }

        val triggerAtMillis =
            scheduledTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_id", habitId)
            putExtra("habit_title", habitTitle)
            putExtra("reminder_type", "main")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            println("DEBUG: ✅ Main reminder scheduled for $scheduledTime")
        } catch (e: Exception) {
            println("DEBUG: ❌ Failed to schedule main reminder: ${e.message}")
        }
    }

    // NEW: Smart reminders for count-based habits
    suspend fun scheduleSmartReminders(
        habitId: Long,
        habitTitle: String,
        target: Int,
        baseReminderTime: LocalTime? = null
    ) {
        println("DEBUG: === SCHEDULING SMART REMINDERS ===")
        println("DEBUG: Habit: $habitTitle (Target: $target)")

        cancelReminder(habitId)

        // Calculate smart reminder times
        val reminderTimes = calculateSmartReminderTimes(target, baseReminderTime)

        reminderTimes.forEachIndexed { index, reminderTime ->
            scheduleIndividualSmartReminder(
                habitId = habitId,
                habitTitle = habitTitle,
                target = target,
                reminderTime = reminderTime,
                reminderIndex = index,
                totalReminders = reminderTimes.size
            )
        }

        println("DEBUG: ✅ Scheduled ${reminderTimes.size} smart reminders")
    }

    private fun calculateSmartReminderTimes(
        target: Int,
        baseReminderTime: LocalTime?
    ): List<LocalTime> {
        val reminderTimes = mutableListOf<LocalTime>()

        // Development mode: Short intervals for testing
        val isDevelopmentMode = true // Set to false for production

        if (isDevelopmentMode) {
            // TEST MODE: Schedule reminders every 2 minutes from now
            val now = LocalTime.now()
            val reminderCount = when {
                target <= 2 -> 2
                target <= 4 -> 3
                target <= 6 -> 4
                else -> 5
            }

            repeat(reminderCount) { index ->
                val testTime = now.plusMinutes((index + 1) * 2L) // 2, 4, 6, 8, 10 minutes
                reminderTimes.add(testTime)
            }

            println("DEBUG: TEST MODE - ${reminderCount} reminders every 2 minutes")
            return reminderTimes
        }

        // PRODUCTION MODE: Realistic intervals
        when {
            target <= 2 -> {
                // Simple habits: 2 reminders
                reminderTimes.add(LocalTime.of(9, 0))   // Morning
                reminderTimes.add(LocalTime.of(19, 0))  // Evening
            }

            target <= 4 -> {
                // Medium habits: 3 reminders
                reminderTimes.add(LocalTime.of(9, 0))   // Morning
                reminderTimes.add(LocalTime.of(14, 0))  // Afternoon
                reminderTimes.add(LocalTime.of(20, 0))  // Evening
            }

            target <= 6 -> {
                // Higher habits: 4 reminders
                reminderTimes.add(LocalTime.of(9, 0))   // Morning
                reminderTimes.add(LocalTime.of(12, 30)) // Midday
                reminderTimes.add(LocalTime.of(16, 0))  // Afternoon
                reminderTimes.add(LocalTime.of(20, 0))  // Evening
            }

            else -> {
                // Very high targets: 5 reminders
                reminderTimes.add(LocalTime.of(8, 0))
                reminderTimes.add(LocalTime.of(11, 30))
                reminderTimes.add(LocalTime.of(15, 0))
                reminderTimes.add(LocalTime.of(18, 0))
                reminderTimes.add(LocalTime.of(21, 0))
            }
        }

        // If user specified a base time, replace the closest reminder
        baseReminderTime?.let { userTime ->
            val userHour = userTime.hour
            val closestIndex = reminderTimes.mapIndexed { index, time ->
                index to kotlin.math.abs(time.hour - userHour)
            }.minByOrNull { it.second }?.first ?: 0

            reminderTimes[closestIndex] = userTime
            println("DEBUG: Replaced closest reminder with user time: $userTime")
        }

        return reminderTimes.sorted()
    }


    private fun scheduleIndividualSmartReminder(
        habitId: Long,
        habitTitle: String,
        target: Int,
        reminderTime: LocalTime,
        reminderIndex: Int,
        totalReminders: Int
    ) {
        val now = LocalDateTime.now()
        var scheduledTime = LocalDateTime.of(now.toLocalDate(), reminderTime)

        if (scheduledTime.isBefore(now) || scheduledTime.isEqual(now)) {
            scheduledTime = scheduledTime.plusDays(1)
        }

        val triggerAtMillis = scheduledTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Create contextual message
        val contextualMessage = when (reminderIndex) {
            0 -> "Start your $habitTitle goal! 🎯 Target: $target"
            totalReminders - 1 -> "Final reminder for $habitTitle! 🏁"
            else -> {
                val progress = (reminderIndex.toFloat() / totalReminders * 100).toInt()
                "Keep going with $habitTitle! 💪 (~$progress% through the day)"
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_id", habitId)
            putExtra("habit_title", habitTitle)
            putExtra("target", target)
            putExtra("reminder_index", reminderIndex)
            putExtra("contextual_message", contextualMessage)
            putExtra("reminder_type", "smart")
        }

        // Use unique request code for each reminder
        val requestCode = (habitId.toInt() * 100) + reminderIndex

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            println("DEBUG: ✅ Smart reminder #${reminderIndex + 1} scheduled for $scheduledTime")
            println("DEBUG: Message: $contextualMessage")
        } catch (e: Exception) {
            println("DEBUG: ❌ Failed to schedule smart reminder: ${e.message}")
        }
    }


    fun cancelReminder(habitId: Long) {
        println("DEBUG: Canceling all reminders for habit ID: $habitId")

        // Cancel main reminder
        val mainIntent = Intent(context, ReminderReceiver::class.java)
        val mainPendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(mainPendingIntent)

        // Cancel smart reminders (up to 10 possible reminders)
        repeat(10) { index ->
            val smartIntent = Intent(context, ReminderReceiver::class.java)
            val requestCode = (habitId.toInt() * 100) + index
            val smartPendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                smartIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(smartPendingIntent)
        }

        println("DEBUG: All reminders canceled for habit $habitId")
    }

    // Keep your test method for development
    fun testReminder(habitId: Long, habitTitle: String) {
        println("DEBUG: Scheduling test reminder in 10 seconds")

        val testTime = LocalDateTime.now().plusSeconds(10)
        val triggerAtMillis = testTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_id", habitId)
            putExtra("habit_title", habitTitle)
            putExtra("reminder_type", "test")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        println("DEBUG: Test alarm scheduled for $testTime")
    }

}

