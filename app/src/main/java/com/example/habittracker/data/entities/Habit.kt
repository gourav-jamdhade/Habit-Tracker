package com.example.habittracker.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val mode: HabitMode, //build or quit
    val unitType: UnitType, //boolean or Count
    val target: Int? = null,
    val schedule: Int,
    val reminderTime: LocalTime? = null,
    val color: Int = 0,
    val createdAt: LocalDate,
    val archived: Boolean = false
)

enum class HabitMode {
    BUILD, QUIT
}

enum class UnitType {
    BOOLEAN, COUNT
}