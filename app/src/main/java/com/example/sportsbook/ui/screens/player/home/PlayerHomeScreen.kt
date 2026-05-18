package com.example.sportsbook.ui.screens.player.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.MatchStatus
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.Sport
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.ui.common.DiscountBadge
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.PriceTag
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.SportBasketball
import com.example.sportsbook.ui.theme.SportFootball
import com.example.sportsbook.ui.theme.SportTennis
import com.example.sportsbook.ui.theme.SportPaddle
import com.example.sportsbook.ui.theme.SportVolleyball
import com.example.sportsbook.ui.theme.SportSwimming
import com.example.sportsbook.ui.theme.SportBoxing
import com.example.sportsbook.ui.theme.SportMMA
import com.example.sportsbook.ui.theme.SportYoga
import com.example.sportsbook.ui.theme.SportPilates
import com.example.sportsbook.ui.theme.SportCrossfit
import com.example.sportsbook.ui.theme.SportRunning
import com.example.sportsbook.ui.theme.SportCycling
import com.example.sportsbook.ui.theme.SportGolf
import com.example.sportsbook.ui.theme.SportBadminton
import com.example.sportsbook.ui.theme.SportTableTennis
import com.example.sportsbook.ui.theme.SportHandball
import com.example.sportsbook.ui.theme.SportBaseball
import com.example.sportsbook.ui.theme.SportCricket
import java.util.Calendar

// ============================================================
// PlayerHomeScreen — "Discover" tab
// Visual direction: light / airy / confident.
//   Matches mockup profile-flow-android.html screen 1.7:
//   - Light gray background (gray-50)
//   - White m3-style cards with subtle elevation
//   - Dark nav bar with gold accents
//   - Colorful gradient sport tiles in a grid
//   - Uppercase section labels in gray-500
// ============================================================

// ── Design tokens (light theme) ──
private val LightBg = Color(0xFFF9FAFB)        // gray-50
private val CardBg = Color.White
private val CardBorder = Color(0xFFE5E7EB)     // gray-200
private val TextPrimary = Color(0xFF111827)     // gray-900
private val TextSecondary = Color(0xFF6B7280)   // gray-500
private val TextTertiary = Color(0xFF9CA3AF)    // gray-400
private val NavBarBg = Color(0xFF111827)        // gray-900
private val GoldAccent = Color(0xFFFDE047)      // yellow-300
private val ChipBg = Color.White
private val ChipBorder = Color(0xFFE5E7EB)
private val ChipActiveBg = Color(0xFF111827)
private val ChipActiveText = Color(0xFFFDE047)

// ── Sport visual mapping ──

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

@Composable
fun PlayerHomeScreen(
    onSportClick: (String) -> Unit,
    onVenueClick: (Long) -> Unit,
    onCoachClick: (Long) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
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

    // Auto-reload when returning from SportsIFollow (or any screen that sets the trigger)
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.loadData()
            onRefreshConsumed()
        }
    }

    // Show a snackbar whenever a watched match changes status
    LaunchedEffect(uiState.matchStatusMessage) {
        uiState.matchStatusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMatchStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadData
            )
            else -> PlayerHomeContent(
                uiState = uiState,
                onSportClick = onSportClick,
                onVenueClick = onVenueClick,
                onCoachClick = onCoachClick,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToFavorites = onNavigateToFavorites,
                onFindMatch = onFindMatch,
                onMatchClick = onMatchClick,
                onBrowseAllSports = onBrowseAllSports,
                notificationCount = uiState.notificationCount
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerHomeContent(
    uiState: PlayerHomeUiState,
    onSportClick: (String) -> Unit,
    onVenueClick: (Long) -> Unit,
    onCoachClick: (Long) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
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
            .background(LightBg),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Dark Nav Bar ──
        item {
            NavBar(
                location = uiState.cityName ?: "Discover",
                onNotificationClick = onNavigateToNotifications,
                notificationCount = notificationCount
            )
        }

        // ── Search Bar ──
        item {
            SearchBar(onClick = onNavigateToSearch)
        }

        // ── Your Active Matches ──
        if (uiState.activeMatches.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("Your matches · ${uiState.activeMatches.size}")
                    Text(
                        text = "See all →",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.clickable(onClick = onFindMatch)
                    )
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.activeMatches, key = { "match-${it.id}" }) { match ->
                        ActiveMatchCard(
                            match = match,
                            onClick = { onMatchClick(match.id) }
                        )
                    }
                }
            }
        }

        // ── Your Sports grid ──
        if (uiState.mySports.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("Your sports · ${uiState.mySports.size}")
                    Text(
                        text = "Edit →",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.clickable(onClick = onBrowseAllSports)
                    )
                }
            }

            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 4
                ) {
                    uiState.mySports.forEach { sport ->
                        SportTile(
                            sport = sport,
                            onClick = { onSportClick(sport.sportType.name.lowercase()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining slots with invisible spacers if needed
                    val remainder = uiState.mySports.size % 4
                    if (remainder != 0) {
                        repeat(4 - remainder) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // ── Tonight in your sports (Popular Venues) ──
        val venues = uiState.filteredVenues
        if (venues.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionLabel(
                    text = "Tonight in your sports",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(venues.take(5), key = { "v-${it.id}" }) { venue ->
                VenueCard(
                    venue = venue,
                    onClick = { onVenueClick(venue.id) }
                )
            }
        }

        // ── Top Coaches ──
        val coaches = uiState.filteredCoaches
        if (coaches.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionLabel(
                    text = "Top coaches",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(coaches, key = { "c-${it.id}" }) { coach ->
                        CoachCard(
                            coach = coach,
                            onClick = { onCoachClick(coach.id) }
                        )
                    }
                }
            }
        }

        // ── Top Deals ──
        val dealVenues = uiState.topDealVenues
        val dealCoaches = uiState.topDealCoaches
        if (dealVenues.isNotEmpty() || dealCoaches.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionLabel(
                    text = "Top deals",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dealVenues, key = { "dv-${it.id}" }) { venue ->
                        DealCard(
                            name = venue.name,
                            subtitle = venue.city ?: venue.address,
                            imageUrl = venue.primaryImageUrl,
                            price = venue.pricePerHour,
                            discountedPrice = venue.discountedPrice,
                            discountText = venue.activeDiscount?.displayValue,
                            rating = venue.avgRating,
                            onClick = { onVenueClick(venue.id) }
                        )
                    }
                    items(dealCoaches, key = { "dc-${it.id}" }) { coach ->
                        DealCard(
                            name = coach.name,
                            subtitle = coach.specialization ?: coach.sportType.displayName,
                            imageUrl = coach.primaryImageUrl,
                            price = coach.pricePerHour,
                            discountedPrice = coach.discountedPrice,
                            discountText = coach.activeDiscount?.displayValue,
                            rating = coach.avgRating,
                            onClick = { onCoachClick(coach.id) }
                        )
                    }
                }
            }
        }

        // ── Find a Match CTA ──
        item {
            Spacer(modifier = Modifier.height(16.dp))
            FindMatchCta(onClick = onFindMatch)
        }

        // ── "Want to see more sports?" CTA ──
        if (uiState.otherSports.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                AddSportCta(onClick = onBrowseAllSports)
            }
        }
    }
}

// ── Dark top nav bar (matches mockup) ──
@Composable
private fun NavBar(
    location: String,
    onNotificationClick: () -> Unit,
    notificationCount: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavBarBg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "📍 $location",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Box {
            IconButton(onClick = onNotificationClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = GoldAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            if (notificationCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .background(Color(0xFFEF4444), CircleShape),
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

// ── Search bar ──
@Composable
private fun SearchBar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(1.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(CardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = TextSecondary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Search venues, coaches…",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary
            )
        }
    }
}

// ── Section label (uppercase, gray-500, small) ──
@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        fontSize = 10.sp,
        modifier = modifier
    )
}

// ── Colorful gradient sport tile (matches mockup grid) ──
@Composable
private fun SportTile(
    sport: Sport,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visual = sportVisuals[sport.sportType]
        ?: SportVisual("🏀", SportBasketball, SportBasketball)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(listOf(visual.gradientStart, visual.gradientEnd))
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = visual.emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sport.sportType.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Venue card (white m3-card with subtle shadow, matches mockup) ──
@Composable
private fun VenueCard(venue: Venue, onClick: () -> Unit) {
    val visual = sportVisuals[venue.sportType]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (venue.primaryImageUrl != null) {
                AsyncImage(
                    model = venue.primaryImageUrl,
                    contentDescription = venue.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    visual?.gradientStart?.copy(alpha = 0.3f) ?: Color(0xFFA7F3D0),
                                    visual?.gradientEnd?.copy(alpha = 0.6f) ?: Color(0xFF6EE7B7)
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = venue.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Sport pill
                SportPill(
                    emoji = visual?.emoji ?: "",
                    label = venue.sportType.displayName
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${venue.city ?: venue.address} · ${venue.pricePerHour.toInt()} den/h",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
            if (venue.avgRating > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFFFBBF24)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format("%.1f", venue.avgRating),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Sport pill (small colored chip) ──
@Composable
private fun SportPill(emoji: String, label: String) {
    val visual = sportVisuals.entries.find { it.value.emoji == emoji }?.value
    val bgColor = visual?.gradientStart?.copy(alpha = 0.12f) ?: Color(0xFFE0E7FF)
    val textColor = visual?.gradientEnd ?: Color(0xFF4338CA)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$emoji $label",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Coach card (horizontal scroll) ──
@Composable
private fun CoachCard(coach: Coach, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                if (coach.primaryImageUrl != null) {
                    AsyncImage(
                        model = coach.primaryImageUrl,
                        contentDescription = coach.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val visual = sportVisuals[coach.sportType]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        visual?.gradientStart?.copy(alpha = 0.2f) ?: Color(0xFFDDD6FE),
                                        visual?.gradientEnd?.copy(alpha = 0.4f) ?: Color(0xFFA78BFA)
                                    )
                                )
                            )
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = coach.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = coach.specialization ?: coach.sportType.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (coach.avgRating > 0) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFFBBF24)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", coach.avgRating),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = "${coach.pricePerHour.toInt()} den/h",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// ── Deal card (horizontal scroll) ──
@Composable
private fun DealCard(
    name: String,
    subtitle: String,
    imageUrl: String?,
    price: Double,
    discountedPrice: Double?,
    discountText: String?,
    rating: Double,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF3F4F6))
                    )
                }
                if (discountText != null) {
                    DiscountBadge(
                        text = discountText,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriceTag(price = price, discountedPrice = discountedPrice)
                    if (rating > 0) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFFBBF24)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

// ── Active match card (horizontal scroll, above sports) ──
@Composable
private fun ActiveMatchCard(match: Match, onClick: () -> Unit) {
    val visual = sportVisuals[match.sportType]
    val statusColor = when (match.status) {
        MatchStatus.OPEN -> Color(0xFF16A34A)        // green
        MatchStatus.FULL -> Color(0xFFF59E0B)         // amber
        MatchStatus.IN_PROGRESS -> Color(0xFF2563EB)  // blue
        else -> TextSecondary
    }
    val statusLabel = when (match.status) {
        MatchStatus.OPEN -> "Open"
        MatchStatus.FULL -> "Full"
        MatchStatus.IN_PROGRESS -> "Live"
        else -> match.status.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    Card(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Gradient header with sport emoji + status chip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                visual?.gradientStart ?: Color(0xFF6366F1),
                                visual?.gradientEnd ?: Color(0xFF4F46E5)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = visual?.emoji ?: "⚽",
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                // Status chip
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusColor.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Date + time
                Text(
                    text = "${match.matchDate} · ${match.displayTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                // Location
                Text(
                    text = match.displayLocation,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Players + cost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Player count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👥 ${match.currentPlayers}/${match.maxPlayers}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // Cost
                    Text(
                        text = match.displayCost,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (match.isFree) Color(0xFF16A34A) else TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Find a Match CTA (dark card with gold text) ──
@Composable
private fun FindMatchCta(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NavBarBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Find a Match",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Join pickup games or create your own",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go",
                tint = GoldAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ── "Want to see more sports?" bottom CTA ──
@Composable
private fun AddSportCta(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Want to see more sports?",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Add another to your follow list and we'll re-tune the feed.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavBarBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "+ Add sport",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Previews ──

@Preview(showBackground = true)
@Composable
private fun PlayerHomeScreenPreview() {
    val sampleSports = listOf(
        Sport(id = 1, sportType = SportType.FOOTBALL, name = "Football"),
        Sport(id = 2, sportType = SportType.TENNIS, name = "Tennis"),
        Sport(id = 3, sportType = SportType.PADDLE, name = "Paddle"),
        Sport(id = 4, sportType = SportType.YOGA, name = "Yoga"),
    )
    val sampleVenues = listOf(
        Venue(id = 1, name = "Skopje Padel Club", sportType = SportType.PADDLE, pricePerHour = 800.0, address = "Main St", city = "Skopje"),
        Venue(id = 2, name = "Vodno Tennis Center", sportType = SportType.TENNIS, pricePerHour = 600.0, address = "Vodno", city = "Skopje"),
    )
    val sampleCoaches = listOf(
        Coach(id = 1, name = "Coach Goran", sportType = SportType.PADDLE, pricePerHour = 1200.0, specialization = "Paddle pro"),
    )
    val previewState = PlayerHomeUiState(
        user = User(
            id = 1,
            displayName = "Bojan",
            email = "bojan@test.com",
            interestedSports = listOf(SportType.FOOTBALL, SportType.TENNIS, SportType.PADDLE, SportType.YOGA)
        ),
        sports = sampleSports,
        allVenues = sampleVenues,
        allCoaches = sampleCoaches,
        topDealVenues = emptyList(),
        topDealCoaches = emptyList(),
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
            onNavigateToSearch = {},
            onNavigateToFavorites = {},
            onFindMatch = {},
            onMatchClick = {},
            onBrowseAllSports = {},
            notificationCount = previewState.notificationCount
        )
    }
}
