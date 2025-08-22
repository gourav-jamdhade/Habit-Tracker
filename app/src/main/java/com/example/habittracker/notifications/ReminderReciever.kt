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

        if (habitId != -1L) {
            println("DEBUG: Received reminder for habit: $habitTitle (ID: $habitId)")
            notificationHelper.showHabitReminder(habitId, habitTitle)
        } else {
            println("DEBUG: Invalid habit ID in reminder")
        }
    }
}
