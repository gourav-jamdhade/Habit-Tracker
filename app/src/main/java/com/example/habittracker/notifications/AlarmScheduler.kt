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

    suspend fun scheduleReminder(habitId: Long, habitTitle: String, reminderTime: LocalTime) {
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

    }


}

