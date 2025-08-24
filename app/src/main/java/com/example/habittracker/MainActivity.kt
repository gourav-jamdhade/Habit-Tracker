package com.example.habittracker

// Add these imports to MainActivity.kt
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.habittracker.notifications.AlarmScheduler
import com.example.habittracker.notifications.NotificationHelper
import com.example.habittracker.ui.navigation.HabitNavigation
import com.example.habittracker.ui.theme.HabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

// Add to MainActivity.kt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle permission result if needed
    }

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: AlarmScheduler


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Debug: Check if notification permission is granted
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Always granted on older versions
        }

        println("DEBUG: Notification permission granted: $hasNotificationPermission")

        if (!hasNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // enableEdgeToEdge()

        setContent {
            HabitTrackerTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Column {
                        // Test buttons
//                        Row(modifier = Modifier.padding(16.dp)) {
//                            Button(onClick = { notificationHelper.testNotification() }) {
//                                Text("Test Notification")
//                            }
//                            Button(onClick = {
//                                alarmScheduler.testReminder(1L, "Test Habit")
//                            }) {
//                                Text("Test 10s Timer")
//                            }
//
//                            // Add this button alongside your existing test buttons
//                            Button(
//                                onClick = {
//                                    println("DEBUG: Testing smart reminders...")
//                                    lifecycleScope.launch {
//                                        alarmScheduler.scheduleSmartReminders(
//                                            habitId = 999L,
//                                            habitTitle = "Test Smart Habit",
//                                            target = 6,
//                                            baseReminderTime = LocalTime.now().plusMinutes(1)
//                                        )
//                                    }
//                                }
//                            ) {
//                                Text("Test Smart Reminders")
//                            }
//                        }

                        val navController = rememberNavController()

                        HabitNavigation(navController = navController)
                    }
                }
            }
        }
        checkBatteryOptimization()
        //testNotificationIn10Seconds()

    }

    // Add this test function to MainActivity.kt
//    private fun testNotificationIn10Seconds() {
//        val workData = Data.Builder()
//            .putLong("habit_id", 1L) // Use a known habit ID
//            .build()
//
//        val testWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
//            .setInputData(workData)
//            .setInitialDelay(10, TimeUnit.SECONDS) // Test in 10 seconds
//            .addTag("test_reminder")
//            .build()
//
//        WorkManager.getInstance(this).enqueue(testWorkRequest)
//        println("DEBUG: Test notification scheduled for 10 seconds from now")
//    }


    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(PowerManager::class.java)
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                println("DEBUG: Requesting battery optimization exemption...")

                // Request user to disable battery optimization for this app
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    data = Uri.parse("package:$packageName")
                }

                try {
                    startActivity(intent)
                    println("DEBUG: Battery optimization dialog shown")
                } catch (e: Exception) {
                    println("DEBUG: Failed to show battery optimization dialog: ${e.message}")

                    // Fallback: Open battery settings manually
                    val settingsIntent =
                        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(settingsIntent)
                }
            } else {
                println("DEBUG: App is already exempt from battery optimization")
            }
        }
    }


}


//@AndroidEntryPoint
//class MainActivity : ComponentActivity() {
//
//    private fun createTestNotification() {
//        println("DEBUG: Creating test notification...")
//
//        // Create notification channel (required for Android 8.0+)
//        createNotificationChannel()
//
//        // Create the notification
//        val intent = Intent(this, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notification = NotificationCompat.Builder(this, "test_channel")
//            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon for now
//            .setContentTitle("Test Notification")
//            .setContentText("This is a test notification from your habit tracker!")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, etc.
//            .build()
//
//        try {
//            NotificationManagerCompat.from(this).notify(999, notification)
//            println("DEBUG: Test notification sent successfully")
//        } catch (e: SecurityException) {
//            println("DEBUG: Notification permission denied: ${e.message}")
//        } catch (e: Exception) {
//            println("DEBUG: Notification error: ${e.message}")
//        }
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "test_channel",
//                "Test Notifications",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Test notifications for debugging"
//                enableVibration(true)
//                setShowBadge(true)
//            }
//
//            val notificationManager = getSystemService(NotificationManager::class.java)
//            notificationManager.createNotificationChannel(channel)
//            println("DEBUG: Notification channel created")
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        setContent {
//            HabitTrackerTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    val navController = rememberNavController()
//
//                    // Add test button to your main UI temporarily
//                    Column {
//                        Button(
//                            onClick = { createTestNotification() },
//                            modifier = Modifier.padding(16.dp)
//                        ) {
//                            Text("Send Test Notification")
//                        }
//
//                        HabitNavigation(navController = navController)
//                    }
//                }
//            }
//        }
//
//        // Request notification permission
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//        }
//
//        // Auto-send test notification after 3 seconds for immediate testing
//        Handler(Looper.getMainLooper()).postDelayed({
//            createTestNotification()
//        }, 3000)
//    }
//
//    // Make sure this is still in your MainActivity
//    private val notificationPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        println("DEBUG: Notification permission granted: $isGranted")
//        if (isGranted) {
//            createTestNotification() // Send test notification when permission granted
//        }
//    }
//}
