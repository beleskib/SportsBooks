package com.example.sportsbook.ui.screens.player.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerHomeScreen(
    onSportClick: (String) -> Unit,
    onVenueClick: (Long) -> Unit,
    onCoachClick: (Long) -> Unit,
    viewModel: PlayerHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.error != null -> ErrorView(
            message = uiState.error!!,
            onRetry = viewModel::loadData
        )
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = "Explore Sports",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.sports.forEach { sport ->
                            ElevatedCard(
                                modifier = Modifier
                                    .width(100.dp)
                                    .clickable { onSportClick(sport.sportType.name) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(40.dp)
                                            .width(40.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = MaterialTheme.shapes.small
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = sport.sportType.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (uiState.topDealVenues.isNotEmpty()) {
                    item {
                        Text(
                            text = "Top Deals — Venues",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.topDealVenues) { venue ->
                                Card(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clickable { onVenueClick(venue.id) }
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                        ) {
                                            if (venue.activeDiscount != null) {
                                                DiscountBadge(
                                                    text = venue.activeDiscount.displayValue,
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = venue.name,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                text = venue.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RatingBar(rating = venue.avgRating)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "(${venue.totalReviews})",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            PriceTag(
                                                price = venue.pricePerHour,
                                                discountedPrice = venue.discountedPrice
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.topDealCoaches.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Top Deals — Coaches",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.topDealCoaches) { coach ->
                                Card(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clickable { onCoachClick(coach.id) }
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                        ) {
                                            if (coach.activeDiscount != null) {
                                                DiscountBadge(
                                                    text = coach.activeDiscount.displayValue,
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = coach.name,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                text = coach.specialization ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RatingBar(rating = coach.avgRating)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "(${coach.totalReviews})",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            PriceTag(
                                                price = coach.pricePerHour,
                                                discountedPrice = coach.discountedPrice
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerHomeScreenPreview() {
    MaterialTheme {
        PlayerHomeScreen(
            onSportClick = {},
            onVenueClick = {},
            onCoachClick = {}
        )
    }
}
