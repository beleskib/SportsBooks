package com.example.sportsbook.ui.v2.play

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.v2.FriendAvailability
import com.example.sportsbook.domain.model.v2.HomeFeedSnapshot
import com.example.sportsbook.domain.model.v2.PlaySearchResult
import com.example.sportsbook.domain.model.v2.PlaySuggestion
import com.example.sportsbook.domain.model.v2.RebookSuggestion
import com.example.sportsbook.domain.model.v2.UpcomingBooking
import com.example.sportsbook.domain.model.v2.VenueSearchGroup
import com.example.sportsbook.ui.theme.TextSecondary
import com.example.sportsbook.ui.theme.BorderGray
import com.example.sportsbook.ui.theme.LightBg
import com.example.sportsbook.ui.theme.CardWhite
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.SportGreen
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.TextPrimary

// ============================================================
// v2-practical-ux: Play Home Screen
// Mirrors PlayHomePage.tsx in section order:
//   1. Greeting bar (displayName + reliability)
//   2. Search form (from/to dates, sport, skill range)
//   3. Rebook strip
//   4. Suggested play cards
//   5. Friends-available chips
// ============================================================

private val SPORTS = listOf(
    "" to "Any sport",
    "tennis" to "Tennis",
    "paddle" to "Paddle",
    "football" to "Football",
    "basketball" to "Basketball",
    "volleyball" to "Volleyball",
    "badminton" to "Badminton"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayHomeScreen(
    onBack: (() -> Unit)? = null,
    onBrowseVenue: (venueId: Long) -> Unit = {},
    onMatchClick: (matchId: Long) -> Unit = {},
    viewModel: PlayHomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Surface errors via snackbar
    LaunchedEffect(state.rebookError) {
        state.rebookError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearRebookError()
        }
    }
    LaunchedEffect(state.searchError) {
        state.searchError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSearchError()
        }
    }

    Scaffold(
        containerColor = NavBarBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Play",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavBarBg),
                navigationIcon = if (onBack != null) {
                    {
                        androidx.compose.material3.IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    }
                } else ({})
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---- 1. Greeting ----
            if (state.feedLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GoldAccent)
                    }
                }
            } else {
                state.feed?.let { feed ->
                    item { GreetingBar(feed = feed) }
                }
                state.feedError?.let { err ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = err,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Button(
                                    onClick = { viewModel.retryFeed() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4F46E5),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }

            // ---- 2. Search form ----
            item {
                SearchFormCard(
                    fromDate = state.fromDate,
                    toDate = state.toDate,
                    sportType = state.sportType,
                    skillLevelMin = state.skillLevelMin,
                    skillLevelMax = state.skillLevelMax,
                    isSearching = state.searching,
                    onFromDateChanged = viewModel::onFromDateChanged,
                    onToDateChanged = viewModel::onToDateChanged,
                    onSportTypeChanged = viewModel::onSportTypeChanged,
                    onSkillMinChanged = viewModel::onSkillMinChanged,
                    onSkillMaxChanged = viewModel::onSkillMaxChanged,
                    onSearch = viewModel::search
                )
            }

            // ---- Search results (replaces rebook/suggested when hasSearched=true) ----
            if (state.hasSearched) {
                state.searchResult?.let { result ->
                    item { SearchResultsHeader(result = result) }
                    if (result.results.isEmpty()) {
                        item {
                            Text(
                                text = "Nothing found for this window. Try a wider time range or another sport.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CardWhite, RoundedCornerShape(8.dp))
                                    .padding(16.dp)
                            )
                        }
                    } else {
                        // Lobbies/matches shown as individual cards
                        val lobbies = result.lobbyResults
                        if (lobbies.isNotEmpty()) {
                            items(lobbies, key = { "${it.type}-${it.id}" }) { suggestion ->
                                PlaySuggestionCard(
                                    suggestion = suggestion,
                                    onAction = { onMatchClick(suggestion.id) }
                                )
                            }
                        }

                        // Open slots grouped by venue
                        val venueGroups = result.venueGroups
                        if (venueGroups.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Available venues")
                            }
                            items(venueGroups, key = { "venue-${it.venueId}" }) { group ->
                                VenueGroupCard(
                                    group = group,
                                    onBrowseVenue = { onBrowseVenue(group.venueId) }
                                )
                            }
                        }
                    }
                }
            } else {
                // ---- 3. Rebook strip ----
                val recentBookings = state.feed?.recentBookings.orEmpty()
                if (recentBookings.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Rebook in one tap")
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(recentBookings, key = { it.bookingId }) { booking ->
                                RebookCard(
                                    booking = booking,
                                    isRebooking = booking.bookingId in state.rebookingIds,
                                    onRebook = { viewModel.rebook(booking.bookingId) }
                                )
                            }
                        }
                    }
                }

                // ---- 4. Suggested play ----
                val suggested = state.feed?.suggestedPlay.orEmpty()
                if (suggested.isNotEmpty()) {
                    item { SectionHeader(title = "Suggested for you") }
                    items(suggested, key = { "sugg-${it.id}" }) { suggestion ->
                        PlaySuggestionCard(
                            suggestion = suggestion,
                            onAction = {
                                if (suggestion.type == "open_slot" && suggestion.venueId != null) {
                                    onBrowseVenue(suggestion.venueId)
                                } else {
                                    onMatchClick(suggestion.id)
                                }
                            }
                        )
                    }
                }

                // ---- 5. Upcoming bookings ----
                val upcoming = state.feed?.upcoming.orEmpty()
                if (upcoming.isNotEmpty()) {
                    item { SectionHeader(title = "Upcoming this week") }
                    items(upcoming, key = { "upcoming-${it.id}" }) { booking ->
                        UpcomingBookingCard(booking = booking)
                    }
                }

                // ---- 6. Friends available ----
                val friends = state.feed?.friendsAvailable.orEmpty()
                if (friends.isNotEmpty()) {
                    item { SectionHeader(title = "Friends available now") }
                    item { FriendsRow(friends = friends) }
                }
            }
        }
    }
}

// ============================================================
// Sub-composables
// ============================================================

@Composable
private fun GreetingBar(feed: HomeFeedSnapshot) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF4338CA), Color(0xFF7C3AED))
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hey ${feed.displayName ?: "there"}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Find a game, book a court, or invite friends",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE0E7FF)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = GoldAccent
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${(feed.reliabilityScore * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
                Text(
                    text = "reliability · ${feed.totalAttended} games",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFC7D2FE)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchFormCard(
    fromDate: String,
    toDate: String,
    sportType: String,
    skillLevelMin: Int?,
    skillLevelMax: Int?,
    isSearching: Boolean,
    onFromDateChanged: (String) -> Unit,
    onToDateChanged: (String) -> Unit,
    onSportTypeChanged: (String) -> Unit,
    onSkillMinChanged: (Int?) -> Unit,
    onSkillMaxChanged: (Int?) -> Unit,
    onSearch: () -> Unit
) {
    var sportDropdownExpanded by remember { mutableStateOf(false) }
    val selectedSportLabel = SPORTS.firstOrNull { it.first == sportType }?.second ?: "Any sport"
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "I want to play\u2026",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )
            }

            // From / To dates — read-only fields that open date+time picker dialogs
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = fromDate.take(16).replace('T', ' '),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From", color = TextSecondary, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = BorderGray,
                            cursorColor = GoldAccent
                        )
                    )
                    // Transparent overlay captures taps on the whole field area
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showFromPicker = true }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = toDate.take(16).replace('T', ' '),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To", color = TextSecondary, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = BorderGray,
                            cursorColor = GoldAccent
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showToPicker = true }
                    )
                }
            }

            // Sport dropdown
            ExposedDropdownMenuBox(
                expanded = sportDropdownExpanded,
                onExpandedChange = { sportDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedSportLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sport", color = TextSecondary, style = MaterialTheme.typography.labelSmall) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sportDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = GoldAccent
                    )
                )
                ExposedDropdownMenu(
                    expanded = sportDropdownExpanded,
                    onDismissRequest = { sportDropdownExpanded = false },
                    modifier = Modifier.background(LightBg)
                ) {
                    SPORTS.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label, color = TextPrimary, style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                onSportTypeChanged(value)
                                sportDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Skill range
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = skillLevelMin?.toString() ?: "",
                    onValueChange = { v -> onSkillMinChanged(v.toIntOrNull()) },
                    label = { Text("Skill min (1-5)", color = TextSecondary, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = GoldAccent
                    )
                )
                OutlinedTextField(
                    value = skillLevelMax?.toString() ?: "",
                    onValueChange = { v -> onSkillMaxChanged(v.toIntOrNull()) },
                    label = { Text("Skill max (1-5)", color = TextSecondary, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = GoldAccent
                    )
                )
            }

            Button(
                onClick = onSearch,
                enabled = !isSearching,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F46E5),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text("Search")
            }
        }
    }

    if (showFromPicker) {
        DateTimePickerDialog(
            onDismiss = { showFromPicker = false },
            onConfirm = { iso ->
                onFromDateChanged(iso)
                showFromPicker = false
            },
            initialDate = fromDate
        )
    }
    if (showToPicker) {
        DateTimePickerDialog(
            onDismiss = { showToPicker = false },
            onConfirm = { iso ->
                onToDateChanged(iso)
                showToPicker = false
            },
            initialDate = toDate
        )
    }
}

@Composable
private fun SearchResultsHeader(result: PlaySearchResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Available now",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CountChip("${result.lobbies} lobbies")
            CountChip("${result.openSlots} courts")
            CountChip("${result.availablePlayers} players")
        }
    }
}

@Composable
private fun CountChip(label: String) {
    Surface(
        color = LightBg,
        shape = RoundedCornerShape(100.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = TextPrimary
    )
}

@Composable
private fun RebookCard(
    booking: RebookSuggestion,
    isRebooking: Boolean,
    onRebook: () -> Unit
) {
    val target = booking.venueName ?: booking.coachName ?: "Booking"
    Card(
        modifier = Modifier.width(220.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = booking.sportType.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = target,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = "${booking.price.toLong()} \u0434\u0435\u043d · ${booking.lastSlotStart.take(10)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            if (booking.timesBooked > 1) {
                Text(
                    text = "Booked ${booking.timesBooked}\u00d7 here",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF818CF8)
                )
            }
            Spacer(Modifier.height(10.dp))
            FilledTonalButton(
                onClick = onRebook,
                enabled = !isRebooking,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF4F46E5),
                    contentColor = Color.White
                )
            ) {
                if (isRebooking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Rebook", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun PlaySuggestionCard(suggestion: PlaySuggestion, onAction: () -> Unit = {}) {
    val typeLabel = when (suggestion.type) {
        "lobby" -> "Lobby"
        "open_slot" -> "Open court"
        "match" -> "Match"
        else -> suggestion.type
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$typeLabel \u00b7 ${suggestion.sportType.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = suggestion.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
                suggestion.price?.let { price ->
                    Text(
                        text = "${price.toLong()} \u0434\u0435\u043d",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = GoldAccent
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = suggestion.startAt.take(16).replace('T', ' '),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            suggestion.venueName?.let { venue ->
                val distStr = suggestion.distanceKm?.let { " \u00b7 ${"%.1f".format(it)} km" } ?: ""
                Text(
                    text = "$venue$distStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            val hasChips = suggestion.maxPlayers > 1 || (suggestion.skillLevelMin != null && suggestion.skillLevelMax != null)
            if (hasChips) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (suggestion.maxPlayers > 1) {
                        CountChip("${suggestion.currentPlayers}/${suggestion.maxPlayers} players")
                    }
                    if (suggestion.skillLevelMin != null && suggestion.skillLevelMax != null) {
                        CountChip("Skill ${suggestion.skillLevelMin}-${suggestion.skillLevelMax}")
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F46E5),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (suggestion.type == "lobby") "Join lobby" else "Book this slot",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun VenueGroupCard(
    group: VenueSearchGroup,
    onBrowseVenue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.sportType.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = group.venueName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                        maxLines = 1
                    )
                }
                group.priceFrom?.let { price ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "from",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "${price.toLong()} ден",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = GoldAccent
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CountChip("${group.availableSlots} slots available")
                group.distanceKm?.let { dist ->
                    CountChip("${"%.1f".format(dist)} km")
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onBrowseVenue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F46E5),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Browse this venue",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun UpcomingBookingCard(booking: UpcomingBooking) {
    val target = booking.venueName ?: booking.coachName ?: "Booking"
    val statusColor = when (booking.status) {
        "confirmed" -> SportGreen
        "approved" -> Color(0xFF3B82F6)
        else -> GoldAccent // pending
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date badge
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = booking.slotDate.takeLast(2), // day
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = booking.slotDate.substring(5, 7), // month
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = target,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${booking.startTime.take(5)} – ${booking.endTime.take(5)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = booking.status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = statusColor
                    )
                }
                if (booking.totalPrice > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${booking.totalPrice.toLong()} ден",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendsRow(friends: List<FriendAvailability>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        friends.forEach { friend ->
            FriendChip(friend = friend)
        }
    }
}

@Composable
private fun FriendChip(friend: FriendAvailability) {
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(100.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4338CA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (friend.displayName ?: "?").take(1).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Column {
                Text(
                    text = friend.displayName ?: "Player",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )
                Text(
                    text = friend.sportType,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

// ============================================================
// Date + Time picker dialog
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    initialDate: String
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val initialMillis = remember(initialDate) {
        try {
            java.time.LocalDate.parse(initialDate.take(10))
                .atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
    val initialHour = remember(initialDate) {
        try { initialDate.substring(11, 13).toInt() } catch (_: Exception) { 19 }
    }
    val initialMinute = remember(initialDate) {
        try { initialDate.substring(14, 16).toInt() } catch (_: Exception) { 0 }
    }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )

    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { showTimePicker = true }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    val date = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneOffset.UTC)
                        .toLocalDate()
                    val time = java.time.LocalTime.of(timePickerState.hour, timePickerState.minute)
                    val iso = java.time.LocalDateTime.of(date, time)
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    onConfirm(iso)
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        )
    }
}

// ============================================================
// Previews
// ============================================================

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun PlayHomeScreenPreview() {
    SportsBookTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GreetingBar(
                feed = HomeFeedSnapshot(
                    displayName = "Alex",
                    reliabilityScore = 0.87,
                    totalAttended = 24,
                    recentBookings = emptyList(),
                    suggestedPlay = emptyList(),
                    friendsAvailable = emptyList(),
                    upcoming = emptyList()
                )
            )
            SearchFormCard(
                fromDate = "2026-04-25T19:00:00.000Z",
                toDate = "2026-04-26T23:00:00.000Z",
                sportType = "",
                skillLevelMin = null,
                skillLevelMax = null,
                isSearching = false,
                onFromDateChanged = {},
                onToDateChanged = {},
                onSportTypeChanged = {},
                onSkillMinChanged = {},
                onSkillMaxChanged = {},
                onSearch = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun PlaySuggestionCardPreview() {
    SportsBookTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PlaySuggestionCard(
                suggestion = PlaySuggestion(
                    type = "lobby",
                    id = 1L,
                    title = "Evening Tennis at City Court",
                    sportType = "tennis",
                    startAt = "2026-04-25T19:00:00.000Z",
                    venueName = "City Tennis Club",
                    distanceKm = 2.4,
                    currentPlayers = 2,
                    maxPlayers = 4,
                    skillLevelMin = 2,
                    skillLevelMax = 4,
                    price = 15.0
                )
            )
        }
    }
}
