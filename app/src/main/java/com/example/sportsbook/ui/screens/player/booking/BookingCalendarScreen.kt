package com.example.sportsbook.ui.screens.player.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkNavBar
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.DarkTextTertiary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── Screen ───────────────────────────────────────────────────────────────────

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
    val dayFormatter = DateTimeFormatter.ofPattern("EEE")
    val monthFormatter = DateTimeFormatter.ofPattern("MMM")
    val dayNumFormatter = DateTimeFormatter.ofPattern("d")

    var selectedDateIndex by remember { mutableIntStateOf(0) }
    var selectedSlotId by remember { mutableStateOf<Long?>(null) }

    // Displayed price from selected slot or venue/coach base price
    val displayedPrice = uiState.availableSlots.find { it.id == selectedSlotId }?.priceOverride?.toInt()
        ?: uiState.availableSlots.firstOrNull { it.isAvailable }?.priceOverride?.toInt()
        ?: uiState.entityPricePerHour.takeIf { it > 0 }?.toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkTextPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Book a Slot", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                Text("Select date, time & court", fontSize = 12.sp, color = DarkTextSecondary)
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurface),
                contentAlignment = Alignment.Center,
            ) {
                Text("♡", fontSize = 16.sp, color = DarkTextPrimary)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Venue mini card ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.linearGradient(listOf(GreenDark, GreenAccent))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(uiState.entitySportEmoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(uiState.entityName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                    if (uiState.entityAddress.isNotBlank()) {
                        Text(uiState.entityAddress, fontSize = 12.sp, color = DarkTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Select Date ──────────────────────────────────────────────
            SectionLabel("Select Date")
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                nextSevenDays.forEachIndexed { index, date ->
                    val isActive = index == selectedDateIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) GreenAccent else DarkSurface)
                            .clickable {
                                selectedDateIndex = index
                                viewModel.onDateChange(date.toString())
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = date.format(dayFormatter).uppercase(),
                            fontSize = 10.sp,
                            color = if (isActive) Color.White.copy(alpha = 0.8f) else DarkTextSecondary,
                        )
                        Text(
                            text = date.format(dayNumFormatter),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary,
                        )
                        Text(
                            text = date.format(monthFormatter),
                            fontSize = 9.sp,
                            color = if (isActive) Color.White.copy(alpha = 0.8f) else DarkTextSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Sport badge ──────────────────────────────────────────────
            val sportName = (uiState.venue?.sportType ?: uiState.coach?.sportType)?.name
                ?.lowercase()?.replaceFirstChar { it.uppercase() }
            if (sportName != null) {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(GreenAccent)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text("${uiState.entitySportEmoji} $sportName", fontSize = 13.sp, color = DarkTextPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Select Time ──────────────────────────────────────────────
            SectionLabel("Select Time")
            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenAccent)
                    }
                }
                uiState.error != null -> {
                    ErrorView(message = uiState.error!!, onRetry = viewModel::loadSlots, modifier = Modifier.fillMaxWidth())
                }
                else -> {
                    val displaySlots = uiState.availableSlots

                    if (displaySlots.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                .background(com.example.sportsbook.ui.theme.DarkSurface)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No available time slots for this date",
                                fontSize = 13.sp,
                                color = com.example.sportsbook.ui.theme.DarkTextSecondary,
                            )
                        }
                    } else {
                        val morning = displaySlots.filter { it.startTime < "12:00" }
                        val afternoon = displaySlots.filter { it.startTime >= "12:00" && it.startTime < "17:00" }
                        val evening = displaySlots.filter { it.startTime >= "17:00" }

                        if (morning.isNotEmpty()) {
                            TimeGroupSection("Morning", morning, selectedSlotId) { selectedSlotId = it; viewModel.onSlotSelected(it) }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        if (afternoon.isNotEmpty()) {
                            TimeGroupSection("Afternoon", afternoon, selectedSlotId) { selectedSlotId = it; viewModel.onSlotSelected(it) }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        if (evening.isNotEmpty()) {
                            TimeGroupSection("Evening", evening, selectedSlotId) { selectedSlotId = it; viewModel.onSlotSelected(it) }
                        }
                    }
                }
            }

            // ── Active discount badge ─────────────────────────────────
            uiState.activeDiscount?.let { discount ->
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenAccent.copy(alpha = 0.15f))
                        .border(1.dp, GreenAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🏷️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(discount.title.ifBlank { "Discount Available" }, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GreenAccent)
                        Text(discount.displayValue, fontSize = 12.sp, color = DarkTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // ── Bottom bar ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkNavBar)
                .border(width = 1.dp, color = DarkBorder, shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("From", fontSize = 11.sp, color = DarkTextSecondary)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${displayedPrice ?: "—"} MKD",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GreenAccent,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("/hour", fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(bottom = 2.dp))
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedSlotId != null) GreenAccent else DarkSurface)
                    .clickable {
                        val slotId = selectedSlotId ?: uiState.selectedSlotId
                        if (slotId != null) onSlotSelected(slotId)
                    }
                    .padding(horizontal = 32.dp, vertical = 14.dp),
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ── Helper composables ───────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = DarkTextPrimary,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun TimeGroupSection(
    label: String,
    slots: List<TimeSlot>,
    selectedSlotId: Long?,
    onSelect: (Long) -> Unit,
) {
    Text(
        text = label,
        fontSize = 13.sp,
        color = DarkTextSecondary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
    // 4-column grid in a non-lazy way (slots ≤ 4 per period in practice)
    val rows = slots.chunked(4)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { rowSlots ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowSlots.forEach { slot ->
                    TimeSlotCell(
                        slot = slot,
                        isSelected = slot.id == selectedSlotId,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Fill empty cells in last row
                repeat(4 - rowSlots.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TimeSlotCell(
    slot: TimeSlot,
    isSelected: Boolean,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = when {
        isSelected -> GreenAccent
        !slot.isAvailable -> Color(0xFF2A2A2A)
        else -> DarkSurface
    }
    val borderColor = when {
        isSelected -> GreenAccent
        !slot.isAvailable -> Color.Transparent
        else -> Color(0xFF333333)
    }
    val textColor = when {
        isSelected -> Color.White
        !slot.isAvailable -> DarkTextTertiary
        else -> DarkTextPrimary
    }
    val decoration = if (!slot.isAvailable) TextDecoration.LineThrough else TextDecoration.None

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(enabled = slot.isAvailable && !isSelected) { onSelect(slot.id) }
            .padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        Text(
            text = slot.startTime,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            textDecoration = decoration,
        )
        Text(
            text = if (slot.isAvailable) "${slot.priceOverride?.toInt() ?: 800} MKD" else "Booked",
            fontSize = 10.sp,
            color = if (isSelected) Color.White.copy(alpha = 0.8f) else DarkTextSecondary,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}


// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun BookingCalendarScreenPreview() {
    val today = LocalDate.now()
    val nextSevenDays = (0..6).map { today.plusDays(it.toLong()) }
    val dayFormatter = DateTimeFormatter.ofPattern("EEE")
    val monthFormatter = DateTimeFormatter.ofPattern("MMM")
    val dayNumFormatter = DateTimeFormatter.ofPattern("d")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Book a Slot", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                    Text("Select date, time & court", fontSize = 12.sp, color = DarkTextSecondary)
                }
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                nextSevenDays.forEachIndexed { index, date ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (index == 1) GreenAccent else DarkSurface)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Text(date.format(dayFormatter).uppercase(), fontSize = 10.sp, color = if (index == 1) Color.White.copy(0.8f) else DarkTextSecondary)
                        Text(date.format(dayNumFormatter), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        Text(date.format(monthFormatter), fontSize = 9.sp, color = if (index == 1) Color.White.copy(0.8f) else DarkTextSecondary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("Select Time")
            Spacer(modifier = Modifier.height(12.dp))
            TimeGroupSection(
                label = "Morning",
                slots = listOf(
                    TimeSlot(id = 0L, startTime = "09:00", endTime = "10:00", priceOverride = 750.0, isAvailable = true),
                    TimeSlot(id = 1L, startTime = "10:00", endTime = "11:00", priceOverride = 750.0, isAvailable = false),
                    TimeSlot(id = 2L, startTime = "11:00", endTime = "12:00", priceOverride = 750.0, isAvailable = true),
                ),
                selectedSlotId = 2L,
                onSelect = {},
            )
        }
    }
