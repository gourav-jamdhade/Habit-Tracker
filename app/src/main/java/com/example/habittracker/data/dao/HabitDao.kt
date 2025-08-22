package com.example.habittracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.habittracker.data.entities.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE archived = 0  ORDER BY createdAt DESC")
    fun getAllActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id  = :id")
    suspend fun getHabitById(id:Long) : Habit?

    @Insert
    suspend fun insertHabit(habit:Habit):Long

    @Update
    suspend fun updateHabit(habit:Habit)

    @Query("UPDATE habits SET archived = 1 WHERE id = :id")
    suspend fun archiveHabit(id:Long)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    @Query("""
        SELECT * FROM habits
        WHERE archived =  0
        AND (schedule & :todayBit)>0
        ORDER BY CreatedAt DESC
        """)
    fun getHabitsForDay(todayBit:Int):Flow<List<Habit>>

    @Query("UPDATE habits SET archived = :archived WHERE id = :id")
    suspend fun setHabitArchived(id: Long, archived: Boolean)

    @Query("SELECT * FROM habits WHERE archived = 1 ORDER BY title ASC")
    fun getArchivedHabits(): Flow<List<Habit>>
}