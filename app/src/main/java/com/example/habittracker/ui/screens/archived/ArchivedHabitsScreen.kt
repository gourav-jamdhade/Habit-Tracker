package com.example.habittracker.ui.screens.archived

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.habittracker.data.entities.Habit
import com.example.habittracker.ui.components.MaterialColors

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedHabitsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ArchivedHabitsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archived Habits") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
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

            uiState.archivedHabits.isEmpty() -> {
                EmptyArchivedState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
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
                    items(
                        items = uiState.archivedHabits,
                        key = { it.id }
                    ) { habit ->
                        ArchivedHabitCard(
                            habit = habit,
                            onUnarchive = { viewModel.unarchiveHabit(it) },
                            onDelete = { viewModel.deleteHabit(it) }
                        )
                    }
                }
            }
        }

        // Show success message
        uiState.message?.let { message ->
            LaunchedEffect(message) {
                // You can show a Snackbar here
                viewModel.clearMessage()
            }
        }
    }
}

@Composable
private fun EmptyArchivedState(
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
                text = "📦",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "No Archived Habits",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Archived habits will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArchivedHabitCard(
    habit: Habit,
    onUnarchive: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator (faded)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        MaterialColors.getColor(habit.color).copy(alpha = 0.5f),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Archived • ${habit.unitType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Actions
            Row {
                // Unarchive button
                IconButton(
                    onClick = { onUnarchive(habit.id) }
                ) {
                    Icon(
                        Icons.Default.Unarchive,
                        contentDescription = "Unarchive",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Delete button
                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete permanently",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Permanently") },
            text = {
                Text("Are you sure you want to permanently delete \"${habit.title}\"? This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(habit.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
