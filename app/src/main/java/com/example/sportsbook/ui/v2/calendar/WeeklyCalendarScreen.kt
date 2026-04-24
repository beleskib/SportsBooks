package com.example.sportsbook.ui.v2.calendar

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.ui.theme.CoolGray
import com.example.sportsbook.ui.theme.Navy600
import com.example.sportsbook.ui.theme.Navy700
import com.example.sportsbook.ui.theme.Navy800
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ============================================================
// v2-practical-ux: Weekly Calendar Screen for Partners
// Mon-Sun × 9-22h grid.
//   - Empty cell (tap) → add available slot
//   - Available cell (tap) → remove slot
//   - Booked cell → read-only
// Mirrors WeeklyCalendarPage.tsx; drag-to-select replaced
// with tap semantics that work naturally on touch screens.
// ============================================================

private val HOURS = (9..22).toList()
private val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val displayDateFmt = DateTimeFormatter.ofPattern("d/M")

private val ColorAvailable = Color(0xFF166534) // deep green
private val ColorAvailableBg = Color(0xFFDCFCE7)
private val ColorBooked = Color(0xFF92400E)
private val ColorBookedBg = Color(0xFFFEF3C7)
private val ColorEmpty = Navy700
private val ColorEmptyBorder = Navy600
private val ColorToday = Color(0xFF1D4ED8)

private enum class CellState { EMPTY, AVAILABLE, BOOKED }

private data class CellInfo(
    val date: String,
    val hour: Int,
    val state: CellState,
    val slot: TimeSlot?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyCalendarScreen(
    onBack: (() -> Unit)? = null,
    viewModel: WeeklyCalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Navy900,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Weekly Calendar",
                        color = WarmWhite,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900),
                navigationIcon = if (onBack != null) {
                    {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WarmWhite)
                        }
                    }
                } else ({})
            )
        }
    ) { innerPadding ->
        if (state.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = USOpenGold)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---- Header description ----
            Text(
                text = "Tap an empty cell to mark it available. Tap an available cell to remove it.",
                style = MaterialTheme.typography.bodySmall,
                color = CoolGray
            )

            // ---- Entity selector row ----
            EntitySelectorRow(
                state = state,
                onEntityTypeSelected = viewModel::selectEntityType,
                onEntitySelected = viewModel::selectEntity
            )

            // ---- Week navigation ----
            WeekNavigationRow(
                weekStart = state.weekStart,
                busy = state.busy,
                onPrev = viewModel::prevWeek,
                onToday = viewModel::goToToday,
                onNext = viewModel::nextWeek
            )

            // ---- Grid ----
            CalendarGrid(
                weekStart = state.weekStart,
                slots = state.slots,
                onCellTap = { date, hour, slotId ->
                    when {
                        slotId != null -> viewModel.removeSlot(slotId)
                        else -> viewModel.addSlot(date, hour)
                    }
                }
            )

            // ---- Legend ----
            Legend(busy = state.busy)
        }
    }
}

// ============================================================
// Entity selector
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntitySelectorRow(
    state: WeeklyCalendarUiState,
    onEntityTypeSelected: (V2EntityType) -> Unit,
    onEntitySelected: (Long) -> Unit
) {
    val hasVenues = state.venues.isNotEmpty()
    val hasCoach = state.myCoach != null
    val showTypeToggle = hasVenues && hasCoach

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showTypeToggle) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = if (state.entityType == V2EntityType.VENUE) "Venue" else "Coach",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .width(120.dp)
                        .menuAnchor(),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = WarmWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = USOpenGold,
                        unfocusedBorderColor = Navy600
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Navy700)
                ) {
                    if (hasVenues) {
                        DropdownMenuItem(
                            text = { Text("Venue", color = WarmWhite) },
                            onClick = { onEntityTypeSelected(V2EntityType.VENUE); expanded = false }
                        )
                    }
                    if (hasCoach) {
                        DropdownMenuItem(
                            text = { Text("Coach", color = WarmWhite) },
                            onClick = { onEntityTypeSelected(V2EntityType.COACH); expanded = false }
                        )
                    }
                }
            }
        }

        // Entity picker (only shows if > 1 option in the current type)
        val entityOptions = when (state.entityType) {
            V2EntityType.VENUE -> state.venues.map { V2EntityOption(it.id, it.name) }
            V2EntityType.COACH -> listOfNotNull(state.myCoach?.let { V2EntityOption(it.id, it.name) })
        }
        if (entityOptions.size > 1) {
            var expanded by remember { mutableStateOf(false) }
            val selectedLabel = entityOptions.firstOrNull { it.id == state.selectedEntityId }?.label ?: ""
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedLabel,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .weight(1f)
                        .menuAnchor(),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = WarmWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = USOpenGold,
                        unfocusedBorderColor = Navy600
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Navy700)
                ) {
                    entityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label, color = WarmWhite) },
                            onClick = { onEntitySelected(option.id); expanded = false }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Week navigation
// ============================================================

@Composable
private fun WeekNavigationRow(
    weekStart: LocalDate,
    busy: Boolean,
    onPrev: () -> Unit,
    onToday: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val weekEnd = weekStart.plusDays(6)
            Text(
                text = "${weekStart.format(displayDateFmt)} – ${weekEnd.format(displayDateFmt)}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = WarmWhite
            )
            if (busy) {
                Spacer(Modifier.width(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = USOpenGold, strokeWidth = 2.dp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onPrev, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous week", tint = WarmWhite)
            }
            OutlinedButton(
                onClick = onToday,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(32.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Navy600),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text("Today", color = WarmWhite, style = MaterialTheme.typography.labelMedium)
            }
            IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next week", tint = WarmWhite)
            }
        }
    }
}

// ============================================================
// Grid
// ============================================================

@Composable
private fun CalendarGrid(
    weekStart: LocalDate,
    slots: List<TimeSlot>,
    onCellTap: (date: String, hour: Int, slotId: Long?) -> Unit
) {
    val today = LocalDate.now().format(dateFmt)

    // Build a lookup map: "date|hour" → TimeSlot
    val slotMap = remember(slots) {
        buildMap {
            slots.forEach { slot ->
                val hour = slot.startTime.split(":").firstOrNull()?.toIntOrNull() ?: return@forEach
                put("${slot.slotDate}|$hour", slot)
            }
        }
    }

    // Build grid[day][hour]
    val grid: List<List<CellInfo>> = remember(weekStart, slotMap) {
        DAY_LABELS.mapIndexed { dIdx, _ ->
            val date = weekStart.plusDays(dIdx.toLong()).format(dateFmt)
            HOURS.map { hour ->
                val slot = slotMap["$date|$hour"]
                val state = when {
                    slot == null -> CellState.EMPTY
                    slot.isAvailable -> CellState.AVAILABLE
                    else -> CellState.BOOKED
                }
                CellInfo(date = date, hour = hour, state = state, slot = slot)
            }
        }
    }

    // Fixed column widths; hour label + 7 day columns
    val hourColWidth = 44.dp
    val dayColWidth = 36.dp

    Column {
        // Header row
        Row {
            Box(
                modifier = Modifier
                    .width(hourColWidth)
                    .height(40.dp)
                    .background(Navy800)
                    .border(0.5.dp, Navy600),
                contentAlignment = Alignment.Center
            ) {
                Text("Hr", style = MaterialTheme.typography.labelSmall, color = CoolGray)
            }
            DAY_LABELS.mapIndexed { idx, label ->
                val date = weekStart.plusDays(idx.toLong()).format(dateFmt)
                val isToday = date == today
                Box(
                    modifier = Modifier
                        .width(dayColWidth)
                        .height(40.dp)
                        .background(if (isToday) Color(0xFF1E3A8A) else Navy800)
                        .border(0.5.dp, Navy600),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = if (isToday) Color(0xFF93C5FD) else WarmWhite
                        )
                        Text(
                            text = weekStart.plusDays(idx.toLong()).format(displayDateFmt),
                            style = MaterialTheme.typography.labelSmall,
                            color = CoolGray
                        )
                    }
                }
            }
        }

        // Data rows
        HOURS.forEachIndexed { hIdx, hour ->
            Row {
                // Hour label
                Box(
                    modifier = Modifier
                        .width(hourColWidth)
                        .height(36.dp)
                        .background(Navy800)
                        .border(0.5.dp, Navy600),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${hour.toString().padStart(2, '0')}:00",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoolGray
                    )
                }
                // Day cells
                grid.forEachIndexed { _, dayCells ->
                    val cell = dayCells[hIdx]
                    val (bgColor, borderColor) = when (cell.state) {
                        CellState.EMPTY -> Pair(ColorEmpty, ColorEmptyBorder)
                        CellState.AVAILABLE -> Pair(ColorAvailableBg.copy(alpha = 0.18f), Color(0xFF16A34A))
                        CellState.BOOKED -> Pair(ColorBookedBg.copy(alpha = 0.15f), Color(0xFFD97706))
                    }
                    Box(
                        modifier = Modifier
                            .width(dayColWidth)
                            .height(36.dp)
                            .background(bgColor)
                            .border(0.5.dp, borderColor)
                            .then(
                                if (cell.state != CellState.BOOKED) {
                                    Modifier.clickable {
                                        onCellTap(cell.date, cell.hour, cell.slot?.id)
                                    }
                                } else Modifier
                            )
                    )
                }
            }
        }
    }
}

// ============================================================
// Legend
// ============================================================

@Composable
private fun Legend(busy: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = Color(0xFF16A34A), label = "Available")
        LegendItem(color = Color(0xFFD97706), label = "Booked")
        LegendItem(color = Navy700, label = "Empty")
        if (busy) {
            Spacer(Modifier.weight(1f))
            Text(
                text = "Saving\u2026",
                style = MaterialTheme.typography.labelSmall,
                color = USOpenGold
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
            modifier = Modifier.size(12.dp),
            color = color,
            shape = RoundedCornerShape(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
        ) {}
        Text(label, style = MaterialTheme.typography.labelSmall, color = CoolGray)
    }
}

// ============================================================
// Previews
// ============================================================

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun WeeklyCalendarScreenPreview() {
    SportsBookTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Weekly Calendar",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = WarmWhite
            )
            WeekNavigationRow(
                weekStart = LocalDate.of(2026, 4, 20),
                busy = false,
                onPrev = {},
                onToday = {},
                onNext = {}
            )
            CalendarGrid(
                weekStart = LocalDate.of(2026, 4, 20),
                slots = listOf(
                    TimeSlot(
                        id = 1L, venueId = 1L, slotDate = "2026-04-21",
                        startTime = "10:00", endTime = "11:00", isAvailable = true
                    ),
                    TimeSlot(
                        id = 2L, venueId = 1L, slotDate = "2026-04-22",
                        startTime = "14:00", endTime = "15:00", isAvailable = false
                    )
                ),
                onCellTap = { _, _, _ -> }
            )
            Legend(busy = false)
        }
    }
}
