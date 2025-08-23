package com.example.habittracker.notifications

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        println("DEBUG: ReminderReceiver triggered")

        val habitId = intent.getLongExtra("habit_id", -1L)
        val habitTitle = intent.getStringExtra("habit_title") ?: "Your Habit"
        val reminderType = intent.getStringExtra("reminder_type") ?: "main"
        val contextualMessage = intent.getStringExtra("contextual_message")
        val target = intent.getIntExtra("target", 1)
        val reminderIndex = intent.getIntExtra("reminder_index", 0)

        if (habitId != -1L) {
            println("DEBUG: Received $reminderType reminder for habit: $habitTitle (ID: $habitId)")

            when (reminderType) {
                "smart" -> {
                    // Use contextual message for smart reminders
                    val message = contextualMessage ?: "Time for $habitTitle!"
                    notificationHelper.showSmartHabitReminder(habitId, habitTitle, message)
                }

                "test" -> {
                    notificationHelper.showHabitReminder(habitId, "Test: $habitTitle")
                }

                else -> {
                    // Default main reminder
                    notificationHelper.showHabitReminder(habitId, habitTitle)
                }
            }
        } else {
            println("DEBUG: Invalid habit ID in reminder")
        }
    }
}

