package com.example.habittracker.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek

// app/src/main/java/com/yourapp/habittracker/ui/components/DaySelector.kt
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DaySelector(
    selectedDays: Set<DayOfWeek>,
    onDaysSelected: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Quick presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedDays.size == 7,
                onClick = { onDaysSelected(DayOfWeek.values().toSet()) },
                label = { Text("Daily") },
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = selectedDays == setOf(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
                ),
                onClick = {
                    onDaysSelected(setOf(
                        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
                    ))
                },
                label = { Text("Weekdays") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Individual day toggles - Use FlowRow for automatic wrapping
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp)

        ) {
            DayOfWeek.values().forEach { day ->
                val isSelected = selectedDays.contains(day)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newDays = if (isSelected) {
                            selectedDays - day
                        } else {
                            selectedDays + day
                        }
                        onDaysSelected(newDays)
                    },
                    label = {
                        Text(
                            day.name.take(3), // Mon, Tue, etc.
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
            }
        }
    }
}
