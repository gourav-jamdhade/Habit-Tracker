package com.example.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp



// app/src/main/java/com/yourapp/habittracker/ui/components/ColorPicker.kt
@Composable
fun ColorPicker(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Use regular Column and Rows instead of LazyGrid
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Split colors into rows of 6
        MaterialColors.colors.chunked(6).forEachIndexed { rowIndex, rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowColors.forEachIndexed { colIndex, color ->
                    val colorIndex = rowIndex * 6 + colIndex
                    ColorItem(
                        color = color,
                        isSelected = selectedColor == colorIndex,
                        onClick = { onColorSelected(colorIndex) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining spaces if last row is incomplete
                repeat(6 - rowColors.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ColorItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f) // Keep it square
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


object MaterialColors {
    val colors = listOf(
        Color(0xFF1976D2), // Blue
        Color(0xFF388E3C), // Green
        Color(0xFFF57C00), // Orange
        Color(0xFFD32F2F), // Red
        Color(0xFF7B1FA2), // Purple
        Color(0xFF00796B), // Teal
        Color(0xFF455A64), // Blue Grey
        Color(0xFFE64A19), // Deep Orange
        Color(0xFF5D4037), // Brown
        Color(0xFF616161), // Grey
        Color(0xFFAD1457), // Pink
        Color(0xFF00695C)  // Dark Teal
    )

    fun getColor(index: Int): Color {
        return if (index in colors.indices) colors[index] else colors[0]
    }
}