package com.example.habittracker.ui.screens.addHabit

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.entities.Habit
import com.example.habittracker.data.entities.HabitMode
import com.example.habittracker.data.entities.UnitType
import com.example.habittracker.data.repository.HabitRepository
import com.example.habittracker.utils.ScheduleUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    var uiState by mutableStateOf(AddHabitUiSate())
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun onEvent(event: AddHabitEvent) {
        when (event) {
            is AddHabitEvent.TitleChanged -> {
                uiState = uiState.copy(
                    title = event.title,
                    isValid = event.title.isNotBlank()
                )
            }

            is AddHabitEvent.ModeChanged -> {
                uiState = uiState.copy(mode = event.mode)
            }

            is AddHabitEvent.UnitTypeChanged -> {
                uiState = uiState.copy(
                    unitType = event.unitType,
                    target = if (event.unitType == UnitType.BOOLEAN) null else uiState.target
                )
            }

            is AddHabitEvent.TargetChanged -> {
                uiState = uiState.copy(target = event.target)

            }

            is AddHabitEvent.ColorChanged -> {
                uiState = uiState.copy(color = event.color)
            }

            is AddHabitEvent.ReminderTimeChanged -> {
                uiState = uiState.copy(reminderTime = event.time)
            }

            AddHabitEvent.SaveHabit -> {
                saveHabit()
            }

            is AddHabitEvent.ScheduleChanged -> {
                val schedule = ScheduleUtils.createSchedule(event.selectedDays)
                uiState = uiState.copy(
                    selectedDays = event.selectedDays,
                    schedule = schedule
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveHabit() {
        if (!uiState.isValid) return

        uiState = uiState.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {

                val habit = Habit(
                    title = uiState.title,
                    mode = uiState.mode,
                    unitType = uiState.unitType,
                    target = uiState.target,
                    schedule = uiState.schedule,
                    reminderTime = uiState.reminderTime,
                    color = uiState.color,
                    createdAt = LocalDate.now()
                )

                val habitId = habitRepository.insertHabit(habit)
                println("Habit saved successfully with ID: $habitId") // Debug log

                uiState = uiState.copy(isSaving = false, isSaved = true)
            } catch (e: Exception) {
                println("Save habit error: ${e.message}") // Debug log
                e.printStackTrace()
                uiState = uiState.copy(
                    isSaving = false,
                    error = "Failed to save habit:${e.message}"
                )
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }

    // Add a function to reset the state when navigating away
    fun resetSaveState() {
        uiState = uiState.copy(isSaving = false, isSaved = false, error = null)
    }
}

data class AddHabitUiSate(
    val title: String = "",
    val mode: HabitMode = HabitMode.BUILD,
    val unitType: UnitType = UnitType.BOOLEAN,
    val target: Int? = null,
    val selectedDays: Set<DayOfWeek> = DayOfWeek.values().toSet(), //Default:Daily
    val schedule: Int = ScheduleUtils.DAILY_SCHEDULE,
    val reminderTime: LocalTime? = null,
    val color: Int = 0,
    val isValid: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

sealed class AddHabitEvent {
    data class TitleChanged(val title: String) : AddHabitEvent()
    data class ModeChanged(val mode: HabitMode) : AddHabitEvent()
    data class UnitTypeChanged(val unitType: UnitType) : AddHabitEvent()
    data class TargetChanged(val target: Int?) : AddHabitEvent()
    data class ScheduleChanged(val selectedDays: Set<DayOfWeek>) : AddHabitEvent()
    data class ReminderTimeChanged(val time: LocalTime?) : AddHabitEvent()
    data class ColorChanged(val color: Int) : AddHabitEvent()
    object SaveHabit : AddHabitEvent()

}