package com.example.habittracker.notifications

// app/src/main/java/com/yourapp/habittracker/notifications/AlarmScheduler.kt
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleReminder(habitId: Long, habitTitle: String, reminderTime: LocalTime) {
        println("DEBUG: === SCHEDULING REMINDER ===")
        println("DEBUG: Habit: $habitTitle (ID: $habitId)")
        println("DEBUG: Requested time: $reminderTime")

        // Calculate next occurrence of this time
        val now = LocalDateTime.now()
        println("DEBUG: Current time: $now")

        var scheduledTime = LocalDateTime.of(now.toLocalDate(), reminderTime)
        println("DEBUG: Initial scheduled time: $scheduledTime")

        // If time has passed today, schedule for tomorrow
        if (scheduledTime.isBefore(now) || scheduledTime.isEqual(now)) {
            scheduledTime = scheduledTime.plusDays(1)
            println("DEBUG: Time has passed, scheduling for tomorrow: $scheduledTime")
        }

        val triggerAtMillis =
            scheduledTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val currentMillis = System.currentTimeMillis()
        val delayMillis = triggerAtMillis - currentMillis

        println("DEBUG: Current millis: $currentMillis")
        println("DEBUG: Trigger millis: $triggerAtMillis")
        println("DEBUG: Delay: ${delayMillis}ms (${delayMillis / 1000}s)")

        if (delayMillis <= 0) {
            println("DEBUG: ERROR - Scheduling time is in the past!")
            return
        }
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_id", habitId)
            putExtra("habit_title", habitTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(), // Unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            println("DEBUG: ✅ Alarm scheduled successfully!")
            println("DEBUG: Will trigger at: $scheduledTime")
        } catch (e: Exception) {
            println("DEBUG: Failed to schedule alarm: ${e.message}")
        }
    }

    fun cancelReminder(habitId: Long) {
        println("DEBUG: Canceling reminder for habit ID: $habitId")

        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        println("DEBUG: Reminder canceled")
    }

    fun testReminder(habitId: Long, habitTitle: String) {
        println("DEBUG: Scheduling test reminder in 10 seconds")

        val testTime = LocalDateTime.now().plusSeconds(10)
        // Fix: Add .toInstant()
        val triggerAtMillis = testTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_id", habitId)
            putExtra("habit_title", habitTitle)
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
