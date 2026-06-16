package com.example.sportsbook.ui.screens.player.venuelobby

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.CreateVenueBookingLobbyRequestDto
import com.example.sportsbook.data.remote.dto.TimeSlotDto
import com.example.sportsbook.data.remote.dto.VenueDto
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.common.toDisplayTime
import com.example.sportsbook.ui.navigation.Route
import com.example.sportsbook.ui.theme.CoralRed
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.SportGreen
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.DarkTextPrimary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class CreateVenueBookingLobbyUiState(
    // Venue selection
    val venues: List<VenueDto> = emptyList(),
    val isLoadingVenues: Boolean = false,
    val selectedVenue: VenueDto? = null,
    val venueSearchQuery: String = "",

    // Time slot selection
    val timeSlots: List<TimeSlotDto> = emptyList(),
    val isLoadingSlots: Boolean = false,
    val selectedTimeSlot: TimeSlotDto? = null,
    val selectedDate: String = "", // yyyy-MM-dd

    // Lobby details
    val title: String = "",
    val paymentType: String = "split",
    val maxPlayers: Int = 2,
    val description: String = "",
    val isCreating: Boolean = false,
    val error: String? = null,
    val createdLobbyId: Long? = null
) {
    val filteredVenues: List<VenueDto>
        get() = if (venueSearchQuery.isBlank()) venues
        else venues.filter {
            it.name.contains(venueSearchQuery, ignoreCase = true) ||
            (it.address.contains(venueSearchQuery, ignoreCase = true)) ||
            it.sportType.name.contains(venueSearchQuery, ignoreCase = true)
        }

    val totalPrice: Double
        get() = selectedTimeSlot?.priceOverride ?: selectedVenue?.pricePerHour ?: 0.0

    val pricePerPlayer: Double
        get() = if (maxPlayers > 0) totalPrice / maxPlayers else totalPrice

    val availableSlots: List<TimeSlotDto>
        get() = timeSlots.filter { it.isAvailable }

    val isValid: Boolean
        get() = title.isNotBlank() && selectedVenue != null && selectedTimeSlot != null
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class CreateVenueBookingLobbyViewModel @Inject constructor(
    private val apiService: ApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.CreateVenueBookingLobby>()

    private val _uiState = MutableStateFlow(CreateVenueBookingLobbyUiState())
    val uiState: StateFlow<CreateVenueBookingLobbyUiState> = _uiState.asStateFlow()

    init {
        // Set today as default date
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        _uiState.update { it.copy(selectedDate = today) }

        if (route.timeSlotId > 0 && route.venueId > 0) {
            // Pre-selected venue and time slot (coming from booking flow)
            loadPreselected(route.venueId, route.timeSlotId)
        } else {
            // Fresh flow — load all venues
            loadVenues()
        }
    }

    private fun loadPreselected(venueId: Long, timeSlotId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingVenues = true) }
            runCatching { apiService.getVenueById(venueId) }
                .onSuccess { r -> _uiState.update { it.copy(selectedVenue = r.data) } }
            runCatching { apiService.getTimeSlotById(timeSlotId) }
                .onSuccess { r -> _uiState.update { it.copy(selectedTimeSlot = r.data) } }
            _uiState.update { it.copy(isLoadingVenues = false) }
        }
    }

    private fun loadVenues() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingVenues = true) }
            runCatching { apiService.getAllVenues() }
                .fold(
                    onSuccess = { r -> _uiState.update { it.copy(isLoadingVenues = false, venues = r.data) } },
                    onFailure = { e -> _uiState.update { it.copy(isLoadingVenues = false, error = e.message) } }
                )
        }
    }

    fun selectVenue(venue: VenueDto) {
        _uiState.update { it.copy(selectedVenue = venue, selectedTimeSlot = null, timeSlots = emptyList()) }
        loadTimeSlotsForVenue(venue.id)
    }

    fun clearVenue() {
        _uiState.update { it.copy(selectedVenue = null, selectedTimeSlot = null, timeSlots = emptyList()) }
        if (_uiState.value.venues.isEmpty()) loadVenues()
    }

    fun onVenueSearchChange(query: String) {
        _uiState.update { it.copy(venueSearchQuery = query) }
    }

    fun onDateSelected(date: String) {
        _uiState.update { it.copy(selectedDate = date, selectedTimeSlot = null) }
        val venue = _uiState.value.selectedVenue ?: return
        loadTimeSlotsForVenue(venue.id)
    }

    private fun loadTimeSlotsForVenue(venueId: Long) {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate
            _uiState.update { it.copy(isLoadingSlots = true) }
            runCatching {
                apiService.getVenueTimeSlots(venueId, dateFrom = date, dateTo = date)
            }.fold(
                onSuccess = { r -> _uiState.update { it.copy(isLoadingSlots = false, timeSlots = r.data) } },
                onFailure = { e -> _uiState.update { it.copy(isLoadingSlots = false, error = e.message) } }
            )
        }
    }

    fun selectTimeSlot(slot: TimeSlotDto) {
        _uiState.update { it.copy(selectedTimeSlot = slot) }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onPaymentTypeChange(type: String) {
        _uiState.update { it.copy(paymentType = type) }
    }

    fun onMaxPlayersIncrease() {
        _uiState.update { it.copy(maxPlayers = (it.maxPlayers + 1).coerceAtMost(10)) }
    }

    fun onMaxPlayersDecrease() {
        _uiState.update { it.copy(maxPlayers = (it.maxPlayers - 1).coerceAtLeast(2)) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun createLobby() {
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, error = null) }
            runCatching {
                apiService.createVenueBookingLobby(
                    CreateVenueBookingLobbyRequestDto(
                        timeSlotId = state.selectedTimeSlot!!.id,
                        title = state.title.trim(),
                        paymentType = state.paymentType,
                        maxPlayers = state.maxPlayers,
                        description = state.description.trim().ifBlank { null }
                    )
                )
            }.fold(
                onSuccess = { response ->
                    _uiState.update { it.copy(isCreating = false, createdLobbyId = response.data.id) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isCreating = false, error = e.message ?: "Failed to create lobby") }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVenueBookingLobbyScreen(
    onLobbyCreated: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateVenueBookingLobbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showVenuePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.createdLobbyId) {
        uiState.createdLobbyId?.let { onLobbyCreated(it) }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GreenAccent,
        unfocusedBorderColor = DarkBorder,
        focusedLabelColor = GreenAccent,
        unfocusedLabelColor = DarkTextSecondary,
        focusedTextColor = DarkTextPrimary,
        unfocusedTextColor = DarkTextPrimary,
        cursorColor = GreenAccent
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkBg)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Create Venue Lobby", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }

            // ── Content ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Step 1: Venue Selection
                VenueSelectionCard(
                    selectedVenue = uiState.selectedVenue,
                    isLoading = uiState.isLoadingVenues,
                    onPickVenue = { showVenuePicker = true },
                    onClearVenue = viewModel::clearVenue
                )

                // Step 2: Time Slot Selection (only when venue selected)
                if (uiState.selectedVenue != null) {
                    DateSelector(
                        selectedDate = uiState.selectedDate,
                        onDateSelected = viewModel::onDateSelected
                    )

                    TimeSlotGrid(
                        slots = uiState.availableSlots,
                        selectedSlot = uiState.selectedTimeSlot,
                        isLoading = uiState.isLoadingSlots,
                        onSlotSelected = viewModel::selectTimeSlot,
                        venuePricePerHour = uiState.selectedVenue?.pricePerHour ?: 0.0
                    )
                }

                // Step 3: Lobby Details (only when time slot selected)
                if (uiState.selectedTimeSlot != null) {
                    // Title field
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChange,
                        label = { Text("Lobby Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                        singleLine = true
                    )

                    // Payment type selector
                    PaymentTypeSelector(
                        selectedType = uiState.paymentType,
                        onTypeSelected = viewModel::onPaymentTypeChange
                    )

                    // Player count picker
                    PlayerCountPicker(
                        count = uiState.maxPlayers,
                        onIncrease = viewModel::onMaxPlayersIncrease,
                        onDecrease = viewModel::onMaxPlayersDecrease
                    )

                    // Price breakdown
                    if (uiState.totalPrice > 0) {
                        PriceBreakdownCard(
                            totalPrice = uiState.totalPrice,
                            maxPlayers = uiState.maxPlayers,
                            paymentType = uiState.paymentType,
                            pricePerPlayer = uiState.pricePerPlayer
                        )
                    }

                    // Description
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (uiState.isValid && !uiState.isCreating) GreenAccent
                                else Color(0xFF2A3D2B)
                            )
                            .clickable(enabled = uiState.isValid && !uiState.isCreating) {
                                viewModel.createLobby()
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.isCreating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Create Lobby",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // ── Snackbar ─────────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Venue Picker Bottom Sheet
    if (showVenuePicker) {
        VenuePickerSheet(
            venues = uiState.filteredVenues,
            isLoading = uiState.isLoadingVenues,
            searchQuery = uiState.venueSearchQuery,
            onSearchChange = viewModel::onVenueSearchChange,
            onVenueSelected = { venue ->
                viewModel.selectVenue(venue)
                showVenuePicker = false
            },
            onDismiss = { showVenuePicker = false }
        )
    }
}

// ── Venue Selection Card ─────────────────────────────────────────────────────

@Composable
private fun VenueSelectionCard(
    selectedVenue: VenueDto?,
    isLoading: Boolean,
    onPickVenue: () -> Unit,
    onClearVenue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Venue",
                fontSize = 14.sp,
                color = GreenAccent,
                fontWeight = FontWeight.SemiBold
            )
            if (selectedVenue != null) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(DarkBg)
                        .clickable(onClick = onClearVenue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Change venue",
                        tint = DarkTextPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenAccent, modifier = Modifier.size(24.dp))
            }
        } else if (selectedVenue != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedVenue.name,
                        fontSize = 16.sp,
                        color = DarkTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${selectedVenue.address} | ${selectedVenue.sportType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        fontSize = 12.sp,
                        color = DarkTextPrimary.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "${"%.0f".format(selectedVenue.pricePerHour)} MKD/h",
                    fontSize = 14.sp,
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBg)
                    .clickable(onClick = onPickVenue)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Venue", color = DarkTextPrimary, fontSize = 14.sp)
                }
            }
        }
    }
}

// ── Venue Picker Bottom Sheet ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VenuePickerSheet(
    venues: List<VenueDto>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onVenueSelected: (VenueDto) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Select Venue",
                fontSize = 16.sp,
                color = DarkTextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search venues...", color = DarkTextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenAccent,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = DarkTextPrimary,
                    unfocusedTextColor = DarkTextPrimary,
                    cursorColor = GreenAccent
                ),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            } else if (venues.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No venues found", color = DarkTextSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(venues, key = { it.id }) { venue ->
                        VenuePickerItem(venue = venue, onClick = { onVenueSelected(venue) })
                    }
                }
            }
        }
    }
}

@Composable
private fun VenuePickerItem(
    venue: VenueDto,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkBg)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GreenAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SportsTennis,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = venue.name,
                fontSize = 14.sp,
                color = DarkTextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${venue.sportType.name.lowercase().replaceFirstChar { it.uppercase() }} | ${venue.address}",
                fontSize = 12.sp,
                color = DarkTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${"%.0f".format(venue.pricePerHour)} MKD",
                fontSize = 14.sp,
                color = GreenAccent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "per hour",
                fontSize = 10.sp,
                color = DarkTextSecondary
            )
        }
    }
}

// ── Date Selector ────────────────────────────────────────────────────────────

@Composable
private fun DateSelector(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val displayFormat = SimpleDateFormat("EEE, MMM d", Locale.US)
    val dates = remember {
        val cal = Calendar.getInstance()
        (0 until 7).map {
            val date = dateFormat.format(cal.time)
            val display = displayFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, 1)
            date to display
        }
    }

    Column {
        Text(
            text = "Select Date",
            fontSize = 14.sp,
            color = DarkTextPrimary.copy(alpha = 0.85f),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dates.forEach { (date, display) ->
                val isSelected = date == selectedDate
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) GreenAccent.copy(alpha = 0.15f) else DarkSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) GreenAccent else DarkBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onDateSelected(date) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = display,
                        fontSize = 12.sp,
                        color = if (isSelected) GreenAccent else DarkTextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ── Time Slot Grid ───────────────────────────────────────────────────────────

@Composable
private fun TimeSlotGrid(
    slots: List<TimeSlotDto>,
    selectedSlot: TimeSlotDto?,
    isLoading: Boolean,
    onSlotSelected: (TimeSlotDto) -> Unit,
    venuePricePerHour: Double
) {
    Column {
        Text(
            text = "Available Time Slots",
            fontSize = 14.sp,
            color = DarkTextPrimary.copy(alpha = 0.85f),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenAccent, modifier = Modifier.size(24.dp))
                }
            }
            slots.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "No available time slots for this date",
                        fontSize = 12.sp,
                        color = DarkTextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                // 3-column grid of time slots
                val rows = slots.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { rowSlots ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSlots.forEach { slot ->
                                val isSelected = selectedSlot?.id == slot.id
                                val price = slot.priceOverride ?: venuePricePerHour
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) GreenAccent.copy(alpha = 0.15f)
                                            else DarkSurface
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) GreenAccent else DarkBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSlotSelected(slot) }
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = GreenAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                        }
                                        Text(
                                            text = "${slot.startTime.toDisplayTime()} - ${slot.endTime.toDisplayTime()}",
                                            fontSize = 12.sp,
                                            color = if (isSelected) GreenAccent else DarkTextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "${"%.0f".format(price)} MKD",
                                            fontSize = 10.sp,
                                            color = if (isSelected) GreenAccent.copy(alpha = 0.8f) else DarkTextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            // Fill remaining space if less than 3 items in row
                            repeat(3 - rowSlots.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Payment Type Selector ────────────────────────────────────────────────────

@Composable
private fun PaymentTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Payment Type",
            fontSize = 14.sp,
            color = DarkTextPrimary.copy(alpha = 0.85f),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentTypeOption(
                label = "Split Equally",
                description = "Each player pays their share",
                isSelected = selectedType == "split",
                color = GreenAccent,
                onClick = { onTypeSelected("split") },
                modifier = Modifier.weight(1f)
            )
            PaymentTypeOption(
                label = "I'll Pay",
                description = "Creator covers the full cost",
                isSelected = selectedType == "creator_pays",
                color = SportGreen,
                onClick = { onTypeSelected("creator_pays") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        PaymentTypeOption(
            label = "Split to Teams",
            description = "Divide cost by teams — team leader pays",
            isSelected = selectedType == "split_to_teams",
            color = CoralRed,
            onClick = { onTypeSelected("split_to_teams") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PaymentTypeOption(
    label: String,
    description: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) color else DarkBorder
    val bgColor = if (isSelected) color.copy(alpha = 0.1f) else DarkSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (isSelected) color else DarkTextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 10.sp,
                color = if (isSelected) color.copy(alpha = 0.8f) else DarkTextSecondary
            )
        }
    }
}

// ── Player Count Picker ──────────────────────────────────────────────────────

@Composable
private fun PlayerCountPicker(
    count: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Max Players",
                fontSize = 14.sp,
                color = DarkTextPrimary.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Min 2, max 10",
                fontSize = 10.sp,
                color = DarkTextSecondary
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable(enabled = count > 2, onClick = onDecrease),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = if (count > 2) GreenAccent else DarkTextPrimary.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = count.toString(),
                    fontSize = 22.sp,
                    color = DarkTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable(enabled = count < 10, onClick = onIncrease),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = if (count < 10) GreenAccent else DarkTextPrimary.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Price Breakdown Card ─────────────────────────────────────────────────────

@Composable
private fun PriceBreakdownCard(
    totalPrice: Double,
    maxPlayers: Int,
    paymentType: String,
    pricePerPlayer: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GreenAccent.copy(alpha = 0.1f))
            .border(1.dp, GreenAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Total Cost",
                fontSize = 10.sp,
                color = DarkTextPrimary.copy(alpha = 0.6f)
            )
            Text(
                text = "${"%.0f".format(totalPrice)} MKD",
                fontSize = 16.sp,
                color = DarkTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
        when (paymentType) {
            "split" -> {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Per Player ($maxPlayers players)",
                        fontSize = 10.sp,
                        color = DarkTextPrimary.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${"%.0f".format(pricePerPlayer)} MKD",
                        fontSize = 16.sp,
                        color = GreenAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            "split_to_teams" -> {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Per Team (2 default teams)",
                        fontSize = 10.sp,
                        color = DarkTextPrimary.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${"%.0f".format(totalPrice / 2)} MKD",
                        fontSize = 16.sp,
                        color = CoralRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            else -> {
                Text(
                    text = "You pay all",
                    fontSize = 12.sp,
                    color = SportGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CreateVenueBookingLobbyScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Create Venue Lobby", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "1. Select Venue  >  2. Pick Time Slot  >  3. Fill Details",
                color = DarkTextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            PriceBreakdownCard(
                totalPrice = 1800.0,
                maxPlayers = 4,
                paymentType = "split",
                pricePerPlayer = 450.0
            )
        }
    }
}
