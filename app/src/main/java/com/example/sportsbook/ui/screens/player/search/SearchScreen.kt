package com.example.sportsbook.ui.screens.player.search

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.ui.common.toDisplayDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onVenueClick: (Long) -> Unit,
    onCoachClick: (Long) -> Unit,
    onMatchClick: (Long) -> Unit,
    onBack: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search venues, coaches, matches...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isSearching -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                !uiState.hasSearched -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Search for venues, coaches, and matches",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                uiState.venues.isEmpty() && uiState.coaches.isEmpty() && uiState.matches.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No results found for \"${uiState.query}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    SearchResultsList(
                        venues = uiState.venues,
                        coaches = uiState.coaches,
                        matches = uiState.matches,
                        onVenueClick = onVenueClick,
                        onCoachClick = onCoachClick,
                        onMatchClick = onMatchClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    venues: List<Venue>,
    coaches: List<Coach>,
    matches: List<Match>,
    onVenueClick: (Long) -> Unit,
    onCoachClick: (Long) -> Unit,
    onMatchClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (venues.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Venues",
                    icon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
            items(venues) { venue ->
                VenueSearchResultItem(venue = venue, onClick = { onVenueClick(venue.id) })
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        }

        if (coaches.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Coaches",
                    icon = { Icon(Icons.Default.EmojiPeople, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
            items(coaches) { coach ->
                CoachSearchResultItem(coach = coach, onClick = { onCoachClick(coach.id) })
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        }

        if (matches.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Matches",
                    icon = { Icon(Icons.Default.SportsSoccer, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
            items(matches) { match ->
                MatchSearchResultItem(match = match, onClick = { onMatchClick(match.id) })
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SearchSectionHeader(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VenueSearchResultItem(
    venue: Venue,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = venue.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = "${venue.sportType.displayName} • ${venue.address}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${venue.pricePerHour.toInt()} ден/hr",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoachSearchResultItem(
    coach: Coach,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = coach.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = "${coach.sportType.displayName}${if (coach.specialization != null) " • ${coach.specialization}" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${coach.pricePerHour.toInt()} ден/hr",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchSearchResultItem(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = match.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = "${match.sportType.displayName} • ${match.matchDate.toDisplayDate()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${match.spotsLeft} spots left",
                style = MaterialTheme.typography.labelMedium,
                color = if (match.spotsLeft > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    val sampleVenues = listOf(
        Venue(id = 1L, name = "City Basketball Court", address = "123 Main St", pricePerHour = 35.0)
    )
    val sampleCoaches = listOf(
        Coach(id = 1L, name = "Alex Rivera", specialization = "Point Guard Trainer", pricePerHour = 60.0)
    )
    val sampleMatches = listOf(
        Match(id = 1L, title = "Weekend Basketball Pick-up", matchDate = "2026-03-29", startTime = "10:00", endTime = "12:00", maxPlayers = 10, currentPlayers = 7)
    )
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Search") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "basketball",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search venues, coaches, matches...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                SearchResultsList(
                    venues = sampleVenues,
                    coaches = sampleCoaches,
                    matches = sampleMatches,
                    onVenueClick = {},
                    onCoachClick = {},
                    onMatchClick = {}
                )
            }
        }
    }
}
