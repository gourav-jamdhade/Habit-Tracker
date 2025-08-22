package com.example.habittracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitTrackerApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() {
            println("DEBUG: Creating WorkManager configuration")
            println("DEBUG: HiltWorkerFactory initialized: ${::workerFactory.isInitialized}")

            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build()
        }

    override fun onCreate() {
        super.onCreate()
        println("DEBUG: Application onCreate called")
    }
}