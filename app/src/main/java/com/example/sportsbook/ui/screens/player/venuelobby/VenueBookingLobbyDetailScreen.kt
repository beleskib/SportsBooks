package com.example.sportsbook.ui.screens.player.venuelobby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import coil.compose.AsyncImage
import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.AddTeamRequestDto
import com.example.sportsbook.data.remote.dto.UpdateVenueBookingLobbyRequestDto
import com.example.sportsbook.data.remote.dto.VenueBookingLobbyDto
import com.example.sportsbook.data.remote.dto.VenueBookingLobbyMemberDto
import com.example.sportsbook.data.remote.dto.VenueBookingLobbyTeamDto
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.common.toDisplayTime
import com.example.sportsbook.ui.navigation.Route
import com.example.sportsbook.ui.theme.CoralRed
import com.example.sportsbook.ui.theme.LightBg
import com.example.sportsbook.ui.theme.CardWhite
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.SportGreen
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.TextPrimary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class VenueBookingLobbyDetailUiState(
    val lobby: VenueBookingLobbyDto? = null,
    val isLoading: Boolean = true,
    val isActioning: Boolean = false,
    val currentUserId: Long? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val paymentProcessing: Boolean = false
) {
    val isCreator: Boolean
        get() = currentUserId != null && lobby?.creatorId == currentUserId

    val isMember: Boolean
        get() = currentUserId != null && lobby?.members?.any { it.userId == currentUserId } == true

    val currentMember: VenueBookingLobbyMemberDto?
        get() = lobby?.members?.find { it.userId == currentUserId }

    val canSubmitBooking: Boolean
        get() = isCreator && (lobby?.currentPlayers ?: 0) >= 2 &&
            (lobby?.status == "open" || lobby?.status == "full")

    val canJoin: Boolean
        get() = !isCreator && !isMember &&
            lobby?.status == "open" &&
            (lobby.currentPlayers) < lobby.maxPlayers

    val canLeave: Boolean
        get() = isMember && !isCreator && lobby?.status == "open"

    val canPay: Boolean
        get() {
            val l = lobby ?: return false
            val status = l.status
            if (status != "booking_approved" && status != "payment_in_progress") return false
            return when (l.paymentType) {
                "creator_pays" -> isCreator
                "split_to_teams" -> {
                    // Only team leaders can pay
                    l.teams.any { it.leaderId == currentUserId } && currentMember?.paidAt == null
                }
                else -> isMember && currentMember?.paidAt == null
            }
        }

    val currentTeam: VenueBookingLobbyTeamDto?
        get() = lobby?.teams?.find { team ->
            team.members.any { it.userId == currentUserId }
        }

    val isTeamLeader: Boolean
        get() = lobby?.teams?.any { it.leaderId == currentUserId } == true

    val hasTeams: Boolean
        get() = (lobby?.teams?.size ?: 0) > 0

    val canEdit: Boolean
        get() = isCreator && (lobby?.status == "open" || lobby?.status == "full")
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class VenueBookingLobbyDetailViewModel @Inject constructor(
    private val apiService: ApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.VenueBookingLobbyDetail>()
    val lobbyId: Long = route.lobbyId

    private val _uiState = MutableStateFlow(VenueBookingLobbyDetailUiState())
    val uiState: StateFlow<VenueBookingLobbyDetailUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadLobby()
        startPolling()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            runCatching { apiService.getProfile() }
                .onSuccess { response ->
                    _uiState.update { it.copy(currentUserId = response.data.id) }
                }
        }
    }

    fun loadLobby() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { apiService.getVenueBookingLobbyById(lobbyId) }
                .fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(isLoading = false, lobby = response.data) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load lobby") }
                    }
                )
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                delay(5_000)
                val status = _uiState.value.lobby?.status
                // Stop polling when confirmed or cancelled
                if (status == "confirmed" || status == "cancelled") break
                refreshLobby()
            }
        }
    }

    private suspend fun refreshLobby() {
        runCatching { apiService.getVenueBookingLobbyById(lobbyId) }
            .onSuccess { response ->
                _uiState.update { it.copy(lobby = response.data) }
            }
    }

    fun joinLobby() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true, error = null) }
            runCatching { apiService.joinVenueBookingLobby(lobbyId) }
                .fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(isActioning = false, lobby = response.data, successMessage = "Joined lobby!") }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isActioning = false, error = e.message ?: "Failed to join") }
                    }
                )
        }
    }

    fun leaveLobby() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true, error = null) }
            runCatching { apiService.leaveVenueBookingLobby(lobbyId) }
                .fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(isActioning = false, lobby = response.data, successMessage = "Left lobby") }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isActioning = false, error = e.message ?: "Failed to leave") }
                    }
                )
        }
    }

    fun createBooking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true, error = null) }
            runCatching { apiService.createBookingFromLobby(lobbyId) }
                .fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(isActioning = false, lobby = response.data, successMessage = "Booking submitted!") }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isActioning = false, error = e.message ?: "Failed to create booking") }
                    }
                )
        }
    }

    fun cancelLobby() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true, error = null) }
            runCatching { apiService.cancelVenueBookingLobby(lobbyId) }
                .fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(isActioning = false, lobby = response.data, successMessage = "Lobby cancelled") }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isActioning = false, error = e.message ?: "Failed to cancel") }
                    }
                )
        }
    }

    fun pay() {
        viewModelScope.launch {
            _uiState.update { it.copy(paymentProcessing = true, error = null) }
            runCatching { apiService.createLobbyPaymentIntent(lobbyId) }
                .fold(
                    onSuccess = { response ->
                        val intent = response.data
                        if (intent.clientSecret.startsWith("dev_secret_")) {
                            // Dev mode: skip Stripe, confirm directly
                            confirmPayment()
                        } else {
                            // In production, launch Stripe PaymentSheet here.
                            // For now fall through to confirm (dev-only path).
                            confirmPayment()
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(paymentProcessing = false, error = e.message ?: "Failed to initiate payment") }
                    }
                )
        }
    }

    private fun confirmPayment() {
        viewModelScope.launch {
            runCatching { apiService.confirmLobbyPayment(lobbyId) }
                .fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(paymentProcessing = false, lobby = response.data, successMessage = "Payment confirmed!") }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(paymentProcessing = false, error = e.message ?: "Payment confirmation failed") }
                    }
                )
        }
    }

    fun joinTeam(teamId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true, error = null) }
            runCatching { apiService.joinLobbyTeam(lobbyId, teamId) }
                .fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(isActioning = false, lobby = response.data, successMessage = "Joined team!") }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isActioning = false, error = e.message ?: "Failed to join team") }
                    }
                )
        }
    }

    fun leaveTeam() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true, error = null) }
            runCatching { apiService.leaveLobbyTeam(lobbyId) }
                .fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(isActioning = false, lobby = response.data, successMessage = "Left team") }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isActioning = false, error = e.message ?: "Failed to leave team") }
                    }
                )
        }
    }

    fun updateLobby(title: String?, description: String?, maxPlayers: Int?, paymentType: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true, error = null) }
            runCatching {
                apiService.updateVenueBookingLobby(
                    lobbyId,
                    UpdateVenueBookingLobbyRequestDto(
                        title = title,
                        description = description,
                        maxPlayers = maxPlayers,
                        paymentType = paymentType
                    )
                )
            }.fold(
                onSuccess = { response ->
                    _uiState.update { it.copy(isActioning = false, lobby = response.data, successMessage = "Lobby updated!") }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isActioning = false, error = e.message ?: "Failed to update lobby") }
                }
            )
        }
    }

    fun addTeam(teamName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true, error = null) }
            runCatching { apiService.addLobbyTeam(lobbyId, AddTeamRequestDto(teamName = teamName)) }
                .fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(isActioning = false, lobby = response.data, successMessage = "Team added!") }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isActioning = false, error = e.message ?: "Failed to add team") }
                    }
                )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenueBookingLobbyDetailScreen(
    onBack: () -> Unit,
    viewModel: VenueBookingLobbyDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    var showEditDialog by remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showEditDialog && uiState.lobby != null) {
        EditLobbyDialog(
            lobby = uiState.lobby!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { title, description, maxPlayers, paymentType ->
                viewModel.updateLobby(title, description, maxPlayers, paymentType)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        containerColor = NavBarBg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.lobby?.title ?: "Lobby",
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (uiState.canEdit) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Lobby",
                                tint = GoldAccent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldAccent)
                }
            }

            uiState.lobby == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lobby not found",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                val lobby = uiState.lobby!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Header / info section
                    item {
                        LobbyDetailHeader(lobby = lobby)
                    }

                    // Status-dependent content
                    item {
                        LobbyStatusSection(
                            uiState = uiState,
                            onJoin = viewModel::joinLobby,
                            onLeave = viewModel::leaveLobby,
                            onSubmitBooking = viewModel::createBooking,
                            onCancel = viewModel::cancelLobby,
                            onPay = viewModel::pay
                        )
                    }

                    // Teams grid (shown when lobby has teams)
                    if (uiState.hasTeams) {
                        item {
                            TeamsSection(
                                uiState = uiState,
                                onJoinTeam = viewModel::joinTeam,
                                onLeaveTeam = viewModel::leaveTeam,
                                onAddTeam = viewModel::addTeam
                            )
                        }
                    }

                    // Members list
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Members (${lobby.currentPlayers}/${lobby.maxPlayers})",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    if (lobby.members.isEmpty()) {
                        item {
                            Text(
                                text = "No members yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        items(lobby.members, key = { it.id }) { member ->
                            MemberRow(
                                member = member,
                                showPaymentStatus = lobby.status == "booking_approved" ||
                                    lobby.status == "payment_in_progress" ||
                                    lobby.status == "confirmed"
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun LobbyDetailHeader(lobby: VenueBookingLobbyDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(16.dp)
    ) {
        // Title + badge row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = lobby.title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            PaymentTypeBadge(paymentType = lobby.paymentType)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Venue
        if (!lobby.venueName.isNullOrBlank()) {
            DetailInfoRow(
                icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp)) },
                text = lobby.venueName
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Date / time
        val dateText = buildString {
            lobby.slotDate?.let { append(it.toDisplayDate()) }
            if (!lobby.startTime.isNullOrBlank()) {
                if (isNotEmpty()) append("  ")
                append(lobby.startTime.toDisplayTime())
                if (!lobby.endTime.isNullOrBlank()) append(" – ${lobby.endTime.toDisplayTime()}")
            }
        }
        if (dateText.isNotBlank()) {
            DetailInfoRow(
                icon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp)) },
                text = dateText
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Price
        val priceDetail = when (lobby.paymentType) {
            "split" -> "${"%.0f".format(lobby.pricePerPlayer)} ${lobby.currency}/player"
            "split_to_teams" -> {
                val teamShare = if (lobby.teams.isNotEmpty()) lobby.totalPrice / lobby.teams.size else lobby.totalPrice
                "${"%.0f".format(teamShare)} ${lobby.currency}/team (${lobby.teams.size} teams)"
            }
            else -> "Creator pays all"
        }
        DetailInfoRow(
            icon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp)) },
            text = "Total: ${"%.0f".format(lobby.totalPrice)} ${lobby.currency}  |  $priceDetail"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        val progress = if (lobby.maxPlayers > 0) lobby.currentPlayers.toFloat() / lobby.maxPlayers else 0f
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${lobby.currentPlayers}/${lobby.maxPlayers} players",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }
            LobbyStatusChip(status = lobby.status)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = GoldAccent,
            trackColor = LightBg
        )

        // Description
        if (!lobby.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lobby.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun DetailInfoRow(icon: @Composable () -> Unit, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun LobbyStatusChip(status: String) {
    val (label, color) = when (status) {
        "open" -> "Open" to SportGreen
        "full" -> "Full" to GoldAccent
        "booking_pending" -> "Pending Approval" to GoldAccent
        "booking_approved" -> "Approved" to SportGreen
        "payment_in_progress" -> "Payment Phase" to GoldAccent
        "confirmed" -> "Confirmed" to SportGreen
        "cancelled" -> "Cancelled" to CoralRed
        else -> status.replaceFirstChar { it.uppercase() } to TextPrimary.copy(alpha = 0.5f)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Status Section ─────────────────────────────────────────────────────────────

@Composable
private fun LobbyStatusSection(
    uiState: VenueBookingLobbyDetailUiState,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onSubmitBooking: () -> Unit,
    onCancel: () -> Unit,
    onPay: () -> Unit
) {
    val lobby = uiState.lobby ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when (lobby.status) {
            "open", "full" -> {
                OpenLobbyActions(
                    uiState = uiState,
                    onJoin = onJoin,
                    onLeave = onLeave,
                    onSubmitBooking = onSubmitBooking,
                    onCancel = onCancel
                )
            }

            "booking_pending" -> {
                PendingApprovalSection()
            }

            "booking_approved", "payment_in_progress" -> {
                PaymentSection(
                    uiState = uiState,
                    onPay = onPay
                )
            }

            "confirmed" -> {
                ConfirmedSection()
            }

            "cancelled" -> {
                Text(
                    text = "This lobby has been cancelled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoralRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (uiState.isActioning || uiState.paymentProcessing) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldAccent, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun OpenLobbyActions(
    uiState: VenueBookingLobbyDetailUiState,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onSubmitBooking: () -> Unit,
    onCancel: () -> Unit
) {
    if (uiState.canJoin) {
        Button(
            onClick = onJoin,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isActioning,
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp), tint = NavBarBg)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Join Lobby", color = NavBarBg, fontWeight = FontWeight.SemiBold)
        }
    }

    if (uiState.canLeave) {
        OutlinedButton(
            onClick = onLeave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isActioning,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Leave Lobby", fontWeight = FontWeight.SemiBold)
        }
    }

    if (uiState.isCreator) {
        if (uiState.canSubmitBooking) {
            Button(
                onClick = onSubmitBooking,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isActioning,
                colors = ButtonDefaults.buttonColors(containerColor = SportGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = NavBarBg)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Submit Booking", color = NavBarBg, fontWeight = FontWeight.SemiBold)
            }
        } else if (uiState.lobby?.status == "open") {
            Text(
                text = "Need at least 2 players to submit booking",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isActioning,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cancel Lobby", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PendingApprovalSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = GoldAccent,
            modifier = Modifier.size(40.dp),
            strokeWidth = 3.dp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Icon(
            imageVector = Icons.Default.HourglassTop,
            contentDescription = null,
            tint = GoldAccent,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Waiting for venue approval...",
            style = MaterialTheme.typography.titleSmall,
            color = GoldAccent,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "The venue partner will review and approve your booking request.",
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PaymentSection(
    uiState: VenueBookingLobbyDetailUiState,
    onPay: () -> Unit
) {
    val lobby = uiState.lobby ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SportGreen.copy(alpha = 0.08f))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SportGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Booking Approved!",
                style = MaterialTheme.typography.titleSmall,
                color = SportGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (lobby.paymentType) {
                "split" -> "Complete your payment to confirm your spot."
                "split_to_teams" -> "Team leaders pay for their team's share."
                else -> "The creator needs to pay the full amount to confirm the booking."
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary.copy(alpha = 0.7f)
        )
    }

    if (uiState.canPay) {
        val payLabel = when (lobby.paymentType) {
            "creator_pays" ->
                "Pay Full Amount (${"%.0f".format(lobby.totalPrice)} ${lobby.currency})"
            "split_to_teams" -> {
                val teamShare = if (lobby.teams.isNotEmpty()) lobby.totalPrice / lobby.teams.size else lobby.totalPrice
                "Pay Team Share (${"%.0f".format(teamShare)} ${lobby.currency})"
            }
            else -> {
                val share = uiState.currentMember?.shareAmount ?: lobby.pricePerPlayer
                "Pay Your Share (${"%.0f".format(share)} ${lobby.currency})"
            }
        }

        Button(
            onClick = onPay,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.paymentProcessing,
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (uiState.paymentProcessing) {
                CircularProgressIndicator(color = NavBarBg, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(18.dp), tint = NavBarBg)
                Spacer(modifier = Modifier.width(6.dp))
                Text(payLabel, color = NavBarBg, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ConfirmedSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SportGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SportGreen,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "All payments received!",
            style = MaterialTheme.typography.titleMedium,
            color = SportGreen,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Booking confirmed. See you at the venue!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

// ── Teams Section ─────────────────────────────────────────────────────────────

@Composable
private fun TeamsSection(
    uiState: VenueBookingLobbyDetailUiState,
    onJoinTeam: (Long) -> Unit,
    onLeaveTeam: () -> Unit,
    onAddTeam: (String) -> Unit
) {
    val lobby = uiState.lobby ?: return
    val teams = lobby.teams

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Teams (${teams.size})",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )

            // Add team button (only for lobby creator in open/full status)
            if (uiState.isCreator && (lobby.status == "open" || lobby.status == "full")) {
                var showAddTeamDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
                Button(
                    onClick = { showAddTeamDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Team", color = GoldAccent, style = MaterialTheme.typography.labelSmall)
                }

                if (showAddTeamDialog) {
                    AddTeamDialog(
                        nextTeamNumber = teams.size + 1,
                        onDismiss = { showAddTeamDialog = false },
                        onConfirm = { name ->
                            onAddTeam(name)
                            showAddTeamDialog = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2-column grid of team cards
        val rows = teams.chunked(2)
        rows.forEach { rowTeams ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTeams.forEach { team ->
                    TeamCard(
                        team = team,
                        uiState = uiState,
                        onJoinTeam = { onJoinTeam(team.id) },
                        onLeaveTeam = onLeaveTeam,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if odd number of teams
                if (rowTeams.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TeamCard(
    team: VenueBookingLobbyTeamDto,
    uiState: VenueBookingLobbyDetailUiState,
    onJoinTeam: () -> Unit,
    onLeaveTeam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMyTeam = uiState.currentTeam?.id == team.id
    val borderColor = if (isMyTeam) GoldAccent else TextPrimary.copy(alpha = 0.15f)
    val bgColor = if (isMyTeam) GoldAccent.copy(alpha = 0.08f) else CardWhite

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                width = if (isMyTeam) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        // Team header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = team.teamName,
                style = MaterialTheme.typography.labelLarge,
                color = if (isMyTeam) GoldAccent else TextPrimary,
                fontWeight = FontWeight.Bold
            )
            if (isMyTeam) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(GoldAccent.copy(alpha = 0.2f))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "YOUR TEAM",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(0.dp)
                    )
                }
            }
        }

        // Team share amount
        if (team.shareAmount != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${"%.0f".format(team.shareAmount)} MKD",
                style = MaterialTheme.typography.labelSmall,
                color = GoldAccent.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Team members list
        if (team.members.isEmpty()) {
            Text(
                text = "No members yet",
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary.copy(alpha = 0.3f)
            )
        } else {
            team.members.forEach { member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small avatar
                    if (!member.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = member.photoUrl,
                            contentDescription = member.displayName,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(LightBg),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(LightBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextPrimary.copy(alpha = 0.4f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = member.displayName ?: "Player",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                    // Leader badge
                    if (member.userId == team.leaderId) {
                        Text(
                            text = "Leader",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Join/Leave team button
        val lobby = uiState.lobby
        val canInteract = lobby?.status == "open" || lobby?.status == "full"
        if (canInteract && uiState.isMember) {
            if (isMyTeam) {
                OutlinedButton(
                    onClick = onLeaveTeam,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isActioning,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Leave", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            } else if (uiState.currentTeam == null) {
                Button(
                    onClick = onJoinTeam,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isActioning,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Join", color = NavBarBg, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AddTeamDialog(
    nextTeamNumber: Int,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var teamName by remember { androidx.compose.runtime.mutableStateOf("Team $nextTeamNumber") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(teamName.trim()) },
                enabled = teamName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                Text("Add", color = NavBarBg)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextPrimary)
            }
        },
        title = { Text("Add Team", color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                label = { Text("Team Name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = TextPrimary.copy(alpha = 0.3f),
                    focusedLabelColor = GoldAccent,
                    unfocusedLabelColor = TextPrimary.copy(alpha = 0.6f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = GoldAccent
                )
            )
        },
        containerColor = CardWhite
    )
}

// ── Edit Lobby Dialog ─────────────────────────────────────────────────────────

@Composable
private fun EditLobbyDialog(
    lobby: VenueBookingLobbyDto,
    onDismiss: () -> Unit,
    onConfirm: (title: String?, description: String?, maxPlayers: Int?, paymentType: String?) -> Unit
) {
    var title by remember { androidx.compose.runtime.mutableStateOf(lobby.title) }
    var description by remember { androidx.compose.runtime.mutableStateOf(lobby.description ?: "") }
    var maxPlayers by remember { androidx.compose.runtime.mutableIntStateOf(lobby.maxPlayers) }
    var paymentType by remember { androidx.compose.runtime.mutableStateOf(lobby.paymentType) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldAccent,
        unfocusedBorderColor = TextPrimary.copy(alpha = 0.3f),
        focusedLabelColor = GoldAccent,
        unfocusedLabelColor = TextPrimary.copy(alpha = 0.6f),
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = GoldAccent
    )

    val hasChanges = title != lobby.title ||
        description != (lobby.description ?: "") ||
        maxPlayers != lobby.maxPlayers ||
        paymentType != lobby.paymentType

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        if (title != lobby.title) title.trim() else null,
                        if (description != (lobby.description ?: "")) description.trim() else null,
                        if (maxPlayers != lobby.maxPlayers) maxPlayers else null,
                        if (paymentType != lobby.paymentType) paymentType else null
                    )
                },
                enabled = hasChanges && title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                Text("Save", color = NavBarBg)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextPrimary)
            }
        },
        title = { Text("Edit Lobby", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )

                // Payment type selector
                Text(
                    text = "Payment Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EditPaymentChip(
                        label = "Split",
                        isSelected = paymentType == "split",
                        color = GoldAccent,
                        onClick = { paymentType = "split" },
                        modifier = Modifier.weight(1f)
                    )
                    EditPaymentChip(
                        label = "Full",
                        isSelected = paymentType == "creator_pays",
                        color = SportGreen,
                        onClick = { paymentType = "creator_pays" },
                        modifier = Modifier.weight(1f)
                    )
                    EditPaymentChip(
                        label = "Teams",
                        isSelected = paymentType == "split_to_teams",
                        color = CoralRed,
                        onClick = { paymentType = "split_to_teams" },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Max players picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Max Players",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "Current: ${lobby.currentPlayers}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary.copy(alpha = 0.5f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { maxPlayers = (maxPlayers - 1).coerceAtLeast(lobby.currentPlayers.coerceAtLeast(2)) },
                            enabled = maxPlayers > lobby.currentPlayers.coerceAtLeast(2)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = if (maxPlayers > lobby.currentPlayers.coerceAtLeast(2)) GoldAccent else TextPrimary.copy(alpha = 0.3f)
                            )
                        }
                        Text(
                            text = maxPlayers.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { maxPlayers = (maxPlayers + 1).coerceAtMost(10) },
                            enabled = maxPlayers < 10
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = if (maxPlayers < 10) GoldAccent else TextPrimary.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        },
        containerColor = CardWhite
    )
}

@Composable
private fun EditPaymentChip(
    label: String,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) color else TextPrimary.copy(alpha = 0.2f)
    val bgColor = if (isSelected) color.copy(alpha = 0.15f) else LightBg

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) color else TextPrimary.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ── Member Row ────────────────────────────────────────────────────────────────

@Composable
private fun MemberRow(
    member: VenueBookingLobbyMemberDto,
    showPaymentStatus: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        if (!member.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = member.photoUrl,
                contentDescription = member.displayName,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(LightBg),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(LightBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.displayName ?: "Player #${member.userId}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            if (showPaymentStatus && member.shareAmount != null) {
                Text(
                    text = "${"%.0f".format(member.shareAmount)} MKD",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary.copy(alpha = 0.5f)
                )
            }
        }

        if (showPaymentStatus) {
            if (member.paidAt != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Paid",
                        tint = SportGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = SportGreen
                    )
                }
            } else {
                Text(
                    text = "Pending",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun VenueBookingLobbyDetailScreenPreview() {
    val lobby = VenueBookingLobbyDto(
        id = 1,
        creatorId = 42,
        creatorName = "Stefan K.",
        timeSlotId = 10,
        venueId = 5,
        venueName = "City Sports Arena",
        title = "Friday Basketball Squad",
        paymentType = "split",
        maxPlayers = 5,
        currentPlayers = 3,
        totalPrice = 2500.0,
        pricePerPlayer = 500.0,
        currency = "MKD",
        status = "open",
        slotDate = "2026-03-28",
        startTime = "18:00",
        endTime = "19:00",
        sportType = "basketball",
        description = "Casual 3v3, all skill levels welcome!",
        members = listOf(
            VenueBookingLobbyMemberDto(id = 1, lobbyId = 1, userId = 42, displayName = "Stefan K.", status = "joined"),
            VenueBookingLobbyMemberDto(id = 2, lobbyId = 1, userId = 7, displayName = "Ana M.", status = "joined"),
            VenueBookingLobbyMemberDto(id = 3, lobbyId = 1, userId = 15, displayName = "Boris D.", status = "joined")
        )
    )
    Scaffold(
        containerColor = NavBarBg,
        topBar = {
            TopAppBar(
                title = { Text(lobby.title, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { LobbyDetailHeader(lobby = lobby) }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("Members", style = MaterialTheme.typography.titleSmall, color = TextPrimary.copy(alpha = 0.8f))
                }
            }
            items(lobby.members) { member ->
                MemberRow(member = member, showPaymentStatus = false)
            }
        }
    }
}
