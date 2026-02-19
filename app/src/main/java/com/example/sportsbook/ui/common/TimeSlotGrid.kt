package com.example.sportsbook.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.ui.theme.SportsBookTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeSlotGrid(
    slots: List<TimeSlot>,
    selectedSlotId: Long?,
    onSlotClick: (TimeSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        slots.forEach { slot ->
            val isSelected = slot.id == selectedSlotId
            FilterChip(
                selected = isSelected,
                onClick = { if (slot.isAvailable) onSlotClick(slot) },
                label = { Text(slot.displayTime) },
                enabled = slot.isAvailable,
                border = if (!slot.isAvailable) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Preview
@Composable
private fun TimeSlotGridPreview() {
    SportsBookTheme {
        TimeSlotGrid(
            slots = listOf(
                TimeSlot(id = 1, startTime = "09:00", endTime = "10:00", isAvailable = true),
                TimeSlot(id = 2, startTime = "10:00", endTime = "11:00", isAvailable = false),
                TimeSlot(id = 3, startTime = "11:00", endTime = "12:00", isAvailable = true)
            ),
            selectedSlotId = 1L,
            onSlotClick = {}
        )
    }
}
