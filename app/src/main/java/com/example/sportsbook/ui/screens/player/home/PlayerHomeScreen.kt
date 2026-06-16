package com.example.sportsbook.ui.screens.player.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.MatchStatus
import com.example.sportsbook.domain.enums.MatchType
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.Party
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.SportsBookTheme
import java.util.Calendar

// ============================================================
// PlayerHomeScreen — Home tab, v3 mockup redesign
// Dark theme: #121212 bg, #1E1E1E cards, #4CAF50 green accent
// ============================================================

// ── Design tokens ──
private val ScreenBg = DarkBg
private val CardBg = DarkSurface
private val SubtleCardBg = Color.White.copy(alpha = 0.06f)
private val SubtleBorder = Color.White.copy(alpha = 0.06f)
private val AccentGreen = GreenAccent
private val GoldColor = Color(0xFFFFC107)
private val BlueAccent = Color(0xFF2196F3)
private val OrangeAccent = Color(0xFFFF9800)
private val PurpleAccent = Color(0xFF9C27B0)
private val RedBadge = Color(0xFFEF4444)

// ── Sport emoji + gradient mapping ──
private data class SportVisual(val emoji: String, val gradientStart: Color, val gradientEnd: Color)

private val sportVisuals = mapOf(
    SportType.BASKETBALL to SportVisual("🏀", Color(0xFFFB923C), Color(0xFFF97316)),
    SportType.FOOTBALL to SportVisual("⚽", Color(0xFF22C55E), Color(0xFF16A34A)),
    SportType.TENNIS to SportVisual("🎾", Color(0xFFA855F7), Color(0xFF7C3AED)),
    SportType.PADDLE to SportVisual("🏓", Color(0xFF06B6D4), Color(0xFF0891B2)),
    SportType.VOLLEYBALL to SportVisual("🏐", Color(0xFFF59E0B), Color(0xFFD97706)),
    SportType.SWIMMING to SportVisual("🏊", Color(0xFF0EA5E9), Color(0xFF0369A1)),
    SportType.BOXING to SportVisual("🥊", Color(0xFFEF4444), Color(0xFFB91C1C)),
    SportType.MMA to SportVisual("🥋", Color(0xFFDC2626), Color(0xFF991B1B)),
    SportType.YOGA to SportVisual("🧘", Color(0xFFEC4899), Color(0xFFBE185D)),
    SportType.PILATES to SportVisual("🤸", Color(0xFFF472B6), Color(0xFFDB2777)),
    SportType.CROSSFIT to SportVisual("🏋️", Color(0xFFFF4500), Color(0xFFCC3700)),
    SportType.RUNNING to SportVisual("🏃", Color(0xFF0EA5E9), Color(0xFF0369A1)),
    SportType.CYCLING to SportVisual("🚴", Color(0xFF14B8A6), Color(0xFF0F766E)),
    SportType.GOLF to SportVisual("⛳", Color(0xFF22C55E), Color(0xFF15803D)),
    SportType.BADMINTON to SportVisual("🏸", Color(0xFF80ED99), Color(0xFF38A169)),
    SportType.TABLE_TENNIS to SportVisual("🏓", Color(0xFFFF5400), Color(0xFFCC4300)),
    SportType.HANDBALL to SportVisual("🤾", Color(0xFF264653), Color(0xFF1B3A4B)),
    SportType.BASEBALL to SportVisual("⚾", Color(0xFFBC6C25), Color(0xFF92400E)),
    SportType.CRICKET to SportVisual("🏏", Color(0xFF606C38), Color(0xFF3F4F24)),
)

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

// ── Avatar colors for placeholder initials ──
private val avatarColors = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800),
    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF00BCD4)
)

private fun avatarColor(index: Int) = avatarColors[index % avatarColors.size]


// ─────────────────────────────────────────────────────────────
// Root composable — keep signature unchanged
// ─────────────────────────────────────────────────────────────
@Composable
fun PlayerHomeScreen(
    onSportClick: (String) -> Unit,
    onVenueClick: (Long) -> Unit,
    onCoachClick: (Long) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToChats: () -> Unit = {},
    onNavigateToCreateParty: () -> Unit = {},
    onNavigateToPartyDetail: (Long) -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onSignOut: () -> Unit,
    onFindMatch: () -> Unit = {},
    onMatchClick: (Long) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToFriends: () -> Unit = {},
    onBrowseAllSports: () -> Unit = {},
    onNavigateToCommunities: () -> Unit = {},
    refreshTrigger: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    viewModel: PlayerHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.loadData()
            onRefreshConsumed()
        }
    }

    LaunchedEffect(uiState.matchStatusMessage) {
        uiState.matchStatusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMatchStatusMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.error != null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadData,
            )
            else -> PlayerHomeContent(
                uiState = uiState,
                onSportClick = onSportClick,
                onVenueClick = onVenueClick,
                onCoachClick = onCoachClick,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToChats = onNavigateToChats,
                onNavigateToCreateParty = onNavigateToCreateParty,
                onNavigateToPartyDetail = onNavigateToPartyDetail,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToFavorites = onNavigateToFavorites,
                onFindMatch = onFindMatch,
                onMatchClick = onMatchClick,
                onBrowseAllSports = onBrowseAllSports,
                notificationCount = uiState.notificationCount,
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Main content — LazyColumn of all sections
// ─────────────────────────────────────────────────────────────
@Composable
private fun PlayerHomeContent(
    uiState: PlayerHomeUiState,
    onSportClick: (String) -> Unit,
    onVenueClick: (Long) -> Unit,
    onCoachClick: (Long) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToCreateParty: () -> Unit,
    onNavigateToPartyDetail: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onFindMatch: () -> Unit,
    onMatchClick: (Long) -> Unit = {},
    onBrowseAllSports: () -> Unit = {},
    notificationCount: Int = 0
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {

        // ── 1. Header ──────────────────────────────────────────
        item {
            HomeHeader(
                user = uiState.user,
                notificationCount = notificationCount,
                onNotificationClick = onNavigateToNotifications,
                onProfileClick = onNavigateToProfile
            )
        }

        // ── 2. Coming Up card ──────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HomeSectionHeader(
                title = "⚡ Coming Up",
                actionLabel = null,
                onAction = {}
            )
            Spacer(modifier = Modifier.height(10.dp))
            val nextMatch = uiState.activeMatches.firstOrNull()
            if (nextMatch != null) {
                ComingUpCard(
                    match = nextMatch,
                    onClick = { onMatchClick(nextMatch.id) }
                )
            } else {
                ComingUpEmptyCard(onFindMatch = onFindMatch)
            }
        }

        // ── 3. Quick Actions ───────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(20.dp))
            QuickActionsRow(
                onFindMatch = onFindMatch,
                onBookVenue = onNavigateToSearch,
                onCreateParty = onNavigateToCreateParty
            )
        }

        // ── 4. Active Party Banner (conditional) ──────────────
        val firstParty = uiState.activeParties.firstOrNull()
        if (firstParty != null) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ActivePartyBanner(
                    party = firstParty,
                    onClick = { onNavigateToPartyDetail(firstParty.id) }
                )
            }
        }

        // ── 5. Upcoming Bookings ───────────────────────────────
        item {
            Spacer(modifier = Modifier.height(20.dp))
            HomeSectionHeader(
                title = "📅 Upcoming Bookings",
                actionLabel = "See All",
                onAction = onNavigateToSearch
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        val bookingProxies = uiState.topDealVenues.take(3)
        if (bookingProxies.isNotEmpty()) {
            items(bookingProxies, key = { "booking-${it.id}" }) { venue ->
                UpcomingBookingCard(
                    venue = venue,
                    onClick = { onVenueClick(venue.id) }
                )
            }
        } else {
            item {
                UpcomingBookingsEmpty(onBook = onNavigateToSearch)
            }
        }

        // ── 6. Top Deals ───────────────────────────────────────
        if (uiState.topDealVenues.isNotEmpty() || uiState.topDealCoaches.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                HomeSectionHeader(
                    title = "🔥 Top Deals",
                    actionLabel = "See All",
                    onAction = onNavigateToSearch
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.topDealVenues, key = { "deal-v-${it.id}" }) { venue ->
                        TopDealCard(
                            name = venue.name,
                            sportType = venue.sportType,
                            originalPrice = venue.pricePerHour,
                            discountedPrice = venue.discountedPrice,
                            discountPercent = venue.activeDiscount?.discountPercent,
                            onClick = { onVenueClick(venue.id) }
                        )
                    }
                    items(uiState.topDealCoaches, key = { "deal-c-${it.id}" }) { coach ->
                        TopDealCard(
                            name = coach.name,
                            sportType = coach.sportType,
                            originalPrice = coach.pricePerHour,
                            discountedPrice = coach.discountedPrice,
                            discountPercent = coach.activeDiscount?.discountPercent,
                            onClick = { onCoachClick(coach.id) }
                        )
                    }
                }
            }
        }

        // ── 7. Activity Feed ───────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(20.dp))
            HomeSectionHeader(
                title = "📣 Activity Feed",
                actionLabel = null,
                onAction = {}
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SubtleCardBg)
                    .border(1.dp, SubtleBorder, RoundedCornerShape(14.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No activity yet",
                    fontSize = 13.sp,
                    color = DarkTextSecondary,
                )
            }
        }

        // ── 8. Friends Leaderboard Mini ────────────────────────
        item {
            Spacer(modifier = Modifier.height(20.dp))
            FriendsLeaderboardMini(onFullBoard = {})
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Section 1: Header
// ─────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(
    user: User?,
    notificationCount: Int,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ScreenBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: greeting + name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${getGreeting()} 👋",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = user?.displayName ?: "Athlete",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary
            )
        }

        // Right: XP chip
        XpChipInline()

        Spacer(modifier = Modifier.width(10.dp))

        // Notification bell with badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SubtleCardBg)
                .clickable(onClick = onNotificationClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp)
            )
            if (notificationCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .background(RedBadge, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (notificationCount > 99) "99+" else notificationCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 9.sp
                    )
                }
            }
        }
    }
}

// Inline XP chip (no hiltViewModel, pure visual) for the header
@Composable
private fun XpChipInline(xp: Int = 0) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(GoldColor.copy(alpha = 0.15f))
            .border(1.dp, GoldColor.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = GoldColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (xp > 0) "$xp XP" else "XP",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoldColor
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Section 2: Coming Up card
// ─────────────────────────────────────────────────────────────
@Composable
private fun ComingUpCard(match: Match, onClick: () -> Unit) {
    val visual = sportVisuals[match.sportType]
        ?: SportVisual("🏅", Color(0xFF4CAF50), Color(0xFF0288D1))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        visual.gradientStart.copy(alpha = 0.85f),
                        visual.gradientEnd.copy(alpha = 0.95f)
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Column {
            // Time badge top-right
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFF4CAF50).copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🕐 In 2 hours",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sport + match type
            Text(
                text = "${visual.emoji} ${match.sportType.displayName} • ${match.matchType.displayName}",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Match title
            Text(
                text = match.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Location + time
            Text(
                text = "📍 ${match.displayLocation}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "⏰ ${match.matchDate} · ${match.displayTime}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom row: avatars + count + view button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Overlapping avatar stack (up to 4)
                val avatarCount = minOf(match.currentPlayers, 4)
                Box(modifier = Modifier.height(28.dp)) {
                    repeat(avatarCount) { idx ->
                        Box(
                            modifier = Modifier
                                .offset(x = (idx * 18).dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(avatarColor(idx))
                                .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ('A' + idx).toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width((avatarCount * 18 + 10).dp))

                Text(
                    text = "${match.currentPlayers}/${match.maxPlayers} players",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "View",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ComingUpEmptyCard(onFindMatch: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        GreenAccent.copy(alpha = 0.15f),
                        Color(0xFF0288D1).copy(alpha = 0.15f)
                    )
                )
            )
            .border(1.dp, SubtleBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onFindMatch)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🏅", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No upcoming matches",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap to find a game near you",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Section 3: Quick Actions
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuickActionsRow(
    onFindMatch: () -> Unit,
    onBookVenue: () -> Unit,
    onCreateParty: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionItem(
            emoji = "🎯",
            label = "Find Match",
            iconBg = GreenAccent.copy(alpha = 0.18f),
            iconTint = GreenAccent,
            onClick = onFindMatch,
            modifier = Modifier.weight(1f)
        )
        QuickActionItem(
            emoji = "📅",
            label = "Book Venue",
            iconBg = BlueAccent.copy(alpha = 0.18f),
            iconTint = BlueAccent,
            onClick = onBookVenue,
            modifier = Modifier.weight(1f)
        )
        QuickActionItem(
            emoji = "👥",
            label = "Create Party",
            iconBg = OrangeAccent.copy(alpha = 0.18f),
            iconTint = OrangeAccent,
            onClick = onCreateParty,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionItem(
    emoji: String,
    label: String,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SubtleCardBg)
            .border(1.dp, SubtleBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkTextPrimary,
            maxLines = 1
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Section 4: Active Party Banner
// ─────────────────────────────────────────────────────────────
@Composable
private fun ActivePartyBanner(party: Party, onClick: () -> Unit) {
    val memberCount = party.members.size
    val maxMembers = 6 // fallback; Party model does not expose maxMembers

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PurpleAccent.copy(alpha = 0.08f))
            .border(1.dp, PurpleAccent.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PurpleAccent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "👥", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = party.name ?: "My Party",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary
            )
            Text(
                text = "$memberCount/$maxMembers members",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(PurpleAccent.copy(alpha = 0.2f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Open",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PurpleAccent
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Section 5: Upcoming Bookings
// ─────────────────────────────────────────────────────────────
@Composable
private fun UpcomingBookingCard(venue: Venue, onClick: () -> Unit) {
    val visual = sportVisuals[venue.sportType]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SubtleCardBg)
            .border(1.dp, SubtleBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sport icon box
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BlueAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = visual?.emoji ?: "🏟️", fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = venue.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "⏰ Today · ${venue.pricePerHour.toInt()} den/h",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Status badge
        val isConfirmed = venue.avgRating > 3.0
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isConfirmed) GreenAccent.copy(alpha = 0.15f)
                    else OrangeAccent.copy(alpha = 0.15f)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isConfirmed) "Confirmed" else "Pending",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isConfirmed) GreenAccent else OrangeAccent
            )
        }
    }
}

@Composable
private fun UpcomingBookingsEmpty(onBook: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SubtleCardBg)
            .border(1.dp, SubtleBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onBook)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📅", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "No upcoming bookings",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Section 6: Top Deal Card (horizontal scroll)
// ─────────────────────────────────────────────────────────────
@Composable
private fun TopDealCard(
    name: String,
    sportType: SportType,
    originalPrice: Double,
    discountedPrice: Double?,
    discountPercent: Double?,
    onClick: () -> Unit
) {
    val visual = sportVisuals[sportType]
        ?: SportVisual("🏅", Color(0xFFEC4899), Color(0xFFF97316))

    Box(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(visual.gradientStart.copy(alpha = 0.7f), visual.gradientEnd)
                )
            )
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top row: emoji + discount badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(text = visual.emoji, fontSize = 28.sp)

                if (discountPercent != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "-${discountPercent.toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sportType.displayName,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing row
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (discountedPrice != null) {
                    Text(
                        text = "${originalPrice.toInt()} den",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.55f),
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${discountedPrice.toInt()} den",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF80FF8C)
                    )
                } else {
                    Text(
                        text = "${originalPrice.toInt()} den/h",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────
// Section 8: Friends Leaderboard Mini
// ─────────────────────────────────────────────────────────────
@Composable
private fun FriendsLeaderboardMini(onFullBoard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GoldColor.copy(alpha = 0.05f))
            .border(1.dp, GoldColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏆 Friends Leaderboard",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary
            )
            Text(
                text = "Full Board →",
                fontSize = 12.sp,
                color = GoldColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onFullBoard)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No leaderboard data",
                fontSize = 13.sp,
                color = DarkTextSecondary,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Shared helpers
// ─────────────────────────────────────────────────────────────
@Composable
private fun HomeSectionHeader(
    title: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextPrimary
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                fontSize = 12.sp,
                color = GreenAccent,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PlayerHomeScreenPreview() {
    val sampleUser = User(
        id = 1,
        displayName = "Bojan",
        email = "bojan@test.com",
        interestedSports = listOf(SportType.BASKETBALL, SportType.TENNIS)
    )
    val sampleVenues = listOf(
        Venue(
            id = 1, name = "Skopje Arena", sportType = SportType.BASKETBALL,
            pricePerHour = 800.0, address = "Main St", city = "Skopje", avgRating = 4.5
        ),
        Venue(
            id = 2, name = "Vodno Tennis Center", sportType = SportType.TENNIS,
            pricePerHour = 600.0, address = "Vodno", city = "Skopje", avgRating = 2.8
        ),
    )
    val sampleMatch = Match(
        id = 1,
        title = "Friday Night Basketball",
        sportType = SportType.BASKETBALL,
        matchType = MatchType.STANDALONE,
        status = MatchStatus.OPEN,
        venueName = "Skopje Arena",
        matchDate = "Fri, Jun 7",
        startTime = "18:00",
        endTime = "20:00",
        currentPlayers = 6,
        maxPlayers = 10
    )
    val previewState = PlayerHomeUiState(
        user = sampleUser,
        myMatches = listOf(sampleMatch),
        allVenues = sampleVenues,
        topDealVenues = sampleVenues,
        notificationCount = 3,
        isLoading = false
    )

    SportsBookTheme {
        PlayerHomeContent(
            uiState = previewState,
            onSportClick = {},
            onVenueClick = {},
            onCoachClick = {},
            onNavigateToProfile = {},
            onNavigateToNotifications = {},
            onNavigateToChats = {},
            onNavigateToCreateParty = {},
            onNavigateToPartyDetail = {},
            onNavigateToSearch = {},
            onNavigateToFavorites = {},
            onFindMatch = {},
            onMatchClick = {},
            onBrowseAllSports = {},
            notificationCount = previewState.notificationCount
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeHeaderPreview() {
    SportsBookTheme {
        HomeHeader(
            user = User(id = 1, displayName = "Bojan", email = ""),
            notificationCount = 5,
            onNotificationClick = {},
            onProfileClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun ComingUpCardPreview() {
    SportsBookTheme {
        ComingUpCard(
            match = Match(
                id = 1,
                title = "Friday Night Basketball",
                sportType = SportType.BASKETBALL,
                matchType = MatchType.STANDALONE,
                status = MatchStatus.OPEN,
                venueName = "Skopje Arena",
                matchDate = "Fri, Jun 7",
                startTime = "18:00",
                endTime = "20:00",
                currentPlayers = 8,
                maxPlayers = 10
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun QuickActionsPreview() {
    SportsBookTheme {
        QuickActionsRow(onFindMatch = {}, onBookVenue = {}, onCreateParty = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun TopDealCardPreview() {
    SportsBookTheme {
        TopDealCard(
            name = "Skopje Padel Club",
            sportType = SportType.PADDLE,
            originalPrice = 800.0,
            discountedPrice = 600.0,
            discountPercent = 25.0,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun FriendsLeaderboardPreview() {
    SportsBookTheme {
        FriendsLeaderboardMini(onFullBoard = {})
    }
}
