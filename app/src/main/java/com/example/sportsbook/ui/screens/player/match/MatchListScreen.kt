package com.example.sportsbook.ui.screens.player.match

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.ui.screens.player.match.components.MatchCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchListScreen(
    onMatchClick: (Long) -> Unit,
    onCreateMatch: () -> Unit,
    onCreateParty: () -> Unit = {},
    onShowMap: () -> Unit = {},
    onBrowseAvailablePlayers: () -> Unit = {},
    viewModel: MatchListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find a Match") },
                actions = {
                    IconButton(onClick = onBrowseAvailablePlayers) {
                        Icon(Icons.Default.PersonSearch, contentDescription = "Available Players")
                    }
                    IconButton(onClick = onCreateParty) {
                        Icon(Icons.Default.Groups, contentDescription = "Create Party")
                    }
                    IconButton(onClick = onShowMap) {
                        Icon(Icons.Default.Map, contentDescription = "Map view")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateMatch) {
                Icon(Icons.Default.Add, contentDescription = "Create Match")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Toggle: All vs My Matches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !uiState.showMyMatchesOnly,
                    onClick = { if (uiState.showMyMatchesOnly) viewModel.toggleMyMatches() },
                    label = { Text("Open Matches") }
                )
                FilterChip(
                    selected = uiState.showMyMatchesOnly,
                    onClick = { if (!uiState.showMyMatchesOnly) viewModel.toggleMyMatches() },
                    label = { Text("My Matches") }
                )
            }

            // Sport filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedSport == null,
                        onClick = { viewModel.selectSport(null) },
                        label = { Text("All") }
                    )
                }
                items(SportType.entries.toList()) { sport ->
                    FilterChip(
                        selected = uiState.selectedSport == sport,
                        onClick = { viewModel.selectSport(sport) },
                        label = { Text(sport.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                uiState.displayedMatches.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No matches found",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Create one or change your filters!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        state = pullToRefreshState
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.displayedMatches, key = { it.id }) { match ->
                                MatchCard(
                                    match = match,
                                    onClick = { onMatchClick(match.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun MatchListScreenPreview() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find a Match") },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.PersonSearch, contentDescription = "Available Players")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Groups, contentDescription = "Create Party")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Map, contentDescription = "Map view")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = "Create Match")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = true, onClick = {}, label = { Text("Open Matches") })
                FilterChip(selected = false, onClick = {}, label = { Text("My Matches") })
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { FilterChip(selected = true, onClick = {}, label = { Text("All") }) }
                items(SportType.entries.toList()) { sport ->
                    FilterChip(selected = false, onClick = {}, label = { Text(sport.displayName) })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No matches found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create one or change your filters!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
