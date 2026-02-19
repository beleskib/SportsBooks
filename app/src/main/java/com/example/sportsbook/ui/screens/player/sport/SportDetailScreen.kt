package com.example.sportsbook.ui.screens.player.sport

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportDetailScreen(
    sportType: String,
    onVenueClick: (Long) -> Unit,
    onCoachClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: SportDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Venues", "Coaches")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = uiState.sportDisplayName.ifBlank { sportType })
                },
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }

            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.error != null -> ErrorView(
                    message = uiState.error!!,
                    onRetry = viewModel::loadData
                )
                selectedTabIndex == 0 -> {
                    if (uiState.venues.isEmpty()) {
                        EmptyStateView(title = "No venues found")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.venues) { venue ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onVenueClick(venue.id) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = venue.name,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    text = venue.address,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    RatingBar(rating = venue.avgRating)
                                                    Spacer(modifier = Modifier.weight(1f))
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
                                    if (venue.activeDiscount != null) {
                                        DiscountBadge(
                                            text = venue.activeDiscount.displayValue,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(top = 6.dp, end = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    if (uiState.coaches.isEmpty()) {
                        EmptyStateView(title = "No coaches found")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.coaches) { coach ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onCoachClick(coach.id) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = coach.name,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    text = coach.specialization ?: "",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "${coach.experienceYears}y exp",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    RatingBar(rating = coach.avgRating)
                                                    Spacer(modifier = Modifier.weight(1f))
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
                                    if (coach.activeDiscount != null) {
                                        DiscountBadge(
                                            text = coach.activeDiscount.displayValue,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(top = 6.dp, end = 16.dp)
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

@Preview(showBackground = true)
@Composable
fun SportDetailScreenPreview() {
    MaterialTheme {
        SportDetailScreen(
            sportType = "BASKETBALL",
            onVenueClick = {},
            onCoachClick = {},
            onBack = {}
        )
    }
}
