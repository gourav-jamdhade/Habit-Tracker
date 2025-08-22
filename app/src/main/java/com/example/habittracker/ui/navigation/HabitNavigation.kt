package com.example.habittracker.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.habittracker.ui.screens.addHabit.AddHabitScreen
import com.example.habittracker.ui.screens.archived.ArchivedHabitsScreen
import com.example.habittracker.ui.screens.editHabit.EditHabitScreen
import com.example.habittracker.ui.screens.today.TodayScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "today"
    ) {
        composable("today") {
            TodayScreen(
                onNavigateToHabit = {
                    navController.navigate("add_habit")
                },
                onNavigateToEditHabit = { habitId ->
                    navController.navigate("edit_habit/$habitId")
                },
                onNavigateToArchived = { navController.navigate("archived_habits") }

            )
        }

        composable("add_habit") {
            AddHabitScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("edit_habit/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")?.toLongOrNull() ?: 0L
            EditHabitScreen(
                habitId = habitId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onHabitDeleted = {
                    navController.popBackStack("today", inclusive = false)
                }
            )
        }

        composable("archived_habits") {
            ArchivedHabitsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}