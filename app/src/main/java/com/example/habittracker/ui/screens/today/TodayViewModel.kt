package com.example.habittracker.ui.screens.today

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.entities.Entry
import com.example.habittracker.data.entities.UnitType
import com.example.habittracker.data.repository.HabitRepository
import com.example.habittracker.data.repository.HabitWithEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadTodayHabits()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadTodayHabits() {
        viewModelScope.launch {
            repository.getHabitsForToday()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load habits: ${error.message}"
                    )
                }
                .collect { habitsWithEntries ->
                    val habitsWithProgress = habitsWithEntries.map { habitWithEntry ->
                        val streak = repository.getStreakForHabit(habitWithEntry.habit.id)
                        HabitWithProgress(habitWithEntry, streak)
                    }

                    _uiState.value = _uiState.value.copy(
                        habits = habitsWithProgress,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun onEvent(event: TodayEvent) {
        when (event) {
            TodayEvent.Refresh -> {
                _uiState.value = _uiState.value.copy(isLoading = true)
                loadTodayHabits()
            }

            is TodayEvent.ToggleBooleanHabit -> {
                toggleBooleanHabit(event.habitId, event.currentValue)

            }

            is TodayEvent.UpdateCountHabit -> {
                updateCountHabit(event.habitId, event.value)
            }
        }
    }

    private fun toggleBooleanHabit(habitId: Long, currentValue: Boolean) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val existingEntry = repository.getEntry(habitId, today)
                if (existingEntry != null) {
                    val updatedEntry = existingEntry.copy(valueBool = !currentValue)
                    repository.insertOrUpdateEntry(updatedEntry)
                } else {
                    val newEntry = Entry(
                        habitId = habitId,
                        localDate = today,
                        valueBool = true
                    )
                    repository.insertOrUpdateEntry(entry = newEntry)


                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update habit: ${e.message}"
                )

            }
        }
    }

    private fun updateCountHabit(habitId: Long, value: Int) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val existingEntry = repository.getEntry(habitId, today)
                if (existingEntry != null) {
                    val updatedEntry = existingEntry.copy(valueCount = value)
                    repository.insertOrUpdateEntry(updatedEntry)
                } else {
                    val newEntry = Entry(
                        habitId = habitId,
                        localDate = today,
                        valueCount = value
                    )
                    repository.insertOrUpdateEntry(entry = newEntry)

                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update habit: ${e.message}"
                )

            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class TodayUiState(
    val habits: List<HabitWithProgress> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class HabitWithProgress(
    val habitWithEntry: HabitWithEntry,
    val streak: Int
) {
    val habit = habitWithEntry.habit
    val entry = habitWithEntry.entry
    val isCompleted = habitWithEntry.isCompleted

    val currentValue: Any?
        get() = when (habit.unitType) {
            UnitType.BOOLEAN -> entry?.valueBool ?: false
            UnitType.COUNT -> entry?.valueCount ?: 0
        }
}

sealed class TodayEvent {
    data class ToggleBooleanHabit(val habitId: Long, val currentValue: Boolean) : TodayEvent()
    data class UpdateCountHabit(val habitId: Long, val value: Int) : TodayEvent()
    object Refresh : TodayEvent()
}