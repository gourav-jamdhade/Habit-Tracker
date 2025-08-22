package com.example.habittracker.ui.screens.addHabit

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.habittracker.data.entities.HabitMode
import com.example.habittracker.data.entities.UnitType
import com.example.habittracker.ui.components.ColorPicker
import com.example.habittracker.ui.components.DaySelector
import com.example.habittracker.ui.components.TimePickerDialog
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddHabitScreen(

    onNavigateBack: () -> Unit, viewModel: AddHabitViewModel = hiltViewModel()
) {

    val uiSate = viewModel.uiState

    LaunchedEffect(uiSate.isSaved) {
        if (uiSate.isSaved) {
            println("Habit saved, navigating back") // Debug log
            viewModel.resetSaveState() // Reset state first
            onNavigateBack()
        }
    }

    uiSate.error?.let { error ->
        LaunchedEffect(error) {
            println("Error occurred: $error") // Debug log
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Add Habit") }, navigationIcon = {
                IconButton(
                    onClick = onNavigateBack
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }, actions = {
                IconButton(
                    onClick = {
                        viewModel.onEvent(AddHabitEvent.SaveHabit)
                    }, enabled = uiSate.isValid && !uiSate.isSaving
                ) {
                    if (uiSate.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp), strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, "Save")
                    }
                }
            })
        }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //Title Input
            item {
                TitleSection(
                    title = uiSate.title, onTitleChange = {
                        viewModel.onEvent(AddHabitEvent.TitleChanged(it))
                    })
            }


            //Mode selection
            item {
                ModeSection(
                    selectedMode = uiSate.mode, onModeChange = {
                        viewModel.onEvent(AddHabitEvent.ModeChanged(it))
                    })
            }


            //Unit type selection
            item {
                UnitTypeSection(
                    unitType = uiSate.unitType, onUnitTypeChange = {
                        viewModel.onEvent(AddHabitEvent.UnitTypeChanged(it))
                    })
            }


            // Target Input (only for count habits)
            item {
                if (uiSate.unitType == UnitType.COUNT) {
                    TargetSection(
                        target = uiSate.target, onTargetChange = {
                            viewModel.onEvent(AddHabitEvent.TargetChanged(it))
                        })
                }
            }


            // Schedule Selection
            item {
                ScheduleSection(
                    selectedDays = uiSate.selectedDays, onScheduleChange = {
                        viewModel.onEvent(AddHabitEvent.ScheduleChanged(it))
                    })
            }


            //Reminder time
            item {
                ReminderSection(
                    reminderTime = uiSate.reminderTime, onReminderTimeChange = {
                        viewModel.onEvent(AddHabitEvent.ReminderTimeChanged(it))
                    })
            }


            //color picker
            item {
                ColorSection(
                    selectedColor = uiSate.color, onColorChange = {
                        viewModel.onEvent(AddHabitEvent.ColorChanged(it))
                    })
            }
        }

    }
}

@Composable
fun TitleSection(
    title: String, onTitleChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Habit Title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text("e.g., Read, Exercise, Drink Water") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}


@Composable
fun ModeSection(
    selectedMode: HabitMode, onModeChange: (HabitMode) -> Unit
) {
    Column {
        Text(
            text = "Habit Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = selectedMode == HabitMode.BUILD,
                onClick = { onModeChange(HabitMode.BUILD) },
                label = { Text("Build") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedMode == HabitMode.QUIT,
                onClick = { onModeChange(HabitMode.QUIT) },
                label = { Text("Quit") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun UnitTypeSection(
    unitType: UnitType, onUnitTypeChange: (UnitType) -> Unit
) {
    Column {
        Text(
            text = "Tracking Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = unitType == UnitType.BOOLEAN,
                        onClick = { onUnitTypeChange(UnitType.BOOLEAN) })
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = unitType == UnitType.BOOLEAN,
                    onClick = { onUnitTypeChange(UnitType.BOOLEAN) })
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Yes/No", fontWeight = FontWeight.Medium)
                    Text(
                        "Track completion (did it or didn't)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = unitType == UnitType.COUNT,
                        onClick = { onUnitTypeChange(UnitType.COUNT) })
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = unitType == UnitType.COUNT,
                    onClick = { onUnitTypeChange(UnitType.COUNT) })
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Count", fontWeight = FontWeight.Medium)
                    Text(
                        "Track number (glasses, reps, pages)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
 fun TargetSection(
    target: Int?, onTargetChange: (Int?) -> Unit
) {
    Column {
        Text(
            text = "Daily Target (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = target?.toString() ?: "",
            onValueChange = {
                val value = it.toIntOrNull()
                onTargetChange(if (value != null && value > 0) value else null)
            },
            placeholder = { Text("e.g., 8 glasses, 20 push-ups") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
 fun ScheduleSection(
    selectedDays: Set<DayOfWeek>, onScheduleChange: (Set<DayOfWeek>) -> Unit
) {
    Column {
        Text(
            text = "Schedule",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        DaySelector(
            selectedDays = selectedDays, onDaysSelected = onScheduleChange
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
 fun ReminderSection(
    reminderTime: LocalTime?, onReminderTimeChange: (LocalTime?) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Reminder",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (reminderTime != null) {
                    "Remind at ${reminderTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                } else {
                    "No reminder"
                }
            )

            Row {
                if (reminderTime != null) {
                    TextButton(onClick = { onReminderTimeChange(null) }) {
                        Text("Remove")
                    }
                }
                TextButton(onClick = { showTimePicker = true }) {
                    Text(if (reminderTime != null) "Change" else "Add")
                }
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { time ->
                onReminderTimeChange(time)
                showTimePicker = false
            }, onDismiss = { showTimePicker = false },
            initialTime = reminderTime ?: LocalTime.now()
        )

    }
}

@Composable
 fun ColorSection(
    selectedColor: Int, onColorChange: (Int) -> Unit
) {
    Column {
        Text(
            text = "Color",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        ColorPicker(
            selectedColor = selectedColor, onColorSelected = onColorChange
        )
    }
}

