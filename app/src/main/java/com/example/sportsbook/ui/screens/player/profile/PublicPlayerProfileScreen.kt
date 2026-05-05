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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.sportsbook.ui.screens.player.match.components.PlayerRatingStars
import com.example.sportsbook.ui.theme.BorderGray
import com.example.sportsbook.ui.theme.LightBg
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PublicPlayerProfileScreen(
    onBack: () -> Unit,
    viewModel: PublicPlayerProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.friendActionSuccess) {
        uiState.friendActionSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            if (uiState.profile != null) {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Player Profile", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavBarBg
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = NavBarBg
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
                    profile = uiState.profile!!,
                    friendActionLoading = uiState.friendActionLoading,
                    onSendFriendRequest = viewModel::sendFriendRequest,
                    onRemoveFriend = viewModel::removeFriend
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PublicPlayerProfileContent(
    profile: PublicPlayerProfile,
    friendActionLoading: Boolean = false,
    onSendFriendRequest: () -> Unit = {},
    onRemoveFriend: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Profile Header ──
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                if (profile.photoUrl != null) {
                    AsyncImage(
                        model = profile.photoUrl,
                        contentDescription = "Player photo",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(BorderGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Player avatar",
                            modifier = Modifier.size(56.dp),
                            tint = GoldAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                Text(
                    text = profile.displayName ?: "Unknown Player",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                // Bio
                if (!profile.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // Member since
                if (profile.createdAt != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val memberSince = try {
                        profile.createdAt.substring(0, 10)
                    } catch (_: Exception) {
                        profile.createdAt
                    }
                    Text(
                        text = "Member since $memberSince",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Friend Action Button ──
                FriendActionButton(
                    friendshipStatus = profile.friendshipStatus,
                    isLoading = friendActionLoading,
                    onSendRequest = onSendFriendRequest,
                    onRemoveFriend = onRemoveFriend
                )
            }
        }

        // ── Stats Row ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightBg.copy(alpha = 0.5f))
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatItem(
                    value = profile.totalMatchesPlayed.toString(),
                    label = "Matches"
                )
                ProfileStatItem(
                    value = String.format("%.1f", profile.avgPlayerSkillRating),
                    label = "Skill"
                )
                ProfileStatItem(
                    value = profile.totalPlayerRatings.toString(),
                    label = "Ratings"
                )
            }
        }

        // ── Player Ratings Section ──
        if (profile.totalPlayerRatings > 0) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle("Player Ratings")
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBg.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        RatingRow(label = "Skill", rating = profile.avgPlayerSkillRating)
                        Spacer(modifier = Modifier.height(12.dp))
                        RatingRow(label = "Sportsmanship", rating = profile.avgPlayerSportsmanshipRating)
                        Spacer(modifier = Modifier.height(12.dp))
                        RatingRow(label = "Punctuality", rating = profile.avgPlayerPunctualityRating)
                    }
                }
            }
        }

        // ── Sport Interests ──
        if (profile.interestedSports.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle("Interested Sports")
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.interestedSports.forEach { sport ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    sport.displayName,
                                    color = TextPrimary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SportsSoccer,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = GoldAccent
                                )
                            }
                        )
                    }
                }
            }
        }

        // ── Recent Matches ──
        if (profile.recentMatches.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle("Recent Matches")
                Spacer(modifier = Modifier.height(12.dp))
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
private fun FriendActionButton(
    friendshipStatus: String?,
    isLoading: Boolean,
    onSendRequest: () -> Unit,
    onRemoveFriend: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> {
            Button(
                onClick = {},
                enabled = false,
                modifier = modifier.fillMaxWidth(0.6f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = BorderGray
                )
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Loading...", color = TextPrimary.copy(alpha = 0.6f))
            }
        }
        friendshipStatus == null -> {
            // No relationship — show Add Friend
            Button(
                onClick = onSendRequest,
                modifier = modifier.fillMaxWidth(0.6f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent
                )
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = NavBarBg
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Friend", color = NavBarBg, fontWeight = FontWeight.Bold)
            }
        }
        friendshipStatus == "pending" -> {
            // Pending request
            OutlinedButton(
                onClick = {},
                enabled = false,
                modifier = modifier.fillMaxWidth(0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.HourglassTop,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = GoldAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Request Pending", color = GoldAccent)
            }
        }
        friendshipStatus == "accepted" -> {
            // Already friends — show Friends badge with option to remove
            Row(
                modifier = modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFF2E7D32).copy(alpha = 0.3f)
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF66BB6A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Friends", color = Color(0xFF66BB6A))
                }
                OutlinedButton(
                    onClick = onRemoveFriend,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.PersonRemove,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Remove",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        else -> {
            // Declined/blocked — allow re-send
            Button(
                onClick = onSendRequest,
                modifier = modifier.fillMaxWidth(0.6f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent
                )
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = NavBarBg
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Friend", color = NavBarBg, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileStatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = GoldAccent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = TextPrimary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Composable
private fun RatingRow(
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
            color = TextPrimary.copy(alpha = 0.8f),
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
                color = GoldAccent
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightBg.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = GoldAccent
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = match.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary
                    )
                    Text(
                        text = "${match.sportType.displayName} • ${match.matchDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary.copy(alpha = 0.5f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (match.status.lowercase()) {
                            "completed" -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                            "in_progress" -> GoldAccent.copy(alpha = 0.2f)
                            else -> BorderGray.copy(alpha = 0.3f)
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = match.status.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (match.status.lowercase()) {
                        "completed" -> Color(0xFF66BB6A)
                        "in_progress" -> GoldAccent
                        else -> TextPrimary.copy(alpha = 0.6f)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun PublicPlayerProfileScreenPreview() {
    SportsBookTheme {
        PublicPlayerProfileContent(
            profile = PublicPlayerProfile(
                id = 1,
                displayName = "Alex Johnson",
                bio = "Passionate basketball and tennis player. Always looking for a game!",
                interestedSports = listOf(SportType.BASKETBALL, SportType.TENNIS, SportType.FOOTBALL),
                avgPlayerSkillRating = 4.2,
                avgPlayerSportsmanshipRating = 4.8,
                avgPlayerPunctualityRating = 3.9,
                totalPlayerRatings = 15,
                totalMatchesPlayed = 23,
                recentMatches = listOf(
                    PublicMatchSummary(1, "Friday Basketball", SportType.BASKETBALL, "2026-03-10", "completed"),
                    PublicMatchSummary(2, "Weekend Tennis", SportType.TENNIS, "2026-03-08", "completed")
                ),
                createdAt = "2025-06-15T10:00:00Z",
                friendshipStatus = null
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun PublicPlayerProfileFriendPreview() {
    SportsBookTheme {
        PublicPlayerProfileContent(
            profile = PublicPlayerProfile(
                id = 2,
                displayName = "Sarah Wilson",
                bio = "Tennis enthusiast and weekend warrior.",
                interestedSports = listOf(SportType.TENNIS, SportType.PADDLE),
                avgPlayerSkillRating = 3.8,
                avgPlayerSportsmanshipRating = 4.5,
                avgPlayerPunctualityRating = 4.7,
                totalPlayerRatings = 8,
                totalMatchesPlayed = 12,
                recentMatches = emptyList(),
                createdAt = "2025-09-01T10:00:00Z",
                friendshipStatus = "accepted"
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun PublicPlayerProfilePendingPreview() {
    SportsBookTheme {
        PublicPlayerProfileContent(
            profile = PublicPlayerProfile(
                id = 3,
                displayName = "Mike Chen",
                photoUrl = null,
                bio = null,
                interestedSports = listOf(SportType.VOLLEYBALL),
                avgPlayerSkillRating = 0.0,
                avgPlayerSportsmanshipRating = 0.0,
                avgPlayerPunctualityRating = 0.0,
                totalPlayerRatings = 0,
                totalMatchesPlayed = 0,
                recentMatches = emptyList(),
                createdAt = "2026-01-20T10:00:00Z",
                friendshipStatus = "pending"
            )
        )
    }
}
