package com.example.sportsbook.ui.screens.player.match

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sportsbook.domain.enums.MatchType
import com.example.sportsbook.domain.enums.MatchVisibility
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.common.toIsoDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMatchScreen(
    onMatchCreated: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateMatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.createdMatch) {
        uiState.createdMatch?.let { onMatchCreated(it.id) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Match") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Match Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Sport Type dropdown
            SportDropdown(
                selected = uiState.sportType,
                onSelected = viewModel::updateSportType
            )

            // Match Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Type:", style = MaterialTheme.typography.bodyMedium)
                MatchType.entries.forEach { type ->
                    androidx.compose.material3.FilterChip(
                        selected = uiState.matchType == type,
                        onClick = { viewModel.updateMatchType(type) },
                        label = { Text(type.displayName) }
                    )
                }
            }

            // Visibility
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Visibility:", style = MaterialTheme.typography.bodyMedium)
                MatchVisibility.entries.forEach { vis ->
                    androidx.compose.material3.FilterChip(
                        selected = uiState.visibility == vis,
                        onClick = { viewModel.updateVisibility(vis) },
                        label = { Text(vis.displayName) }
                    )
                }
            }

            // Date picker
            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState = rememberDatePickerState()

            OutlinedTextField(
                value = if (uiState.matchDate.isNotBlank()) uiState.matchDate.toDisplayDate() else "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Date *") },
                placeholder = { Text("DD-MM-YYYY") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date")
                    }
                },
                enabled = false,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                viewModel.updateMatchDate(millis.toIsoDate())
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // Time pickers
            var showStartTimePicker by remember { mutableStateOf(false) }
            var showEndTimePicker by remember { mutableStateOf(false) }
            val startTimeState = rememberTimePickerState(initialHour = 18, initialMinute = 0)
            val endTimeState = rememberTimePickerState(initialHour = 19, initialMinute = 0)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.startTime,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start *") },
                    placeholder = { Text("HH:MM") },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showStartTimePicker = true },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showStartTimePicker = true }) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Pick start time", modifier = Modifier.size(20.dp))
                        }
                    },
                    enabled = false,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                OutlinedTextField(
                    value = uiState.endTime,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End *") },
                    placeholder = { Text("HH:MM") },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndTimePicker = true },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showEndTimePicker = true }) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Pick end time", modifier = Modifier.size(20.dp))
                        }
                    },
                    enabled = false,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (showStartTimePicker) {
                TimePickerDialog(
                    onDismiss = { showStartTimePicker = false },
                    onConfirm = {
                        val h = startTimeState.hour.toString().padStart(2, '0')
                        val m = startTimeState.minute.toString().padStart(2, '0')
                        viewModel.updateStartTime("$h:$m")
                        showStartTimePicker = false
                    }
                ) {
                    TimePicker(state = startTimeState)
                }
            }

            if (showEndTimePicker) {
                TimePickerDialog(
                    onDismiss = { showEndTimePicker = false },
                    onConfirm = {
                        val h = endTimeState.hour.toString().padStart(2, '0')
                        val m = endTimeState.minute.toString().padStart(2, '0')
                        viewModel.updateEndTime("$h:$m")
                        showEndTimePicker = false
                    }
                ) {
                    TimePicker(state = endTimeState)
                }
            }

            // Player counts
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.minPlayers.toString(),
                    onValueChange = { viewModel.updateMinPlayers(it.toIntOrNull() ?: 2) },
                    label = { Text("Min Players") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.maxPlayers.toString(),
                    onValueChange = { viewModel.updateMaxPlayers(it.toIntOrNull() ?: 10) },
                    label = { Text("Max Players") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Skill level range
            Text("Skill Level: ${skillLabel(uiState.minSkillLevel)} – ${skillLabel(uiState.maxSkillLevel)}")
            RangeSlider(
                min = uiState.minSkillLevel,
                max = uiState.maxSkillLevel,
                onMinChange = viewModel::updateMinSkillLevel,
                onMaxChange = viewModel::updateMaxSkillLevel
            )

            // Location Name with venue search suggestions
            Box {
                Column {
                    OutlinedTextField(
                        value = uiState.locationName,
                        onValueChange = viewModel::updateLocationName,
                        label = { Text("Location Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (uiState.isSearchingVenues) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else if (uiState.selectedVenueId != null) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Venue selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        supportingText = if (uiState.selectedVenueId != null) {
                            { Text("Linked to venue", color = MaterialTheme.colorScheme.primary) }
                        } else {
                            { Text("Type to search venues or enter custom location") }
                        }
                    )

                    // Venue suggestion dropdown
                    if (uiState.showSuggestions && uiState.venueSuggestions.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            uiState.venueSuggestions.forEach { venue ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectVenue(venue) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = venue.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (!venue.address.isNullOrBlank()) {
                                            Text(
                                                text = venue.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = uiState.address,
                onValueChange = viewModel::updateAddress,
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = uiState.selectedVenueId != null
            )

            // Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Free to join")
                Switch(checked = uiState.isFree, onCheckedChange = viewModel::updateIsFree)
            }
            if (!uiState.isFree) {
                OutlinedTextField(
                    value = if (uiState.costPerPlayer == 0.0) "" else uiState.costPerPlayer.toString(),
                    onValueChange = { viewModel.updateCostPerPlayer(it.toDoubleOrNull() ?: 0.0) },
                    label = { Text("Cost per Player ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            // Error
            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::createMatch,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isValid && !uiState.isCreating
            ) {
                Text(if (uiState.isCreating) "Creating..." else "Create Match")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SportDropdown(selected: SportType, onSelected: (SportType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sport") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SportType.entries.forEach { sport ->
                DropdownMenuItem(
                    text = { Text(sport.displayName) },
                    onClick = { onSelected(sport); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun RangeSlider(min: Int, max: Int, onMinChange: (Int) -> Unit, onMaxChange: (Int) -> Unit) {
    Column {
        Text("Min: ${skillLabel(min)}", style = MaterialTheme.typography.labelSmall)
        Slider(
            value = min.toFloat(),
            onValueChange = { onMinChange(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3
        )
        Text("Max: ${skillLabel(max)}", style = MaterialTheme.typography.labelSmall)
        Slider(
            value = max.toFloat(),
            onValueChange = { onMaxChange(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3
        )
    }
}

private fun skillLabel(level: Int): String = when (level) {
    1 -> "Newbie"; 2 -> "Beginner"; 3 -> "Intermediate"; 4 -> "Semi Pro"; 5 -> "Pro"; else -> "Lvl $level"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select time",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )
                content()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun CreateMatchScreenPreview() {
    val previewState = CreateMatchUiState(
        title = "Sunday Basketball",
        description = "Casual pickup game, all skill levels welcome",
        sportType = SportType.BASKETBALL,
        matchType = MatchType.STANDALONE,
        visibility = MatchVisibility.PUBLIC,
        matchDate = "2026-03-30",
        startTime = "18:00",
        endTime = "19:30",
        minPlayers = 6,
        maxPlayers = 12,
        minSkillLevel = 2,
        maxSkillLevel = 4,
        locationName = "City Sports Center",
        address = "123 Main St",
        isFree = true
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Match") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = previewState.title,
                onValueChange = {},
                label = { Text("Match Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = previewState.description,
                onValueChange = {},
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            SportDropdown(selected = previewState.sportType, onSelected = {})
            Text("Skill Level: ${skillLabel(previewState.minSkillLevel)} – ${skillLabel(previewState.maxSkillLevel)}")
            RangeSlider(
                min = previewState.minSkillLevel,
                max = previewState.maxSkillLevel,
                onMinChange = {},
                onMaxChange = {}
            )
            OutlinedTextField(
                value = previewState.locationName,
                onValueChange = {},
                label = { Text("Location Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Free to join")
                Switch(checked = previewState.isFree, onCheckedChange = {})
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            ) {
                Text("Create Match")
            }
        }
    }
}
