package com.example.sportsbook.ui.screens.player.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.PublicMatchSummary
import com.example.sportsbook.domain.model.PublicPlayerProfile
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.screens.player.match.components.PlayerRatingStars

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PublicPlayerProfileScreen(
    onBack: () -> Unit,
    viewModel: PublicPlayerProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Player Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.error != null && uiState.profile == null -> ErrorView(
                    message = uiState.error!!,
                    onRetry = viewModel::loadProfile
                )
                uiState.profile != null -> PublicPlayerProfileContent(
                    profile = uiState.profile!!
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PublicPlayerProfileContent(
    profile: PublicPlayerProfile,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar + name + bio
        item {
            Spacer(modifier = Modifier.height(24.dp))
            if (profile.photoUrl != null) {
                AsyncImage(
                    model = profile.photoUrl,
                    contentDescription = "Player photo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Player avatar",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = profile.displayName ?: "Unknown Player",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            if (!profile.bio.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = profile.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Stats row
        item {
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PlayerStatItem(
                    label = "Matches Played",
                    value = profile.totalMatchesPlayed.toString()
                )
                PlayerStatItem(
                    label = "Total Ratings",
                    value = profile.totalPlayerRatings.toString()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Ratings section
        item {
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Player Ratings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            PlayerRatingRow(label = "Skill", rating = profile.avgPlayerSkillRating)
            Spacer(modifier = Modifier.height(8.dp))
            PlayerRatingRow(label = "Sportsmanship", rating = profile.avgPlayerSportsmanshipRating)
            Spacer(modifier = Modifier.height(8.dp))
            PlayerRatingRow(label = "Punctuality", rating = profile.avgPlayerPunctualityRating)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sport interests
        if (profile.interestedSports.isNotEmpty()) {
            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Interested Sports",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    profile.interestedSports.forEach { sport ->
                        AssistChip(
                            onClick = {},
                            label = { Text(sport.displayName) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SportsSoccer,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Recent matches
        if (profile.recentMatches.isNotEmpty()) {
            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Recent Matches",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(profile.recentMatches) { match ->
                RecentMatchItem(match = match)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PlayerStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlayerRatingRow(
    label: String,
    rating: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(120.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlayerRatingStars(rating = rating)
            Text(
                text = String.format("%.1f", rating),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentMatchItem(
    match: PublicMatchSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "${match.sportType.displayName} • ${match.matchDate.toDisplayDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = match.status.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublicPlayerProfileScreenPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Player Profile") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            PublicPlayerProfileContent(
                profile = PublicPlayerProfile(
                    id = 1,
                    displayName = "Alex Johnson",
                    bio = "Passionate basketball and tennis player.",
                    interestedSports = listOf(SportType.BASKETBALL, SportType.TENNIS),
                    avgPlayerSkillRating = 4.2,
                    avgPlayerSportsmanshipRating = 4.8,
                    avgPlayerPunctualityRating = 3.9,
                    totalPlayerRatings = 15,
                    totalMatchesPlayed = 23,
                    recentMatches = listOf(
                        PublicMatchSummary(1, "Friday Basketball", SportType.BASKETBALL, "2026-03-10", "completed"),
                        PublicMatchSummary(2, "Weekend Tennis", SportType.TENNIS, "2026-03-08", "completed")
                    )
                ),
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PublicPlayerProfileContentPreview() {
    MaterialTheme {
        PublicPlayerProfileContent(
            profile = PublicPlayerProfile(
                id = 1,
                displayName = "Alex Johnson",
                bio = "Passionate basketball and tennis player.",
                interestedSports = listOf(SportType.BASKETBALL, SportType.TENNIS),
                avgPlayerSkillRating = 4.2,
                avgPlayerSportsmanshipRating = 4.8,
                avgPlayerPunctualityRating = 3.9,
                totalPlayerRatings = 15,
                totalMatchesPlayed = 23,
                recentMatches = listOf(
                    PublicMatchSummary(1, "Friday Basketball", SportType.BASKETBALL, "2026-03-10", "completed"),
                    PublicMatchSummary(2, "Weekend Tennis", SportType.TENNIS, "2026-03-08", "completed")
                )
            )
        )
    }
}
