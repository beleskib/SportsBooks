package com.example.sportsbook.ui.screens.player.venuelobby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.sportsbook.data.remote.dto.VenueBookingLobbyDto
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.common.toDisplayTime
import com.example.sportsbook.ui.navigation.Route
import com.example.sportsbook.ui.theme.CoralRed
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
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
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class BrowseLobbiesUiState(
    val lobbies: List<VenueBookingLobbyDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val joinedLobbyId: Long? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class BrowseVenueLobbiesViewModel @Inject constructor(
    private val apiService: ApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.BrowseVenueLobbies>()
    private val venueId: Long? = route.venueId

    private val _uiState = MutableStateFlow(BrowseLobbiesUiState())
    val uiState: StateFlow<BrowseLobbiesUiState> = _uiState.asStateFlow()

    init {
        loadLobbies()
    }

    fun loadLobbies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                apiService.getOpenVenueBookingLobbies(venueId = venueId)
            }.fold(
                onSuccess = { response ->
                    _uiState.update { it.copy(isLoading = false, lobbies = response.data) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load lobbies") }
                }
            )
        }
    }

    fun joinLobby(lobbyId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            runCatching {
                apiService.joinVenueBookingLobby(lobbyId)
            }.fold(
                onSuccess = {
                    _uiState.update { it.copy(joinedLobbyId = lobbyId) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to join lobby") }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun BrowseVenueLobbiesScreen(
    onLobbyClick: (Long) -> Unit,
    onCreateLobby: () -> Unit,
    onBack: () -> Unit,
    viewModel: BrowseVenueLobbiesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.joinedLobbyId) {
        uiState.joinedLobbyId?.let { onLobbyClick(it) }
    }

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
                Text("Open Venue Lobbies", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }

            // ── Content ──────────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenAccent)
                    }
                }

                uiState.lobbies.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = DarkTextPrimary.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No open lobbies yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarkTextPrimary.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Be the first to create one!",
                                fontSize = 14.sp,
                                color = DarkTextPrimary.copy(alpha = 0.35f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.lobbies, key = { it.id }) { lobby ->
                            VenueBookingLobbyCard(
                                lobby = lobby,
                                onCardClick = { onLobbyClick(lobby.id) },
                                onJoinClick = { viewModel.joinLobby(lobby.id) }
                            )
                        }
                    }
                }
            }
        }

        // ── FAB ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(GreenAccent)
                .clickable(onClick = onCreateLobby),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Lobby", tint = Color.White)
        }

        // ── Snackbar ─────────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ── Lobby Card ────────────────────────────────────────────────────────────────

@Composable
internal fun VenueBookingLobbyCard(
    lobby: VenueBookingLobbyDto,
    onCardClick: () -> Unit,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onCardClick)
            .padding(16.dp)
    ) {
        // Header row: title + payment badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = lobby.title,
                fontSize = 16.sp,
                color = DarkTextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            PaymentTypeBadge(paymentType = lobby.paymentType)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Venue name
        if (!lobby.venueName.isNullOrBlank()) {
            LobbyInfoRow(
                icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(14.dp)) },
                text = lobby.venueName
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Sport type
        if (!lobby.sportType.isNullOrBlank()) {
            LobbyInfoRow(
                icon = { Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(14.dp)) },
                text = lobby.sportType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Date & time
        val dateText = buildString {
            lobby.slotDate?.let { append(it.toDisplayDate()) }
            if (!lobby.startTime.isNullOrBlank()) {
                if (isNotEmpty()) append("  ")
                append(lobby.startTime.toDisplayTime())
                if (!lobby.endTime.isNullOrBlank()) append(" – ${lobby.endTime.toDisplayTime()}")
            }
        }
        if (dateText.isNotBlank()) {
            LobbyInfoRow(
                icon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(14.dp)) },
                text = dateText
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Creator
        if (!lobby.creatorName.isNullOrBlank()) {
            LobbyInfoRow(
                icon = { Icon(Icons.Default.Person, contentDescription = null, tint = DarkTextPrimary.copy(alpha = 0.6f), modifier = Modifier.size(14.dp)) },
                text = "by ${lobby.creatorName}"
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Players progress
        val progress = if (lobby.maxPlayers > 0) lobby.currentPlayers.toFloat() / lobby.maxPlayers else 0f
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${lobby.currentPlayers}/${lobby.maxPlayers} players",
                    fontSize = 12.sp,
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold
                )
            }
            // Price info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = DarkTextPrimary.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${"%.0f".format(lobby.pricePerPlayer)} ${lobby.currency}/player",
                    fontSize = 12.sp,
                    color = DarkTextPrimary.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DarkBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(GreenAccent)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Join button – only show for open lobbies that are not full
        if (lobby.status == "open" && lobby.currentPlayers < lobby.maxPlayers) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onJoinClick)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Join Lobby",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        } else if (lobby.currentPlayers >= lobby.maxPlayers) {
            Text(
                text = "Lobby Full",
                fontSize = 12.sp,
                color = CoralRed,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LobbyInfoRow(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = DarkTextPrimary.copy(alpha = 0.75f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun PaymentTypeBadge(paymentType: String, modifier: Modifier = Modifier) {
    val (label, bgColor) = when (paymentType) {
        "creator_pays" -> "Creator Pays" to SportGreen.copy(alpha = 0.2f)
        "split_to_teams" -> "Team Split" to CoralRed.copy(alpha = 0.2f)
        else -> "Split" to GreenAccent.copy(alpha = 0.2f)
    }
    val textColor = when (paymentType) {
        "creator_pays" -> SportGreen
        "split_to_teams" -> CoralRed
        else -> GreenAccent
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun BrowseVenueLobbiesScreenPreview() {
    val sampleLobbies = listOf(
        VenueBookingLobbyDto(
            id = 1,
            creatorId = 42,
            creatorName = "Stefan K.",
            timeSlotId = 10,
            venueId = 5,
            venueName = "City Sports Arena",
            title = "Friday Basketball",
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
            sportType = "basketball"
        ),
        VenueBookingLobbyDto(
            id = 2,
            creatorId = 7,
            creatorName = "Ana M.",
            timeSlotId = 11,
            venueId = 6,
            venueName = "Tennis Club Skopje",
            title = "Weekend Tennis",
            paymentType = "creator_pays",
            maxPlayers = 2,
            currentPlayers = 1,
            totalPrice = 1200.0,
            pricePerPlayer = 1200.0,
            currency = "MKD",
            status = "open",
            slotDate = "2026-03-29",
            startTime = "10:00",
            endTime = "11:00",
            sportType = "tennis"
        )
    )
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
            Text("Open Venue Lobbies", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleLobbies) { lobby ->
                VenueBookingLobbyCard(
                    lobby = lobby,
                    onCardClick = {},
                    onJoinClick = {}
                )
            }
        }
    }
}
