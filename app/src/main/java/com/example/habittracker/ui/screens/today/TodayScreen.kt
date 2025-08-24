package com.example.habittracker.ui.screens.today

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    onNavigateToAnalytics: () -> Unit,
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
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Analytics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

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
    var showCelebration by remember { mutableStateOf(false) }
    var celebrationTriggered by remember { mutableStateOf(false) }
    var lastCelebratedCount by remember { mutableStateOf(-1) }


    // Track celebration state for this card
    val currentCount = currentValue as? Int ?: 0
    val target = habit.target ?: 1
    val hasReachedTarget = currentCount >= target
    val hasExceededTarget = currentCount > target

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                showCelebration -> MaterialTheme.colorScheme.primaryContainer
                isCompleted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                else ->
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
                        CountProgressDisplay(
                            currentCount = currentCount,
                            target = target,
                            hasExceededTarget = hasExceededTarget
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
                    CountHabitControls(
                        currentCount = currentCount,
                        target = target,
                        onIncrement = {
                            val newCount = currentCount + 1
                            onUpdateCount(habit.id, newCount)

                            // Trigger celebration when reaching target
                            if (newCount == target && currentCount < target && lastCelebratedCount != newCount) {
                                showCelebration = true
                                celebrationTriggered = true
                                lastCelebratedCount = newCount

                                // Hide celebration after 2 seconds
                                kotlin.concurrent.timer(period = 2000, initialDelay = 2000) {
                                    showCelebration = false
                                    cancel()
                                }
                            }
                        },
                        onShowDialog = { showCountDialog = true }
                    )
                }
            }

            IconButton(
                onClick = {
                    println("DEBUG: Toady Screen Edit Habit Button Clicked for Habit ID: ${habit.id}")
                    onEditHabit(habit.id)
                }
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit habit"
                )
            }
        }

        // Celebration animation overlay
        if (showCelebration) {
            CelebrationAnimation(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
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

        // Reset celebration tracking when count goes below target
        LaunchedEffect(currentCount, target) {
            if (currentCount < target) {
                lastCelebratedCount = -1 // Reset so we can celebrate reaching target again
            }
        }

    }
}

@Composable
private fun CountProgressDisplay(
    currentCount: Int,
    target: Int,
    hasExceededTarget: Boolean
) {
    Column {
        Text(
            text = "$currentCount / $target",
            style = MaterialTheme.typography.bodySmall,
            color = when {
                hasExceededTarget -> MaterialTheme.colorScheme.tertiary
                currentCount >= target -> Color(0xFF4CAF50) // Success green
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        if (hasExceededTarget) {
            Text(
                text = "🚨 +${currentCount - target} extra",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        } else if (currentCount >= target) {
            Text(
                text = "✅ Target reached!",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun CountHabitControls(
    currentCount: Int,
    target: Int,
    onIncrement: () -> Unit,
    onShowDialog: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // +1 button (always enabled for soft limit)
        Button(
            onClick = onIncrement,
            colors = ButtonDefaults.buttonColors(
                containerColor = when {
                    currentCount >= target -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                "+1",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Edit count button
        TextButton(
            onClick = onShowDialog,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Edit", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CelebrationAnimation(
    modifier: Modifier = Modifier
) {

    val infiniteTransition = rememberInfiniteTransition()

    // Multiple animation values for dynamic effects
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        )
    )

    val glow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glowing background effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f * glow),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f * glow),
                            Color.Transparent
                        ),
                        radius = 200f
                    ),
                    RoundedCornerShape(12.dp)
                )
        )

        // Multiple celebration emojis with different animations
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouncing trophy
            Text(
                text = "🏆",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .scale(scale)
                    .rotate(rotation * 0.5f)
            )

            // Success text with glow effect
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GOAL REACHED!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = glow),
                    modifier = Modifier.scale(scale * 0.9f)
                )

                Text(
                    text = "🎉 Amazing work! 🎉",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = glow),
                    modifier = Modifier.scale(scale * 0.8f)
                )
            }

            // Spinning star
            Text(
                text = "⭐",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .scale(scale * 1.1f)
                    .rotate(rotation * 2f)
            )
        }

        // Sparkling particles effect
        SparkleEffect(
            modifier = Modifier.fillMaxSize(),
            intensity = glow
        )
    }
}

@Composable
private fun SparkleEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    // Simple sparkle effect using positioned emojis
    Box(modifier = modifier) {
        val sparkles = listOf("✨", "💫", "⭐", "🌟")
        val positions = listOf(
            Alignment.TopStart,
            Alignment.TopEnd,
            Alignment.BottomStart,
            Alignment.BottomEnd
        )

        sparkles.zip(positions).forEach { (sparkle, position) ->
            Box(
                modifier = Modifier
                    .align(position)
                    .padding(16.dp)
                    .alpha(intensity)
                    .scale(0.8f + intensity * 0.4f)
            ) {
                Text(
                    text = sparkle,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

