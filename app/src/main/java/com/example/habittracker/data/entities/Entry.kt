package com.example.habittracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate


@Entity(
    tableName = "entries",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId"), Index("localDate")]
)
data class Entry(

    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val localDate: LocalDate,
    val valueBool: Boolean? = null, // for boolean habits
    val valueCount: Int? = null, // for count habits
    val note: String? = null // optional, not used in UI yet
)