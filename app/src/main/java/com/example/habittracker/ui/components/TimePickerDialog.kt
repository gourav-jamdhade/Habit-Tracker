package com.example.habittracker.ui.components


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimePickerDialog(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    initialTime: LocalTime = LocalTime.now()
) {
    val now = LocalTime.now()
    val currentHour = now.hour
    val currentMinute = now.minute

    // Initialize with next available time
    val nextAvailableTime = now.plusMinutes(1)

    var selectedHour by remember { mutableIntStateOf(nextAvailableTime.hour) }
    var selectedMinute by remember { mutableIntStateOf(nextAvailableTime.minute) }

    // Validate and update selected time when it changes
    LaunchedEffect(selectedHour, selectedMinute) {
        val selectedTime = LocalTime.of(selectedHour, selectedMinute)
        if (selectedTime.isBefore(now) || selectedTime == now) {
            // If selected time is not valid, move to next available
            selectedHour = nextAvailableTime.hour
            selectedMinute = nextAvailableTime.minute
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Reminder Time",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Show current time and indication
                Text(
                    text = "Current: ${String.format("%02d:%02d", currentHour, currentMinute)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Live time display
                Text(
                    text = String.format("%02d:%02d", selectedHour, selectedMinute),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Time sliders with proper restrictions
                Row(
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour slider
                    TimeSlider(
                        label = "Hours",
                        values = getValidHours(currentHour, currentMinute),
                        selectedValue = selectedHour,
                        onValueSelected = { newHour ->
                            selectedHour = newHour
                            // Update minute if needed
                            val newMinutes = getValidMinutes(newHour, currentHour, currentMinute)
                            if (selectedMinute !in newMinutes) {
                                selectedMinute = newMinutes.firstOrNull() ?: selectedMinute
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Minute slider - dynamically filtered based on selected hour
                    TimeSlider(
                        label = "Minutes",
                        values = getValidMinutes(selectedHour, currentHour, currentMinute),
                        selectedValue = selectedMinute,
                        onValueSelected = { selectedMinute = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val selectedTime = LocalTime.of(selectedHour, selectedMinute)
                            onTimeSelected(selectedTime)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

// Helper function to get valid hours (current hour onwards)
private fun getValidHours(currentHour: Int, currentMinute: Int): List<Int> {
    val validHours = mutableListOf<Int>()

    // Add current hour only if there are valid minutes left
    if (currentMinute < 59) {
        validHours.add(currentHour)
    }

    // Add all subsequent hours
    for (hour in (currentHour + 1)..23) {
        validHours.add(hour)
    }

    // Add hours for next day (0 to currentHour-1) if needed
    if (validHours.isEmpty() || currentHour >= 23) {
        for (hour in 0 until 24) {
            if (hour != currentHour || validHours.isEmpty()) {
                validHours.add(hour)
            }
        }
    }

    return validHours
}

// Helper function to get valid minutes based on selected hour
private fun getValidMinutes(selectedHour: Int, currentHour: Int, currentMinute: Int): List<Int> {
    return when {
        selectedHour > currentHour -> {
            // Future hour - all minutes available
            (0..59).toList()
        }
        selectedHour == currentHour -> {
            // Current hour - only future minutes
            ((currentMinute + 1)..59).toList()
        }
        else -> {
            // Past hour (next day) - all minutes available
            (0..59).toList()
        }
    }
}

@Composable
private fun TimeSlider(
    label: String,
    values: List<Int>,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Find current selection index
    val currentIndex = values.indexOf(selectedValue).takeIf { it != -1 } ?: 0

    // Auto-scroll to selected item when first shown or when value changes externally
    LaunchedEffect(selectedValue) {
        listState.animateScrollToItem(currentIndex)
    }

    // Listen to scroll changes to update selected value
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex +
                    if (listState.firstVisibleItemScrollOffset > 30) 1 else 0
        }
            .collect { newIndex ->
                val clampedIndex = newIndex.coerceIn(values.indices)
                if (values[clampedIndex] != selectedValue) {
                    onValueSelected(values[clampedIndex])
                }
            }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.fillMaxHeight()) {
            // Selection indicator background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.Center)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 76.dp) // Center the middle item
            ) {
                itemsIndexed(values) { index, value ->
                    TimeSliderItem(
                        value = value,
                        isSelected = value == selectedValue,
                        onClick = {
                            onValueSelected(value)
                            scope.launch {
                                listState.animateScrollToItem(index)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSliderItem(
    value: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = if (isSelected) 1f else 0.6f
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .width(60.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()
        )
    }
}

// Helper function to get available minutes based on current time
private fun getAvailableMinutes(
    selectedHour: Int,
    currentHour: Int,
    currentMinute: Int
): List<Int> {
    return if (selectedHour == currentHour) {
        // For current hour, only show future minutes
        ((currentMinute + 1)..59).toList()
    } else {
        // For other hours, show all minutes
        (0..59).toList()
    }
}

