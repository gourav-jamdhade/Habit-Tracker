package com.example.habittracker.ui.screens.today

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.habittracker.data.entities.UnitType
import com.example.habittracker.ui.components.CountInputDialog
import com.example.habittracker.ui.components.MaterialColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TodayScreen(
    onNavigateToHabit: () -> Unit,
    onNavigateToEditHabit: (Long) -> Unit,
    onNavigateToArchived: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val today = remember { LocalDate.now() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Today")
                        Text(
                            text = today.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Add archived habits button
                    IconButton(onClick = onNavigateToArchived) {
                        Icon(
                            Icons.Default.Archive,
                            contentDescription = "Archived Habits"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToHabit
            ) {
                Icon(Icons.Default.Add, "Add habit")
            }
        },

    ) { paddingValues ->

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.habits.isEmpty() -> {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onAddHabit = onNavigateToHabit
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //Summary Card
                    item {
                        SummaryCard(habits = uiState.habits)
                    }

                    //Habit Items
                    items(
                        items = uiState.habits,
                        key = { it.habit.id }
                    ) { habitWithProgress ->
                        HabitCard(
                            habitWithProgress = habitWithProgress,
                            onToggleBoolean = { habitId, currentValue ->
                                viewModel.onEvent(
                                    TodayEvent.ToggleBooleanHabit(
                                        habitId,
                                        currentValue
                                    )
                                )
                            },
                            onUpdateCount = { habitId, value ->
                                viewModel.onEvent(TodayEvent.UpdateCountHabit(habitId, value))

                            },
                            onEditHabit = onNavigateToEditHabit
                        )

                    }
                }

            }

        }
        //Error Handling
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                println("Today Screen Error : $error")
                viewModel.clearError()
            }

        }

    }

}

@Composable
fun EmptyState(
    onAddHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "🎯",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "No habits yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Create your first habit to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onAddHabit,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Habit")
            }
        }
    }
}

@Composable
private fun SummaryCard(
    habits: List<HabitWithProgress>,
    modifier: Modifier = Modifier
) {
    val completedToday = habits.count { it.isCompleted }
    val totalHabits = habits.size
    val completionRate = if (totalHabits > 0) (completedToday.toFloat() / totalHabits) else 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$completedToday of $totalHabits completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(completionRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { completionRate },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }
    }
}

@Composable
private fun HabitCard(
    habitWithProgress: HabitWithProgress,
    onToggleBoolean: (Long, Boolean) -> Unit,
    onUpdateCount: (Long, Int) -> Unit,
    onEditHabit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithProgress.habit
    val isCompleted = habitWithProgress.isCompleted
    val currentValue = habitWithProgress.currentValue

    var showCountDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        MaterialColors.getColor(habit.color),
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Habit info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Streak info
                    Text(
                        text = "🔥 ${habitWithProgress.streak} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Progress info for count habits
                    if (habit.unitType == UnitType.COUNT) {
                        val count = currentValue as? Int ?: 0
                        val targetText = habit.target?.let { " / $it" } ?: ""
                        Text(
                            text = "$count$targetText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }



            // Action button
            when (habit.unitType) {
                UnitType.BOOLEAN -> {
                    IconButton(
                        onClick = {
                            onToggleBoolean(habit.id, currentValue as? Boolean ?: false)
                        }
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                            )
                        }
                    }


                }

                UnitType.COUNT -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick increment button
                        IconButton(
                            onClick = {
                                val newCount = (currentValue as? Int ?: 0) + 1
                                onUpdateCount(habit.id, newCount)
                            }
                        ) {
                            Text(
                                "+1",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Edit count button
                        TextButton(
                            onClick = { showCountDialog = true }
                        ) {
                            Text("Edit")
                        }
                    }
                }
            }

            IconButton(
                onClick = {
                    println("DEBUG: Toady Screen Edit Habit Button Clicked for Habit ID: ${habit.id}")
                    onEditHabit(habit.id) }
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit habit"
                )
            }
        }



        // Count input dialog
        if (showCountDialog) {
            CountInputDialog(
                currentValue = currentValue as? Int ?: 0,
                habitTitle = habit.title,
                onValueChange = { newValue ->
                    onUpdateCount(habit.id, newValue)
                    showCountDialog = false
                },
                onDismiss = { showCountDialog = false }
            )
        }

    }
}


