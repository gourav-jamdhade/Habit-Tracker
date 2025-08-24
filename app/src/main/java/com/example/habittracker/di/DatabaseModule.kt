package com.example.habittracker.di

import android.content.Context
import androidx.room.Room
import com.example.habittracker.data.HabitDatabase
import com.example.habittracker.data.analytics.AnalyticsCalculator
import com.example.habittracker.data.dao.EntryDao
import com.example.habittracker.data.dao.HabitDao
import com.example.habittracker.data.repository.AnalyticsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHabitDatabase(@ApplicationContext context: Context): HabitDatabase {

        return Room.databaseBuilder(
            context.applicationContext,
            HabitDatabase::class.java,
            "habit_database"
        ).build()
    }

    @Provides
    fun provideHabitDao(database: HabitDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    fun provideEntryDao(database: HabitDatabase): EntryDao {
        return database.entryDao()
    }

    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        habitDao: HabitDao,
        entryDao: EntryDao,
        analyticsCalculator: AnalyticsCalculator
    ): AnalyticsRepository {
        return AnalyticsRepository(habitDao, entryDao, analyticsCalculator)
    }

    @Provides
    @Singleton
    fun provideAnalyticsCalculator(): AnalyticsCalculator {
        return AnalyticsCalculator()
    }
}
