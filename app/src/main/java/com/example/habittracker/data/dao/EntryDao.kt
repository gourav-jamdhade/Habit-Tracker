package com.example.habittracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.habittracker.data.entities.Entry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: Entry)

    @Query("SELECT * FROM entries WHERE habitId = :habitId AND localDate = :date")
    suspend fun getEntry(habitId: Long, date: LocalDate): Entry?

    @Query("SELECT * FROM entries WHERE habitId = :habitId ORDER BY localDate DESC")
    fun getEntriesForHabit(habitId: Long): Flow<List<Entry>>

    @Query("SELECT * FROM entries WHERE localDate = :date")
    fun getEntriesForDate(date: LocalDate): Flow<List<Entry>>


    @Query(
        """
        SELECT * FROM entries
        WHERE habitId =:habitId
        AND localDate BETWEEN :startDate AND :endDate
        ORDER BY localDate DESC
    """
    )
    suspend fun getEntriesInRange(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Entry>

    @Delete
    suspend fun deleteEntry(entry: Entry)
}