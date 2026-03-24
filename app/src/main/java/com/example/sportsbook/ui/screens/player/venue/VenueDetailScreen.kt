package com.example.sportsbook.ui.screens.player.venue

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Review
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.model.VenueEquipment
import com.example.sportsbook.ui.common.DiscountBadge
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.ImageCarousel
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.PriceTag
import com.example.sportsbook.ui.common.RatingBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenueDetailScreen(
    venueId: Long,
    onBookClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: VenueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(venueId) {
        viewModel.loadVenueDetail(venueId)
    }

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

        else -> {
            val venue = uiState.selectedVenue
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = venue?.name ?: "") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "Book Now") },
                        icon = {},
                        onClick = onBookClick
                    )
                }
            ) { innerPadding ->
                if (venue != null) {
                    VenueDetailContent(
                        venue = venue,
                        reviews = uiState.reviews,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun VenueDetailContent(
    venue: Venue,
    reviews: List<Review>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                ImageCarousel(
                    imageUrls = venue.images.map { it.imageUrl },
                    contentDescription = venue.name,
                    modifier = Modifier.fillMaxSize()
                )

                if (venue.activeDiscount != null) {
                    DiscountBadge(
                        text = venue.activeDiscount.displayValue,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = venue.name,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = venue.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBar(rating = venue.avgRating)

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "(${venue.totalReviews} reviews)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                PriceTag(
                    price = venue.pricePerHour,
                    discountedPrice = venue.discountedPrice
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = venue.description ?: "No description available",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Equipment",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        items(
            items = venue.equipment,
            key = { equipment -> equipment.id }
        ) { equipment ->
            EquipmentItem(
                equipment = equipment,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Divider()

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "(${reviews.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (reviews.isEmpty()) {
            item {
                Text(
                    text = "No reviews yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            items(
                items = reviews,
                key = { review -> review.id }
            ) { review ->
                ReviewItem(
                    review = review,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
private fun EquipmentItem(
    equipment: VenueEquipment,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (equipment.isIncluded) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (equipment.isIncluded) "Included" else "Not included",
            tint = if (equipment.isIncluded) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = equipment.name,
                style = MaterialTheme.typography.bodyMedium
            )

            if (!equipment.description.isNullOrBlank()) {
                Text(
                    text = equipment.description!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReviewItem(
    review: Review,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = review.playerName ?: "Anonymous",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )

            RatingBar(rating = review.rating.toDouble())
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (!review.comment.isNullOrBlank()) {
            Text(
                text = review.comment!!,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(2.dp))
        }

        Text(
            text = review.createdAt ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Divider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun VenueDetailScreenPreview() {
    val sampleVenue = Venue(
        id = 1L,
        name = "City Basketball Court",
        description = "A premium indoor basketball court with professional-grade flooring and equipment.",
        sportType = com.example.sportsbook.domain.enums.SportType.BASKETBALL,
        pricePerHour = 35.0,
        address = "123 Main Street, Skopje",
        avgRating = 4.6,
        totalReviews = 28,
        equipment = listOf(
            VenueEquipment(id = 1L, venueId = 1L, name = "Basketballs", isIncluded = true),
            VenueEquipment(id = 2L, venueId = 1L, name = "Lockers", isIncluded = true),
            VenueEquipment(id = 3L, venueId = 1L, name = "Parking", isIncluded = false)
        )
    )
    val sampleReviews = listOf(
        Review(id = 1L, playerId = 10L, venueId = 1L, rating = 5, comment = "Great court, very clean.", playerName = "Jordan M.", createdAt = "2026-02-20")
    )
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(sampleVenue.name) },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("Book Now") },
                    icon = {},
                    onClick = {}
                )
            }
        ) { innerPadding ->
            VenueDetailContent(
                venue = sampleVenue,
                reviews = sampleReviews,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
