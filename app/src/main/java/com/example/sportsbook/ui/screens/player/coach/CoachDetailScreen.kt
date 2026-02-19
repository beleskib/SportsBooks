package com.example.sportsbook.ui.screens.player.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.CoachCertification
import com.example.sportsbook.domain.model.Review
import com.example.sportsbook.ui.common.DiscountBadge
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.PriceTag
import com.example.sportsbook.ui.common.RatingBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachDetailScreen(
    coachId: Long,
    onBookClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: CoachViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(coachId) {
        viewModel.loadCoachDetail(coachId)
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
            val coach = uiState.selectedCoach
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = coach?.name ?: "") },
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
                        text = { Text(text = "Book Session") },
                        icon = {},
                        onClick = onBookClick
                    )
                }
            ) { innerPadding ->
                if (coach != null) {
                    CoachDetailContent(
                        coach = coach,
                        reviews = uiState.reviews,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun CoachDetailContent(
    coach: Coach,
    reviews: List<Review>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Coach Images",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                if (coach.activeDiscount != null) {
                    DiscountBadge(
                        text = coach.activeDiscount.displayValue,
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
                    text = coach.name,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = coach.specialization ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${coach.experienceYears} years of experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBar(rating = coach.avgRating)

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "(${coach.totalReviews} reviews)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                PriceTag(
                    price = coach.pricePerHour,
                    discountedPrice = coach.discountedPrice
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = coach.bio ?: "No bio available",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Certifications",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (coach.certifications.isEmpty()) {
            item {
                Text(
                    text = "No certifications listed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        } else {
            items(
                items = coach.certifications,
                key = { certification -> certification.id }
            ) { certification ->
                CertificationItem(
                    certification = certification,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
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
                CoachReviewItem(
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
private fun CertificationItem(
    certification: CoachCertification,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = certification.name,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "${certification.issuingBody ?: "Unknown"} · ${certification.yearObtained ?: ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun CoachReviewItem(
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

@Preview(showBackground = true)
@Composable
private fun CoachDetailScreenPreview() {
    MaterialTheme {
        CoachDetailScreen(
            coachId = 1L,
            onBookClick = {},
            onBack = {}
        )
    }
}
