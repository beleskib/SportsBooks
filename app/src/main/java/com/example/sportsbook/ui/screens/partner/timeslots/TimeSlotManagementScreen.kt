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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
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
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Time Slots", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                    if (uiState.selectedEntityName.isNotBlank()) {
                        Text(uiState.selectedEntityName, fontSize = 13.sp, color = DarkTextSecondary)
                    }
                }
                // Slot count badge
                if (uiState.slots.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text("${uiState.slots.size} slots", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GreenAccent)
                    }
                }
            }

            // ── Loading / Content ─────────────────────────────────────────
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // ─── Entity Selector ──────────────────
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("Select Venue or Coach", fontSize = 12.sp, color = DarkTextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.venues.forEach { venue ->
                                val selected = uiState.selectedEntityType == "venue" && uiState.selectedEntityId == venue.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (selected) GreenAccent else DarkSurface)
                                        .border(1.dp, if (selected) GreenAccent else DarkBorder, RoundedCornerShape(20.dp))
                                        .clickable { viewModel.selectEntity("venue", venue.id, venue.name) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                ) {
                                    Text(venue.name, fontSize = 13.sp, color = if (selected) Color.White else DarkTextSecondary, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                                }
                            }
                            uiState.coachProfile?.let { coach ->
                                val selected = uiState.selectedEntityType == "coach" && uiState.selectedEntityId == coach.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (selected) GreenAccent else DarkSurface)
                                        .border(1.dp, if (selected) GreenAccent else DarkBorder, RoundedCornerShape(20.dp))
                                        .clickable { viewModel.selectEntity("coach", coach.id, coach.name) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                ) {
                                    Text(coach.name, fontSize = 13.sp, color = if (selected) Color.White else DarkTextSecondary, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                                }
                            }
                        }
                    }

                    // ─── Date Range ───────────────────────
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurface)
                                .padding(16.dp),
                        ) {
                            Text("Date Range", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkBg)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                    .clickable { showDatePicker = true }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("📅", fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${formatDateShort(uiState.dateFrom)}  →  ${formatDateShort(uiState.dateTo)}",
                                    fontSize = 14.sp,
                                    color = DarkTextPrimary,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }

                    // ─── Messages ─────────────────────────
                    uiState.successMessage?.let { msg ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1B5E20).copy(alpha = 0.3f))
                                    .border(1.dp, GreenAccent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("✅", fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(msg, fontSize = 13.sp, color = Color(0xFF81C784), modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .clickable { viewModel.dismissMessage() },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color(0xFF81C784), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    uiState.error?.let { err ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFB71C1C).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFFEF5350).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("⚠️", fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(err, fontSize = 13.sp, color = Color(0xFFEF9A9A), modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .clickable { viewModel.dismissMessage() },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color(0xFFEF9A9A), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    // ─── Generate Section Toggle ──────────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (showGenerateSection) DarkSurface else GreenAccent)
                                .clickable { showGenerateSection = !showGenerateSection }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (showGenerateSection) Icons.Default.Close else Icons.Default.Add,
                                    null,
                                    tint = if (showGenerateSection) DarkTextPrimary else Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (showGenerateSection) "Hide Generator" else "⚡ Generate Time Slots",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showGenerateSection) DarkTextPrimary else Color.White,
                                )
                            }
                        }
                    }

                    // ─── Generate Configuration ───────────
                    if (showGenerateSection) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkSurface)
                                    .padding(16.dp),
                            ) {
                                // Operating Hours
                                Text("🕐 Operating Hours", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    HourPicker(
                                        label = "From",
                                        value = uiState.startHour,
                                        range = 0 until uiState.endHour,
                                        onValueChange = { viewModel.setStartHour(it) },
                                    )
                                    Text("→", fontSize = 14.sp, color = DarkTextSecondary)
                                    HourPicker(
                                        label = "To",
                                        value = uiState.endHour,
                                        range = (uiState.startHour + 1)..23,
                                        onValueChange = { viewModel.setEndHour(it) },
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GreenAccent.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                    ) {
                                        Text("${uiState.endHour - uiState.startHour}h/day", fontSize = 12.sp, color = GreenAccent, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                // Days of Week
                                Text("📆 Available Days", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                                Spacer(Modifier.height(10.dp))

                                val dayLabels = listOf("Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4, "Fri" to 5, "Sat" to 6, "Sun" to 0)

                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    dayLabels.forEach { (label, dayNum) ->
                                        val selected = uiState.selectedDays.contains(dayNum)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (selected) GreenAccent else DarkBg)
                                                .border(1.dp, if (selected) GreenAccent else DarkBorder, RoundedCornerShape(8.dp))
                                                .clickable { viewModel.toggleDay(dayNum) }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                label,
                                                fontSize = 13.sp,
                                                color = if (selected) Color.White else DarkTextSecondary,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        "Weekdays",
                                        fontSize = 12.sp,
                                        color = GreenAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { viewModel.selectWeekdays() },
                                    )
                                    Text(
                                        "Every day",
                                        fontSize = 12.sp,
                                        color = GreenAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { viewModel.selectAllDays() },
                                    )
                                }

                                Spacer(Modifier.height(14.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (!uiState.isGenerating && uiState.selectedEntityId != null) GreenAccent else DarkBorder)
                                        .clickable(enabled = !uiState.isGenerating && uiState.selectedEntityId != null) { viewModel.generateSlots() }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (uiState.isGenerating) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                    } else {
                                        Text("⚡ Generate Slots", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // ─── Existing Slots ───────────────────
                    val slotsByDate = uiState.slots.groupBy { it.slotDate }.toSortedMap()

                    if (slotsByDate.isEmpty() && uiState.selectedEntityId != null) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkSurface)
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("📅", fontSize = 40.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("No time slots", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                                Text("Generate slots to get started", fontSize = 13.sp, color = DarkTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    slotsByDate.forEach { (date, daySlots) ->
                        item {
                            val localDate = LocalDate.parse(date)
                            val dayName = localDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                            val formatted = localDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                            Row(
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(GreenAccent))
                                Spacer(Modifier.width(8.dp))
                                Text("$dayName, $formatted", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                            }
                        }

                        item {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                daySlots.sortedBy { it.startTime }.forEach { slot ->
                                    val isAvailable = slot.isAvailable
                                    val bgColor = if (isAvailable) Color(0xFF1B5E20).copy(alpha = 0.25f) else Color(0xFFB71C1C).copy(alpha = 0.2f)
                                    val borderColor = if (isAvailable) GreenAccent.copy(alpha = 0.5f) else Color(0xFFEF5350).copy(alpha = 0.4f)
                                    val textColor = if (isAvailable) Color(0xFF81C784) else Color(0xFFEF9A9A)

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bgColor)
                                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(slot.startTime.take(5), fontSize = 13.sp, color = textColor, fontWeight = FontWeight.Medium)
                                            if (isAvailable) {
                                                Spacer(Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFB71C1C).copy(alpha = 0.6f))
                                                        .clickable { viewModel.deleteSlot(slot.id) },
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                                }
                                            } else {
                                                Spacer(Modifier.width(6.dp))
                                                Text("Booked", fontSize = 10.sp, color = Color(0xFFEF9A9A))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
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
            },
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
    onValueChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Box(
            modifier = Modifier
                .width(90.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkBg)
                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Column {
                Text(label, fontSize = 10.sp, color = DarkTextSecondary)
                Text("%02d:00".format(value), fontSize = 14.sp, color = DarkTextPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            range.forEach { h ->
                DropdownMenuItem(
                    text = { Text("%02d:00".format(h)) },
                    onClick = {
                        onValueChange(h)
                        expanded = false
                    },
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

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun TimeSlotManagementScreenPreview() {
    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg),
    ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text("Time Slots", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(GreenAccent.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text("13 slots", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GreenAccent)
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(GreenAccent).padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text("City Tennis Center", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(DarkSurface).border(1.dp, DarkBorder, RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text("Downtown Basketball", fontSize = 13.sp, color = DarkTextSecondary)
                        }
                    }
                }
                item {
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DarkSurface).padding(16.dp)) {
                        Text("Date Range", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(DarkBg).border(1.dp, DarkBorder, RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("📅", fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("02-06  →  09-06", fontSize = 14.sp, color = DarkTextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                item {
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(GreenAccent).padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                        Text("⚡ Generate Time Slots", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

