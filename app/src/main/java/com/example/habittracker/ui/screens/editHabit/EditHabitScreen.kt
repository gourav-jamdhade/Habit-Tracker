package com.example.habittracker.ui.screens.editHabit

// app/src/main/java/com/yourapp/habittracker/ui/screens/edithabit/EditHabitScreen.kt
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.habittracker.data.entities.UnitType
import com.example.habittracker.ui.screens.addHabit.ColorSection
import com.example.habittracker.ui.screens.addHabit.ModeSection
import com.example.habittracker.ui.screens.addHabit.ReminderSection
import com.example.habittracker.ui.screens.addHabit.ScheduleSection
import com.example.habittracker.ui.screens.addHabit.TargetSection
import com.example.habittracker.ui.screens.addHabit.TitleSection
import com.example.habittracker.ui.screens.addHabit.UnitTypeSection

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    habitId: Long,
    onNavigateBack: () -> Unit,
    onHabitDeleted: () -> Unit,
    viewModel: EditHabitViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    // Load habit data when screen opens
    LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId)
    }

    // Handle save success
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    // Handle delete success
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onHabitDeleted()
        }
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Habit") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Delete button
                        IconButton(
                            onClick = { viewModel.onEvent(EditHabitEvent.ShowDeleteDialog) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }

                        IconButton(
                            onClick = { viewModel.onEvent(EditHabitEvent.ArchiveHabit) }
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive")
                        }

                        // Save button
                        IconButton(
                            onClick = { viewModel.onEvent(EditHabitEvent.SaveHabit) },
                            enabled = uiState.isValid && !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = "Save")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Reuse the same sections from AddHabitScreen
                item {
                    TitleSection(
                        title = uiState.title,
                        onTitleChange = { viewModel.onEvent(EditHabitEvent.TitleChanged(it)) }
                    )
                }

                item {
                    ModeSection(
                        selectedMode = uiState.mode,
                        onModeChange = { viewModel.onEvent(EditHabitEvent.ModeChanged(it)) }
                    )
                }

                item {
                    UnitTypeSection(
                        unitType = uiState.unitType,
                        onUnitTypeChange = { viewModel.onEvent(EditHabitEvent.UnitTypeChanged(it)) },
                    )
                }

                if (uiState.unitType == UnitType.COUNT) {
                    item {
                        TargetSection(
                            target = uiState.target,
                            onTargetChange = { viewModel.onEvent(EditHabitEvent.TargetChanged(it)) }
                        )
                    }
                }

                item {
                    ScheduleSection(
                        selectedDays = uiState.selectedDays,
                        onScheduleChange = { viewModel.onEvent(EditHabitEvent.ScheduleChanged(it)) }
                    )
                }

                item {
                    ReminderSection(
                        reminderTime = uiState.reminderTime,
                        onReminderTimeChange = {
                            viewModel.onEvent(
                                EditHabitEvent.ReminderTimeChanged(
                                    it
                                )
                            )
                        }
                    )
                }

                item {
                    ColorSection(
                        selectedColor = uiState.color,
                        onColorChange = { viewModel.onEvent(EditHabitEvent.ColorChanged(it)) }
                    )
                }
            }
        }

        // Delete confirmation dialog
        if (uiState.showDeleteDialog) {
            DeleteConfirmationDialog(
                habitTitle = uiState.title,
                onConfirm = { viewModel.onEvent(EditHabitEvent.ConfirmDelete) },
                onDismiss = { viewModel.onEvent(EditHabitEvent.DismissDeleteDialog) }
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    habitTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Habit") },
        text = {
            Text("Are you sure you want to delete \"$habitTitle\"? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
