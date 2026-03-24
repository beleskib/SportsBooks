package com.example.sportsbook.ui.screens.player.booking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.common.EmptyStateView
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.TimeSlotGrid
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingCalendarScreen(
    venueId: Long?,
    coachId: Long?,
    onSlotSelected: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val nextSevenDays = (0..6).map { today.plusDays(it.toLong()) }
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE")
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Time Slot") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select Date",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                nextSevenDays.forEach { date ->
                    val dateString = date.toString()
                    val isSelected = uiState.selectedDate == dateString
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onDateChange(dateString) },
                        label = {
                            Column {
                                Text(
                                    text = date.format(dayOfWeekFormatter),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = date.format(dateFormatter),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    LoadingIndicator(modifier = Modifier.fillMaxWidth())
                }
                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error!!,
                        onRetry = viewModel::loadSlots,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                uiState.availableSlots.isEmpty() && uiState.selectedDate.isNotEmpty() -> {
                    EmptyStateView(
                        title = "No slots available",
                        subtitle = "Try another date",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                uiState.availableSlots.isNotEmpty() -> {
                    Text(
                        text = "Available Slots",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TimeSlotGrid(
                        slots = uiState.availableSlots,
                        selectedSlotId = uiState.selectedSlotId,
                        onSlotClick = { slot -> viewModel.onSlotSelected(slot.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (uiState.selectedSlot != null) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onSlotSelected(uiState.selectedSlotId!!) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun BookingCalendarScreenPreview() {
    val today = LocalDate.now()
    val nextSevenDays = (0..6).map { today.plusDays(it.toLong()) }
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE")
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Select Time Slot") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Select Date", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    nextSevenDays.forEach { date ->
                        FilterChip(
                            selected = date == today,
                            onClick = {},
                            label = {
                                Column {
                                    Text(
                                        text = date.format(dayOfWeekFormatter),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = date.format(dateFormatter),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Available Slots", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
