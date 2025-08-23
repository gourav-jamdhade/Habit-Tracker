package com.example.habittracker.ui.screens.editHabit

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
import com.example.habittracker.notifications.AlarmScheduler
import com.example.habittracker.utils.ScheduleUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class EditHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository, private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    var uiState by mutableStateOf(EditHabitUiState())
        private set

    private var originalHabit: Habit? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadHabit(habitId: Long) {
        uiState = uiState.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val habit = habitRepository.getHabitById(habitId)
                if (habit != null) {
                    originalHabit = habit
                    uiState = uiState.copy(
                        isLoading = false,
                        title = habit.title,
                        mode = habit.mode,
                        unitType = habit.unitType,
                        target = habit.target,
                        selectedDays = ScheduleUtils.getScheduledDays(habit.schedule),
                        reminderTime = habit.reminderTime,
                        color = habit.color,
                        isArchived = habit.archived, // ADD THIS
                        isValid = true,
                    )
                } else {
                    uiState = uiState.copy(
                        error = "Habit not found",
                    )
                    println("DEBUG: Habit not found")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    error = "Failed to load habit: ${e.message}",
                )
                println("DEBUG: Failed to load habit: ${e.message}")
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onEvent(event: EditHabitEvent) {
        when (event) {
            is EditHabitEvent.ColorChanged -> {
                uiState = uiState.copy(color = event.color)
            }

            EditHabitEvent.ConfirmDelete -> deleteHabit()
            EditHabitEvent.DismissDeleteDialog -> {
                uiState = uiState.copy()
            }

            is EditHabitEvent.ModeChanged -> {
                uiState = uiState.copy(mode = event.mode)

            }

            is EditHabitEvent.ReminderTimeChanged -> {
                uiState = uiState.copy(reminderTime = event.time)
            }

            is EditHabitEvent.SaveHabit -> saveHabit()
            is EditHabitEvent.ScheduleChanged -> {
                uiState = uiState.copy(
                    selectedDays = event.selectedDays,
                    schedule = ScheduleUtils.createSchedule(event.selectedDays),
                )
            }

            EditHabitEvent.ShowDeleteDialog -> {
                uiState = uiState.copy(showDeleteDialog = true)

            }

            is EditHabitEvent.TargetChanged -> {
                uiState = uiState.copy(target = event.target)

            }

            is EditHabitEvent.TitleChanged -> {
                uiState = uiState.copy(
                    title = event.title,
                    isValid = event.title.isNotBlank(),
                )
            }

            is EditHabitEvent.UnitTypeChanged -> {
                uiState = uiState.copy(
                    unitType = event.unitType,
                    target = if (event.unitType == UnitType.BOOLEAN) null else uiState.target,
                )

            }

            EditHabitEvent.ArchiveHabit -> archiveHabit()
            EditHabitEvent.UnarchiveHabit -> unarchiveHabit()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveHabit() {
        if (!uiState.isValid) return
        val original = originalHabit ?: return

        uiState = uiState.copy(isSaving = true)
        viewModelScope.launch {
            try {
                val updatedHabit = original.copy(
                    title = uiState.title.trim(),
                    mode = uiState.mode,
                    unitType = uiState.unitType,
                    target = uiState.target,
                    schedule = uiState.schedule,
                    reminderTime = uiState.reminderTime,
                    color = uiState.color
                )

                habitRepository.updateHabit(updatedHabit)

                // Update reminder scheduling
                alarmScheduler.cancelReminder(updatedHabit.id)
                updatedHabit.reminderTime?.let { time ->
                    alarmScheduler.scheduleReminder(
                        updatedHabit.id,
                        updatedHabit.title,
                        time
                    )
                }
                uiState = uiState.copy(isSaved = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    error = "Failed to update habit: ${e.message}",
                )
            }
        }
    }

    private fun deleteHabit() {
        val original = originalHabit ?: return

        uiState = uiState.copy(isDeleting = true)

        viewModelScope.launch {
            try {
                // Cancel any scheduled reminders
                alarmScheduler.cancelReminder(original.id)

                // Delete the habit
                habitRepository.deleteHabit(original.id)

                uiState = uiState.copy(isDeleted = true)

            } catch (e: Exception) {
                uiState = uiState.copy(
                    error = "Failed to delete habit: ${e.message}",
                )
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy()
    }

    private fun archiveHabit() {
        val original = originalHabit ?: return

        uiState = uiState.copy(isDeleting = true) // Reuse loading state

        viewModelScope.launch {
            try {
                // Cancel any scheduled reminders
                alarmScheduler.cancelReminder(original.id)

                // Archive the habit
                habitRepository.archiveHabit(original.id)

                uiState = uiState.copy(isDeleting = false, isDeleted = true)

            } catch (e: Exception) {
                uiState = uiState.copy(
                    isDeleting = false,
                    error = "Failed to archive habit: ${e.message}"
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun unarchiveHabit() {
        val original = originalHabit ?: return

        uiState = uiState.copy(isDeleting = true)

        viewModelScope.launch {
            try {
                // Unarchive the habit
                habitRepository.unarchiveHabit(original.id)

                // Reschedule reminder if it exists
                original.reminderTime?.let { time ->
                    alarmScheduler.scheduleReminder(
                        original.id,
                        original.title,
                        time
                    )
                }

                uiState = uiState.copy(isDeleting = false, isSaved = true)

            } catch (e: Exception) {
                uiState = uiState.copy(
                    isDeleting = false,
                    error = "Failed to unarchive habit: ${e.message}"
                )
            }
        }
    }


}

//UI State
data class EditHabitUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val mode: HabitMode = HabitMode.BUILD,
    val unitType: UnitType = UnitType.BOOLEAN,
    val target: Int? = null,
    val selectedDays: Set<DayOfWeek> = DayOfWeek.values().toSet(),
    val schedule: Int = ScheduleUtils.DAILY_SCHEDULE,
    val reminderTime: LocalTime? = null,
    val color: Int = 0,
    val isValid: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val error: String? = null,
    val isArchived: Boolean = false,
    val scheduledReminders: List<String> = emptyList(),
)

// Events
sealed class EditHabitEvent {
    data class TitleChanged(val title: String) : EditHabitEvent()
    data class ModeChanged(val mode: HabitMode) : EditHabitEvent()
    data class UnitTypeChanged(val unitType: UnitType) : EditHabitEvent()
    data class TargetChanged(val target: Int?) : EditHabitEvent()
    data class ScheduleChanged(val selectedDays: Set<DayOfWeek>) : EditHabitEvent()
    data class ReminderTimeChanged(val time: LocalTime?) : EditHabitEvent()
    data class ColorChanged(val color: Int) : EditHabitEvent()
    object SaveHabit : EditHabitEvent()
    object ShowDeleteDialog : EditHabitEvent()
    object DismissDeleteDialog : EditHabitEvent()
    object ConfirmDelete : EditHabitEvent()
    object ArchiveHabit : EditHabitEvent()
    object UnarchiveHabit : EditHabitEvent()
}