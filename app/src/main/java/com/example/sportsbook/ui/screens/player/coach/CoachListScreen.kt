package com.example.sportsbook.ui.screens.player.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.ui.common.DiscountBadge
import com.example.sportsbook.ui.common.EmptyStateView
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.PriceTag
import com.example.sportsbook.ui.common.RatingBar
import com.example.sportsbook.ui.common.SearchBar
import com.example.sportsbook.ui.theme.BlueAccent
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary

@Composable
fun CoachListScreen(
    sportType: String,
    onCoachClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: CoachViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sportType) {
        viewModel.loadCoachesBySport(sportType)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Coaches", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.weight(1f))
        }

        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            placeholder = "Search coaches...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.fillMaxSize())

            uiState.error != null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::retry,
                modifier = Modifier.fillMaxSize(),
            )

            uiState.coaches.isEmpty() -> EmptyStateView(
                title = "No coaches found",
                subtitle = "Try adjusting your search or check back later.",
                modifier = Modifier.fillMaxSize(),
            )

            else -> {
                val filteredCoaches = uiState.coaches.filter { coach ->
                    uiState.searchQuery.isBlank() ||
                        coach.name.contains(uiState.searchQuery, ignoreCase = true) ||
                        coach.specialization?.contains(uiState.searchQuery, ignoreCase = true) == true
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(items = filteredCoaches, key = { it.id }) { coach ->
                        CoachCard(
                            coach = coach,
                            onClick = { onCoachClick(coach.id) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CoachCard(
    coach: Coach,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BlueAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = coach.name.take(1).uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueAccent,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = coach.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextPrimary,
                    maxLines = 1,
                )
                if (!coach.specialization.isNullOrBlank()) {
                    Text(
                        text = coach.specialization!!,
                        fontSize = 13.sp,
                        color = DarkTextSecondary,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Text(
                    text = "${coach.experienceYears} years exp",
                    fontSize = 13.sp,
                    color = DarkTextSecondary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBar(rating = coach.avgRating)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("(${coach.totalReviews})", fontSize = 12.sp, color = DarkTextSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                PriceTag(price = coach.pricePerHour, discountedPrice = coach.discountedPrice)
            }
        }

        if (coach.activeDiscount != null) {
            DiscountBadge(
                text = coach.activeDiscount.displayValue,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 6.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CoachListScreenPreview() {
    val sampleCoaches = listOf(
        Coach(id = 1L, name = "Alex Rivera", specialization = "Point Guard Trainer", experienceYears = 8, pricePerHour = 60.0, avgRating = 4.7, totalReviews = 34),
        Coach(id = 2L, name = "Maria Gonzalez", specialization = "Defensive Coach", experienceYears = 5, pricePerHour = 45.0, avgRating = 4.3, totalReviews = 18),
    )
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Coaches", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(sampleCoaches, key = { it.id }) { coach ->
                CoachCard(coach = coach, onClick = {}, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
