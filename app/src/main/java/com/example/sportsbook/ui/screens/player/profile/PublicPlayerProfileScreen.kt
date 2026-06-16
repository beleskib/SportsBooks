package com.example.sportsbook.ui.screens.player.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.PublicMatchSummary
import com.example.sportsbook.domain.model.PublicPlayerProfile
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.screens.player.match.components.PlayerRatingStars
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary

@OptIn(ExperimentalLayoutApi::class)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                Spacer(modifier = Modifier.width(12.dp))
                Text("Player Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }

            // ── Content ──────────────────────────────────────────────────
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
                            .background(DarkBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Player avatar",
                            modifier = Modifier.size(56.dp),
                            tint = GreenAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.displayName ?: "Unknown Player",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary,
                )

                if (!profile.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.bio,
                        fontSize = 14.sp,
                        color = DarkTextPrimary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                if (profile.createdAt != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val memberSince = try {
                        profile.createdAt.substring(0, 10)
                    } catch (_: Exception) {
                        profile.createdAt
                    }
                    Text(
                        text = "Member since $memberSince",
                        fontSize = 12.sp,
                        color = DarkTextPrimary.copy(alpha = 0.4f),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                    .background(DarkSurface)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatItem(value = profile.totalMatchesPlayed.toString(), label = "Matches")
                ProfileStatItem(value = String.format("%.1f", profile.avgPlayerSkillRating), label = "Skill")
                ProfileStatItem(value = profile.totalPlayerRatings.toString(), label = "Ratings")
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    RatingRow(label = "Skill", rating = profile.avgPlayerSkillRating)
                    Spacer(modifier = Modifier.height(12.dp))
                    RatingRow(label = "Sportsmanship", rating = profile.avgPlayerSportsmanshipRating)
                    Spacer(modifier = Modifier.height(12.dp))
                    RatingRow(label = "Punctuality", rating = profile.avgPlayerPunctualityRating)
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
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(GreenAccent.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = GreenAccent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(sport.displayName, fontSize = 13.sp, color = DarkTextPrimary)
                        }
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
            Box(
                modifier = modifier
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBorder)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = DarkTextPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Loading...", fontSize = 14.sp, color = DarkTextPrimary.copy(alpha = 0.6f))
                }
            }
        }
        friendshipStatus == null -> {
            Box(
                modifier = modifier
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onSendRequest)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp), tint = DarkBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Friend", fontSize = 14.sp, color = DarkBg, fontWeight = FontWeight.Bold)
                }
            }
        }
        friendshipStatus == "pending" -> {
            Box(
                modifier = modifier
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBg)
                    .border(1.dp, GreenAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HourglassTop, null, modifier = Modifier.size(18.dp), tint = GreenAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request Pending", fontSize = 14.sp, color = GreenAccent)
                }
            }
        }
        friendshipStatus == "accepted" -> {
            Row(
                modifier = modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E7D32).copy(alpha = 0.3f))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp), tint = Color(0xFF66BB6A))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Friends", fontSize = 14.sp, color = Color(0xFF66BB6A))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBg)
                        .border(1.dp, DarkTextPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onRemoveFriend)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonRemove, null, modifier = Modifier.size(16.dp), tint = Color(0xFFEF5350))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove", fontSize = 12.sp, color = Color(0xFFEF5350))
                    }
                }
            }
        }
        else -> {
            Box(
                modifier = modifier
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onSendRequest)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp), tint = DarkBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Friend", fontSize = 14.sp, color = DarkBg, fontWeight = FontWeight.Bold)
                }
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
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
        Text(text = label, fontSize = 12.sp, color = DarkTextPrimary.copy(alpha = 0.6f))
    }
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = DarkTextPrimary,
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
            fontSize = 14.sp,
            color = DarkTextPrimary.copy(alpha = 0.8f),
            modifier = Modifier.width(120.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlayerRatingStars(rating = rating)
            Text(text = String.format("%.1f", rating), fontSize = 12.sp, color = GreenAccent)
        }
    }
}

@Composable
private fun RecentMatchItem(
    match: PublicMatchSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
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
                tint = GreenAccent
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = match.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkTextPrimary,
                )
                Text(
                    text = "${match.sportType.displayName} • ${match.matchDate}",
                    fontSize = 12.sp,
                    color = DarkTextPrimary.copy(alpha = 0.5f),
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    when (match.status.lowercase()) {
                        "completed" -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                        "in_progress" -> GreenAccent.copy(alpha = 0.2f)
                        else -> DarkBorder.copy(alpha = 0.3f)
                    }
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = match.status.replaceFirstChar { it.uppercase() },
                fontSize = 10.sp,
                color = when (match.status.lowercase()) {
                    "completed" -> Color(0xFF66BB6A)
                    "in_progress" -> GreenAccent
                    else -> DarkTextPrimary.copy(alpha = 0.6f)
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PublicPlayerProfileScreenPreview() {
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

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PublicPlayerProfileFriendPreview() {
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

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PublicPlayerProfilePendingPreview() {
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
