package com.example.sportsbook.ui.screens.partner.timeslots

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.TimeSlotRepository
import com.example.sportsbook.domain.repository.VenueRepository
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.LightBg
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.TextPrimary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

// ─── ViewModel ────────────────────────────────────────────

data class TimeSlotManagementState(
    val venues: List<Venue> = emptyList(),
    val coachProfile: Coach? = null,
    val selectedEntityType: String = "venue", // "venue" or "coach"
    val selectedEntityId: Long? = null,
    val selectedEntityName: String = "",
    val dateFrom: String = "",
    val dateTo: String = "",
    val startHour: Int = 9,
    val endHour: Int = 22,
    val selectedDays: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6),
    val slots: List<TimeSlot> = emptyList(),
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class TimeSlotManagementViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    private val coachRepository: CoachRepository,
    private val timeSlotRepository: TimeSlotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimeSlotManagementState())
    val uiState: StateFlow<TimeSlotManagementState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now()
        val nextWeek = today.plusDays(7)
        _uiState.update {
            it.copy(
                dateFrom = today.toString(),
                dateTo = nextWeek.toString()
            )
        }
        loadEntities()
    }

    private fun loadEntities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            venueRepository.getMyVenues().onSuccess { venues ->
                _uiState.update { state ->
                    val newState = state.copy(venues = venues)
                    if (venues.isNotEmpty() && state.selectedEntityId == null) {
                        newState.copy(
                            selectedEntityId = venues.first().id,
                            selectedEntityType = "venue",
                            selectedEntityName = venues.first().name
                        )
                    } else newState
                }
            }

            coachRepository.getMyCoachProfile().onSuccess { coach ->
                _uiState.update { state ->
                    val newState = state.copy(coachProfile = coach)
                    if (state.venues.isEmpty() && coach != null && state.selectedEntityId == null) {
                        newState.copy(
                            selectedEntityId = coach.id,
                            selectedEntityType = "coach",
                            selectedEntityName = coach.name
                        )
                    } else newState
                }
            }

            _uiState.update { it.copy(isLoading = false) }
            loadSlots()
        }
    }

    fun selectEntity(type: String, id: Long, name: String) {
        _uiState.update {
            it.copy(
                selectedEntityType = type,
                selectedEntityId = id,
                selectedEntityName = name
            )
        }
        loadSlots()
    }

    fun setDateRange(from: String, to: String) {
        _uiState.update { it.copy(dateFrom = from, dateTo = to) }
        loadSlots()
    }

    fun setStartHour(hour: Int) {
        _uiState.update { it.copy(startHour = hour) }
    }

    fun setEndHour(hour: Int) {
        _uiState.update { it.copy(endHour = hour) }
    }

    fun toggleDay(day: Int) {
        _uiState.update { state ->
            val current = state.selectedDays.toMutableList()
            if (current.contains(day)) current.remove(day) else current.add(day)
            state.copy(selectedDays = current.sorted())
        }
    }

    fun selectWeekdays() {
        _uiState.update { it.copy(selectedDays = listOf(1, 2, 3, 4, 5)) }
    }

    fun selectAllDays() {
        _uiState.update { it.copy(selectedDays = listOf(0, 1, 2, 3, 4, 5, 6)) }
    }

    fun loadSlots() {
        val state = _uiState.value
        if (state.selectedEntityId == null || state.dateFrom.isBlank() || state.dateTo.isBlank()) return

        viewModelScope.launch {
            timeSlotRepository.getAvailableSlots(
                venueId = if (state.selectedEntityType == "venue") state.selectedEntityId else null,
                coachId = if (state.selectedEntityType == "coach") state.selectedEntityId else null,
                dateFrom = state.dateFrom,
                dateTo = state.dateTo
            ).onSuccess { slots ->
                _uiState.update { it.copy(slots = slots) }
            }
        }
    }

    fun generateSlots() {
        val state = _uiState.value
        if (state.selectedEntityId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null, successMessage = null) }

            timeSlotRepository.generateSlots(
                venueId = if (state.selectedEntityType == "venue") state.selectedEntityId else null,
                coachId = if (state.selectedEntityType == "coach") state.selectedEntityId else null,
                dateFrom = state.dateFrom,
                dateTo = state.dateTo,
                startHour = state.startHour,
                endHour = state.endHour,
                daysOfWeek = state.selectedDays
            ).onSuccess { generated ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        successMessage = "Generated ${generated.size} time slots"
                    )
                }
                loadSlots()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isGenerating = false, error = e.message ?: "Failed to generate slots")
                }
            }
        }
    }

    fun deleteSlot(slotId: Long) {
        viewModelScope.launch {
            timeSlotRepository.deleteSlot(slotId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(slots = state.slots.filter { it.id != slotId })
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to delete slot") }
                }
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

// ─── Screen ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimeSlotManagementScreen(
    onBack: () -> Unit,
    viewModel: TimeSlotManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showGenerateSection by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NavBarBg,
        topBar = {
            TopAppBar(
                title = { Text("Time Slots", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavBarBg)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── Entity Selector ──────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Text("Select Venue or Coach", style = MaterialTheme.typography.labelLarge, color = TextPrimary.copy(alpha = 0.7f))
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.venues.forEach { venue ->
                        FilterChip(
                            selected = uiState.selectedEntityType == "venue" && uiState.selectedEntityId == venue.id,
                            onClick = { viewModel.selectEntity("venue", venue.id, venue.name) },
                            label = { Text(venue.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = NavBarBg,
                                containerColor = LightBg,
                                labelColor = TextPrimary
                            )
                        )
                    }
                    uiState.coachProfile?.let { coach ->
                        FilterChip(
                            selected = uiState.selectedEntityType == "coach" && uiState.selectedEntityId == coach.id,
                            onClick = { viewModel.selectEntity("coach", coach.id, coach.name) },
                            label = { Text(coach.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = NavBarBg,
                                containerColor = LightBg,
                                labelColor = TextPrimary
                            )
                        )
                    }
                }
            }

            // ─── Date Range ───────────────────────
            item {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = LightBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Date Range", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = { showDatePicker = true },
                                label = {
                                    Text(
                                        "${formatDateShort(uiState.dateFrom)} → ${formatDateShort(uiState.dateTo)}",
                                        color = TextPrimary
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarMonth, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                                }
                            )
                        }
                    }
                }
            }

            // ─── Messages ─────────────────────────
            uiState.successMessage?.let { msg ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(msg, color = Color(0xFF81C784), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            IconButton(onClick = { viewModel.dismissMessage() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            uiState.error?.let { err ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(err, color = Color(0xFFEF9A9A), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            IconButton(onClick = { viewModel.dismissMessage() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color(0xFFEF9A9A), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // ─── Generate Section Toggle ──────────
            item {
                Button(
                    onClick = { showGenerateSection = !showGenerateSection },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showGenerateSection) LightBg else GoldAccent,
                        contentColor = if (showGenerateSection) TextPrimary else NavBarBg
                    )
                ) {
                    Icon(if (showGenerateSection) Icons.Default.Close else Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (showGenerateSection) "Hide Generator" else "Generate Time Slots")
                }
            }

            // ─── Generate Configuration ───────────
            if (showGenerateSection) {
                item {
                    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = LightBg)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Operating Hours
                            Text("Operating Hours", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HourPicker(
                                    label = "From",
                                    value = uiState.startHour,
                                    range = 0 until uiState.endHour,
                                    onValueChange = { viewModel.setStartHour(it) }
                                )
                                Text("to", color = TextPrimary.copy(alpha = 0.6f))
                                HourPicker(
                                    label = "To",
                                    value = uiState.endHour,
                                    range = (uiState.startHour + 1)..23,
                                    onValueChange = { viewModel.setEndHour(it) }
                                )
                                Text(
                                    "${uiState.endHour - uiState.startHour}h/day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldAccent
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // Days of Week
                            Text("Available Days", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Spacer(Modifier.height(8.dp))

                            val dayLabels = listOf("Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4, "Fri" to 5, "Sat" to 6, "Sun" to 0)

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                dayLabels.forEach { (label, dayNum) ->
                                    val selected = uiState.selectedDays.contains(dayNum)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) GoldAccent else NavBarBg.copy(alpha = 0.5f))
                                            .border(1.dp, if (selected) GoldAccent else TextPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .clickable { viewModel.toggleDay(dayNum) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) NavBarBg else TextPrimary.copy(alpha = 0.7f),
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TextButton(onClick = { viewModel.selectWeekdays() }) {
                                    Text("Weekdays", color = GoldAccent, style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(onClick = { viewModel.selectAllDays() }) {
                                    Text("Every day", color = GoldAccent, style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.generateSlots() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isGenerating && uiState.selectedEntityId != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldAccent,
                                    contentColor = NavBarBg
                                )
                            ) {
                                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (uiState.isGenerating) "Generating..." else "Generate Slots",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ─── Existing Slots ───────────────────
            val slotsByDate = uiState.slots.groupBy { it.slotDate }.toSortedMap()

            if (slotsByDate.isEmpty() && uiState.selectedEntityId != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = TextPrimary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No time slots for this range",
                                color = TextPrimary.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Generate slots to get started",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary.copy(alpha = 0.3f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            slotsByDate.forEach { (date, daySlots) ->
                item {
                    val localDate = LocalDate.parse(date)
                    val dayName = localDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    val formatted = localDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))

                    Text(
                        "$dayName, $formatted",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        daySlots.sortedBy { it.startTime }.forEach { slot ->
                            val bgColor = if (slot.isAvailable) Color(0xFF1B5E20).copy(alpha = 0.3f) else Color(0xFFB71C1C).copy(alpha = 0.3f)
                            val borderColor = if (slot.isAvailable) Color(0xFF4CAF50).copy(alpha = 0.4f) else Color(0xFFEF5350).copy(alpha = 0.4f)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        slot.startTime.take(5),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (slot.isAvailable) {
                                        Spacer(Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFB71C1C).copy(alpha = 0.6f))
                                                .clickable { viewModel.deleteSlot(slot.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Close, null, tint = TextPrimary, modifier = Modifier.size(12.dp))
                                        }
                                    } else {
                                        Spacer(Modifier.width(6.dp))
                                        Text("Booked", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF9A9A))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    // Date Range Picker Dialog
    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val startMillis = dateRangePickerState.selectedStartDateMillis
                    val endMillis = dateRangePickerState.selectedEndDateMillis
                    if (startMillis != null && endMillis != null) {
                        val from = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                        val to = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                        viewModel.setDateRange(from, to)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(state = dateRangePickerState, modifier = Modifier.height(500.dp))
        }
    }
}

// ─── Helper Composables ───────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HourPicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = "%02d:00".format(value),
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = TextPrimary.copy(alpha = 0.6f)) },
            modifier = Modifier.width(100.dp).menuAnchor(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            range.forEach { h ->
                DropdownMenuItem(
                    text = { Text("%02d:00".format(h)) },
                    onClick = {
                        onValueChange(h)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatDateShort(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        date.format(DateTimeFormatter.ofPattern("dd-MM"))
    } catch (_: Exception) {
        dateStr
    }
}

@Preview(showBackground = true)
@Composable
private fun TimeSlotManagementScreenPreview() {
    MaterialTheme {
        Scaffold(
            containerColor = NavBarBg,
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Time Slots", color = TextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NavBarBg)
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Select Venue or Coach",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text("City Sports Hall") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = NavBarBg,
                                containerColor = LightBg,
                                labelColor = TextPrimary
                            )
                        )
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text("Jane Doe") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = NavBarBg,
                                containerColor = LightBg,
                                labelColor = TextPrimary
                            )
                        )
                    }
                }

                item {
                    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = LightBg)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Date Range", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Spacer(Modifier.height(8.dp))
                            AssistChip(
                                onClick = {},
                                label = { Text("24-03 → 31-03", color = TextPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarMonth, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                                }
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavBarBg)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Generate Time Slots")
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = TextPrimary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No time slots for this range",
                                color = TextPrimary.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Generate slots to get started",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary.copy(alpha = 0.3f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}
