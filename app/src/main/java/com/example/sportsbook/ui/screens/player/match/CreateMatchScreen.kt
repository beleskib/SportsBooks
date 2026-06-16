package com.example.sportsbook.ui.screens.player.match

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.MatchPaymentType
import com.example.sportsbook.domain.enums.MatchType
import com.example.sportsbook.domain.enums.MatchVisibility
import com.example.sportsbook.domain.enums.SportType

// ── Color tokens ────────────────────────────────────────────────────────────
private val DarkBg = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkBorder = Color(0xFF2A2A2A)
private val DarkTextPrimary = Color.White
private val DarkTextSecondary = Color(0xFF888888)
private val GreenAccent = Color(0xFF4CAF50)
private val GreenActiveBg = Color(0xFF1B3A1E)

// ── Public screen ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMatchScreen(
    onMatchCreated: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateMatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdMatch) {
        uiState.createdMatch?.let { onMatchCreated(it.id) }
    }

    var autoApprove by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Create Match",
                            color = DarkTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Spacer to balance the back button
                    Spacer(modifier = Modifier.size(36.dp))
                }
            }

            // ── Match Title ──────────────────────────────────────────────
            item {
                FormSection(label = "MATCH TITLE") {
                    DarkTextField(
                        value = uiState.title,
                        onValueChange = viewModel::updateTitle,
                        hint = "e.g. Sunday Basketball Showdown"
                    )
                }
            }

            // ── Sport ────────────────────────────────────────────────────
            item {
                FormSection(label = "SPORT") {
                    SportGrid(
                        selected = uiState.sportType,
                        onSelected = viewModel::updateSportType
                    )
                }
            }

            // ── Match Type ───────────────────────────────────────────────
            item {
                FormSection(label = "MATCH TYPE") {
                    MatchTypeRow(
                        selected = uiState.matchType,
                        onSelected = viewModel::updateMatchType
                    )
                }
            }

            // ── Date & Time ──────────────────────────────────────────────
            item {
                FormSection(label = "DATE & TIME") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DateTimeBox(
                            label = "Date",
                            value = uiState.matchDate.ifBlank { "Select date" },
                            modifier = Modifier.weight(1f),
                            onClick = { showDatePicker = true }
                        )
                        DateTimeBox(
                            label = "Start",
                            value = uiState.startTime.ifBlank { "Start time" },
                            modifier = Modifier.weight(1f),
                            onClick = { showStartTimePicker = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    DateTimeBox(
                        label = "End",
                        value = uiState.endTime.ifBlank { "End time" },
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showEndTimePicker = true }
                    )
                }
            }

            // ── Players ──────────────────────────────────────────────────
            item {
                FormSection(label = "PLAYERS") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PlayerStepper(
                            label = "Min",
                            value = uiState.minPlayers,
                            onDecrement = { if (uiState.minPlayers > 1) viewModel.updateMinPlayers(uiState.minPlayers - 1) },
                            onIncrement = { viewModel.updateMinPlayers(uiState.minPlayers + 1) },
                            modifier = Modifier.weight(1f)
                        )
                        PlayerStepper(
                            label = "Max",
                            value = uiState.maxPlayers,
                            onDecrement = { if (uiState.maxPlayers > uiState.minPlayers) viewModel.updateMaxPlayers(uiState.maxPlayers - 1) },
                            onIncrement = { viewModel.updateMaxPlayers(uiState.maxPlayers + 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Skill Level ──────────────────────────────────────────────
            item {
                FormSection(label = "SKILL LEVEL") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in 1..5) {
                            val filled = i <= uiState.maxSkillLevel
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (filled) GreenAccent else DarkSurface)
                                    .border(
                                        width = 1.dp,
                                        color = if (filled) GreenAccent else DarkBorder,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.updateMaxSkillLevel(i) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$i",
                                    color = if (filled) Color.White else DarkTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Beginner", color = DarkTextSecondary, fontSize = 11.sp)
                        Text("Intermediate", color = DarkTextSecondary, fontSize = 11.sp)
                        Text("Pro", color = DarkTextSecondary, fontSize = 11.sp)
                    }
                }
            }

            // ── Location ─────────────────────────────────────────────────
            item {
                FormSection(label = "LOCATION") {
                    // Location name / venue search
                    DarkTextField(
                        value = uiState.locationName,
                        onValueChange = viewModel::updateLocationName,
                        hint = "Search venue or enter location"
                    )

                    // Venue suggestions dropdown
                    if (uiState.showSuggestions && uiState.venueSuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                        ) {
                            uiState.venueSuggestions.forEachIndexed { index, venue ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectVenue(venue) }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📍", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = venue.name,
                                            color = DarkTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (!venue.address.isNullOrBlank()) {
                                            Text(
                                                text = venue.address,
                                                color = DarkTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                                if (index < uiState.venueSuggestions.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(DarkBorder)
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.isSearchingVenues) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = GreenAccent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Searching venues…", color = DarkTextSecondary, fontSize = 12.sp)
                        }
                    }

                    // Selected venue indicator
                    if (uiState.selectedVenueId != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GreenActiveBg)
                                .border(1.dp, GreenAccent, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏟️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Venue: ${uiState.locationName}",
                                    color = GreenAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (uiState.address.isNotBlank()) {
                                    Text(uiState.address, color = DarkTextSecondary, fontSize = 11.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(DarkBorder)
                                    .clickable {
                                        viewModel.updateLocationName("")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", color = DarkTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }

                    // Address (for pickup games / manual entry)
                    if (uiState.selectedVenueId == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DarkTextField(
                            value = uiState.address,
                            onValueChange = viewModel::updateAddress,
                            hint = "Address (optional)"
                        )
                    }

                    // Available time slots (when venue + date selected)
                    if (uiState.selectedVenueId != null && uiState.matchDate.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "AVAILABLE TIME SLOTS",
                            color = DarkTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (uiState.isLoadingTimeSlots) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = GreenAccent,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Loading time slots…", color = DarkTextSecondary, fontSize = 12.sp)
                            }
                        } else if (uiState.availableTimeSlots.isEmpty()) {
                            Text(
                                text = "No available slots for this date",
                                color = DarkTextSecondary,
                                fontSize = 13.sp
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                uiState.availableTimeSlots.filter { it.isAvailable }.forEach { slot ->
                                    val isSelected = uiState.selectedTimeSlotId == slot.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) GreenActiveBg else DarkSurface)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) GreenAccent else DarkBorder,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { viewModel.selectTimeSlot(slot) }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${slot.startTime} – ${slot.endTime}",
                                            color = if (isSelected) GreenAccent else DarkTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                        if (slot.price > 0) {
                                            Text(
                                                text = "$${String.format("%.2f", slot.price)}",
                                                color = if (isSelected) GreenAccent else DarkTextSecondary,
                                                fontSize = 13.sp
                                            )
                                        } else {
                                            Text("Free", color = GreenAccent, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Payment ──────────────────────────────────────────────────
            item {
                FormSection(label = "PAYMENT") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val paymentOptions = listOf(
                            Triple(MatchPaymentType.SPLIT, "💰", "Split Equally"),
                            Triple(MatchPaymentType.HOST_PAYS, "💳", "Host Pays"),
                            Triple(MatchPaymentType.CASH_AT_VENUE, "🛒", "Cash at Venue")
                        )
                        paymentOptions.forEach { (type, emoji, label) ->
                            val active = uiState.paymentType == type
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (active) GreenActiveBg else DarkSurface)
                                    .border(
                                        width = if (active) 1.5.dp else 1.dp,
                                        color = if (active) GreenAccent else DarkBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.updatePaymentType(type) }
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(emoji, fontSize = 18.sp)
                                Text(
                                    text = label,
                                    color = if (active) GreenAccent else DarkTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // ── Visibility ───────────────────────────────────────────────
            item {
                FormSection(label = "VISIBILITY") {
                    ToggleRow(
                        title = "Public Match",
                        subtitle = "Anyone can join",
                        checked = uiState.visibility == MatchVisibility.PUBLIC,
                        onCheckedChange = { checked ->
                            viewModel.updateVisibility(
                                if (checked) MatchVisibility.PUBLIC else MatchVisibility.PRIVATE
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleRow(
                        title = "Auto-approve",
                        subtitle = "No approval needed",
                        checked = autoApprove,
                        onCheckedChange = { autoApprove = it }
                    )
                }
            }

            // ── Error ────────────────────────────────────────────────────
            if (uiState.error != null) {
                item {
                    Text(
                        text = uiState.error!!,
                        color = Color(0xFFCF6679),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // ── Sticky bottom bar ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(DarkBg)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            val canCreate = uiState.isValid && !uiState.isCreating
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (canCreate) GreenAccent else Color(0xFF2A3D2B))
                    .then(if (canCreate) Modifier.clickable(onClick = viewModel::createMatch) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isCreating) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Creating...", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                } else {
                    Text("Create Match", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = if (canCreate) Color.White else Color(0xFF5A7A5C))
                }
            }
        }

        // ── Date Picker Dialog ──────────────────────────────────────────
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                            viewModel.updateMatchDate(formatted)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK", color = GreenAccent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = DarkTextSecondary)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // ── Start Time Picker Dialog ────────────────────────────────────
        if (showStartTimePicker) {
            val timePickerState = rememberTimePickerState()
            Dialog(onDismissRequest = { showStartTimePicker = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Select Start Time",
                            color = DarkTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TimePicker(state = timePickerState)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showStartTimePicker = false }) {
                                Text("Cancel", color = DarkTextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                val formatted = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                                viewModel.updateStartTime(formatted)
                                showStartTimePicker = false
                            }) {
                                Text("OK", color = GreenAccent)
                            }
                        }
                    }
                }
            }
        }

        // ── End Time Picker Dialog ──────────────────────────────────────
        if (showEndTimePicker) {
            val timePickerState = rememberTimePickerState()
            Dialog(onDismissRequest = { showEndTimePicker = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Select End Time",
                            color = DarkTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TimePicker(state = timePickerState)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showEndTimePicker = false }) {
                                Text("Cancel", color = DarkTextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                val formatted = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                                viewModel.updateEndTime(formatted)
                                showEndTimePicker = false
                            }) {
                                Text("OK", color = GreenAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Private composables ──────────────────────────────────────────────────────

@Composable
private fun FormSection(
    label: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp)
    ) {
        Text(
            text = label,
            color = DarkTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        if (value.isEmpty()) {
            Text(text = hint, color = DarkTextSecondary, fontSize = 14.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = DarkTextPrimary, fontSize = 14.sp),
            cursorBrush = SolidColor(GreenAccent),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SportGrid(
    selected: SportType,
    onSelected: (SportType) -> Unit
) {
    val sports = listOf(
        SportType.BASKETBALL to "🏀",
        SportType.FOOTBALL to "⚽",
        SportType.TENNIS to "🎾",
        SportType.PADDLE to "🏓",
        SportType.VOLLEYBALL to "🏐",
        SportType.BADMINTON to "🏸",
        SportType.BOXING to "🥊",
        SportType.RUNNING to "🏃"
    )
    val columns = 4
    val rows = (sports.size + columns - 1) / columns
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until columns) {
                    val idx = row * columns + col
                    if (idx < sports.size) {
                        val (sport, emoji) = sports[idx]
                        val active = selected == sport
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) GreenActiveBg else DarkSurface)
                                .border(
                                    width = if (active) 1.5.dp else 1.dp,
                                    color = if (active) GreenAccent else DarkBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelected(sport) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(emoji, fontSize = 24.sp)
                            Text(
                                text = sport.displayName,
                                color = if (active) GreenAccent else DarkTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchTypeRow(
    selected: MatchType,
    onSelected: (MatchType) -> Unit
) {
    val options = listOf(
        Triple(MatchType.STANDALONE, "⚔️", "Competitive"),
        Triple(MatchType.STANDALONE, "🎉", "Casual"),
        Triple(MatchType.VENUE_LINKED, "🏆", "Tournament")
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (type, emoji, label) ->
            val active = selected == type && label == when (type) {
                MatchType.STANDALONE -> "Competitive"
                MatchType.VENUE_LINKED -> "Tournament"
                else -> ""
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) GreenActiveBg else DarkSurface)
                    .border(
                        width = if (active) 1.5.dp else 1.dp,
                        color = if (active) GreenAccent else DarkBorder,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelected(type) }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(emoji, fontSize = 20.sp)
                Text(
                    text = label,
                    color = if (active) GreenAccent else DarkTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun DateTimeBox(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Text(text = label, color = DarkTextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = DarkTextPrimary, fontSize = 14.sp)
    }
}

@Composable
private fun PlayerStepper(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = DarkTextSecondary,
            fontSize = 12.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(DarkBorder)
                    .clickable { onDecrement() },
                contentAlignment = Alignment.Center
            ) {
                Text("−", color = DarkTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "$value",
                color = DarkTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(GreenAccent)
                    .clickable { onIncrement() },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = title, color = DarkTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = DarkTextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GreenAccent,
                uncheckedThumbColor = DarkTextSecondary,
                uncheckedTrackColor = DarkBorder
            )
        )
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CreateMatchScreenPreview() {
    val previewState = CreateMatchUiState(
        title = "Sunday Basketball Showdown",
        sportType = SportType.BASKETBALL,
        matchType = MatchType.STANDALONE,
        visibility = MatchVisibility.PUBLIC,
        matchDate = "2026-06-08",
        startTime = "18:00",
        endTime = "19:30",
        minPlayers = 6,
        maxPlayers = 12,
        minSkillLevel = 2,
        maxSkillLevel = 4,
        locationName = "City Sports Center",
        address = "123 Main St",
        isFree = true,
        paymentType = MatchPaymentType.SPLIT
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        "Create Match",
                        color = DarkTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.size(36.dp))
            }

            // Sport grid preview
            FormSection(label = "SPORT") {
                SportGrid(selected = previewState.sportType, onSelected = {})
            }
        }

        // Bottom bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(DarkBg)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GreenAccent),
                contentAlignment = Alignment.Center,
            ) {
                Text("Create Match", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}
