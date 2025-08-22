package com.example.habittracker.ui.screens.archived

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.entities.Habit
import com.example.habittracker.data.repository.HabitRepository
import com.example.habittracker.notifications.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedHabitsViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchivedHabitsUiState())
    val uiState: StateFlow<ArchivedHabitsUiState> = _uiState.asStateFlow()

    init {
        loadArchivedHabits()
    }

    private fun loadArchivedHabits() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                repository.getArchivedHabits().collect { habits ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        archivedHabits = habits
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load archived habits: ${e.message}"
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun unarchiveHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                repository.unarchiveHabit(habitId)

                // Reschedule reminder if habit has one
                val habit = repository.getHabitById(habitId)
                habit?.reminderTime?.let { time ->
                    alarmScheduler.scheduleReminder(habitId, habit.title, time)
                }

                _uiState.value = _uiState.value.copy(
                    message = "Habit unarchived successfully"
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to unarchive habit: ${e.message}"
                )
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                // Cancel any reminders (just in case)
                alarmScheduler.cancelReminder(habitId)

                // Permanently delete
                repository.deleteHabit(habitId)

                _uiState.value = _uiState.value.copy(
                    message = "Habit deleted permanently"
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete habit: ${e.message}"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class ArchivedHabitsUiState(
    val isLoading: Boolean = false,
    val archivedHabits: List<Habit> = emptyList(),
    val message: String? = null,
    val error: String? = null
)
