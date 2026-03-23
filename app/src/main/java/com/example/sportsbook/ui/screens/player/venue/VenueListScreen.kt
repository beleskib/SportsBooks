package com.example.sportsbook.ui.screens.player.venue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.common.DiscountBadge
import com.example.sportsbook.ui.common.EmptyStateView
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.PriceTag
import com.example.sportsbook.ui.common.RatingBar
import com.example.sportsbook.ui.common.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenueListScreen(
    sportType: String,
    onVenueClick: (Long) -> Unit,
    onBack: () -> Unit,
    onShowMap: () -> Unit = {},
    viewModel: VenueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sportType) {
        viewModel.loadVenuesBySport(sportType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Venues") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShowMap) {
                        Icon(Icons.Default.Map, contentDescription = "Map view")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                placeholder = "Search venues...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                uiState.isLoading -> {
                    LoadingIndicator(modifier = Modifier.fillMaxSize())
                }

                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error!!,
                        onRetry = viewModel::retry,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.venues.isEmpty() -> {
                    EmptyStateView(
                        title = "No venues found",
                        subtitle = "Try adjusting your search or check back later.",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    val filteredVenues = uiState.venues.filter { venue ->
                        uiState.searchQuery.isBlank() ||
                            venue.name.contains(uiState.searchQuery, ignoreCase = true) ||
                            venue.address.contains(uiState.searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = filteredVenues,
                            key = { venue -> venue.id }
                        ) { venue ->
                            VenueCard(
                                venue = venue,
                                onClick = { onVenueClick(venue.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VenueCard(
    venue: com.example.sportsbook.domain.model.Venue,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = venue.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = venue.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = venue.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingBar(rating = venue.avgRating)

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "(${venue.totalReviews})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    PriceTag(
                        price = venue.pricePerHour,
                        discountedPrice = venue.discountedPrice
                    )
                }
            }

            if (venue.activeDiscount != null) {
                DiscountBadge(
                    text = venue.activeDiscount.displayValue,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VenueListScreenPreview() {
    MaterialTheme {
        VenueListScreen(
            sportType = "BASKETBALL",
            onVenueClick = {},
            onBack = {}
        )
    }
}
