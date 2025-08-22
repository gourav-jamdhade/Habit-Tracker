package com.example.habittracker.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.habittracker.ui.components.MaterialColors

fun Int.toHabitColor(): Color {
    return MaterialColors.getColor(this)
}
