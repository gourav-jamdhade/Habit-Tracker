package com.example.habittracker.data.entities

import androidx.room.Entity
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "habit_entries",
    primaryKeys = ["habitId", "date"]
)
data class HabitEntry(
    val habitId: Long,
    val date: LocalDate,
    val completed: Boolean = false,
    val value: Int = 0, // Current count
    val target: Int = 1, // Target for this habit
    val hasCelebrated: Boolean = false, // Add this field
    val completedAt: LocalDateTime? = null
)