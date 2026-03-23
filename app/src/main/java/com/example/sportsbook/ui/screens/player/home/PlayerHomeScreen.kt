package com.example.sportsbook.ui.screens.player.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Sport
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.ui.common.DiscountBadge
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.PriceTag
import com.example.sportsbook.ui.theme.Navy600
import com.example.sportsbook.ui.theme.Navy700
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite
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

// ── Sport visual mapping ──

private data class SportVisual(val emoji: String, val color: Color, val tagline: String)

private val sportVisuals = mapOf(
    SportType.BASKETBALL to SportVisual("\uD83C\uDFC0", SportBasketball, "Courts & coaching"),
    SportType.FOOTBALL to SportVisual("\u26BD", SportFootball, "Pitches & training"),
    SportType.TENNIS to SportVisual("\uD83C\uDFBE", SportTennis, "Courts & lessons"),
    SportType.PADDLE to SportVisual("\uD83C\uDFD3", SportPaddle, "Book sessions"),
    SportType.VOLLEYBALL to SportVisual("\uD83C\uDFD0", SportVolleyball, "Courts & teams"),
    SportType.SWIMMING to SportVisual("\uD83C\uDFCA", SportSwimming, "Pools & coaching"),
    SportType.BOXING to SportVisual("\uD83E\uDD4A", SportBoxing, "Train with pros"),
    SportType.MMA to SportVisual("\uD83E\uDD4B", SportMMA, "Combat training"),
    SportType.YOGA to SportVisual("\uD83E\uDDD8", SportYoga, "Studios & classes"),
    SportType.PILATES to SportVisual("\uD83E\uDD38", SportPilates, "Book sessions"),
    SportType.CROSSFIT to SportVisual("\uD83C\uDFCB\uFE0F", SportCrossfit, "Find boxes"),
    SportType.RUNNING to SportVisual("\uD83C\uDFC3", SportRunning, "Groups & coaches"),
    SportType.CYCLING to SportVisual("\uD83D\uDEB4", SportCycling, "Routes & clubs"),
    SportType.GOLF to SportVisual("\u26F3", SportGolf, "Book tee times"),
    SportType.BADMINTON to SportVisual("\uD83C\uDFF8", SportBadminton, "Reserve courts"),
    SportType.TABLE_TENNIS to SportVisual("\uD83C\uDFD3", SportTableTennis, "Book tables"),
    SportType.HANDBALL to SportVisual("\uD83E\uDD3E", SportHandball, "Courts & teams"),
    SportType.BASEBALL to SportVisual("\u26BE", SportBaseball, "Fields & coaching"),
    SportType.CRICKET to SportVisual("\uD83C\uDFCF", SportCricket, "Pitches & nets"),
)

// Sports hidden from the category grid (still visible in filter chips)
private val hiddenFromGrid = setOf(
    SportType.CRICKET,
    SportType.HANDBALL,
    SportType.BASEBALL,
    SportType.SWIMMING,
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
    onNavigateToSearch: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToFriends: () -> Unit = {},
    viewModel: PlayerHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            onNavigateToFriends = onNavigateToFriends,
            onFindMatch = onFindMatch,
            onSelectSport = viewModel::selectSport
        )
    }
}

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
    onNavigateToFriends: () -> Unit,
    onFindMatch: () -> Unit,
    onSelectSport: (SportType?) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Greeting Header ──
        item {
            GreetingHeader(
                userName = uiState.user?.displayName?.split(" ")?.firstOrNull() ?: "Player",
                userPhotoUrl = uiState.user?.photoUrl,
                onProfileClick = onNavigateToProfile,
                onNotificationClick = onNavigateToNotifications,
                onSearchClick = onNavigateToSearch,
                onFavoritesClick = onNavigateToFavorites
            )
        }

        // ── Filter Chips ──
        item {
            FilterChipsRow(
                sports = uiState.sports,
                selectedSportType = uiState.selectedSportType,
                onSelectSport = onSelectSport
            )
        }

        // ── Sport Category Grid (only when "All" selected) ──
        if (uiState.selectedSportType == null && uiState.sports.isNotEmpty()) {
            val gridSports = uiState.sports.filter { it.sportType !in hiddenFromGrid }
            val rows = gridSports.chunked(2)
            items(rows.size) { index ->
                val pair = rows[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { sport ->
                        SportCategoryCard(
                            sport = sport,
                            onClick = { onSportClick(sport.sportType.name.lowercase()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // ── Top Deals ──
        val dealVenues = if (uiState.selectedSportType != null)
            uiState.topDealVenues.filter { it.sportType == uiState.selectedSportType }
        else uiState.topDealVenues

        val dealCoaches = if (uiState.selectedSportType != null)
            uiState.topDealCoaches.filter { it.sportType == uiState.selectedSportType }
        else uiState.topDealCoaches

        if (dealVenues.isNotEmpty() || dealCoaches.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Top Deals")
            }
            item {
                DealsRow(
                    venues = dealVenues,
                    coaches = dealCoaches,
                    onVenueClick = onVenueClick,
                    onCoachClick = onCoachClick
                )
            }
        }

        // ── Find a Match CTA ──
        item {
            Spacer(modifier = Modifier.height(8.dp))
            FindMatchCard(onClick = onFindMatch)
        }

        // ── Popular Venues ──
        val venues = uiState.filteredVenues
        if (venues.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Popular Venues")
            }
            item {
                HorizontalCardRow(
                    items = venues,
                    getId = { it.id },
                    getName = { it.name },
                    getSubtitle = { it.city ?: it.address },
                    getImageUrl = { it.primaryImageUrl },
                    getPrice = { it.pricePerHour },
                    getDiscountedPrice = { it.discountedPrice },
                    getRating = { it.avgRating },
                    onClick = { onVenueClick(it.id) }
                )
            }
        }

        // ── Top Coaches ──
        val coaches = uiState.filteredCoaches
        if (coaches.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Top Coaches")
            }
            item {
                HorizontalCardRow(
                    items = coaches,
                    getId = { it.id },
                    getName = { it.name },
                    getSubtitle = { it.specialization ?: it.sportType.displayName },
                    getImageUrl = { it.primaryImageUrl },
                    getPrice = { it.pricePerHour },
                    getDiscountedPrice = { it.discountedPrice },
                    getRating = { it.avgRating },
                    onClick = { onCoachClick(it.id) }
                )
            }
        }
    }

        // ── Friends FAB (bottom-right) ──
        FloatingActionButton(
            onClick = onNavigateToFriends,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            containerColor = USOpenGold,
            contentColor = Navy900,
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = "Friends",
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

// ── Greeting Header (Spotify-style) ──

@Composable
private fun GreetingHeader(
    userName: String,
    userPhotoUrl: String?,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile avatar
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Navy600)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            if (userPhotoUrl != null) {
                AsyncImage(
                    model = userPhotoUrl,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = userName.firstOrNull()?.uppercase() ?: "P",
                    style = MaterialTheme.typography.titleMedium,
                    color = USOpenGold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Greeting
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = getGreeting(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                color = WarmWhite
            )
        }

        // Action icons
        IconButton(onClick = onSearchClick) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = WarmWhite)
        }
        IconButton(onClick = onFavoritesClick) {
            Icon(Icons.Default.Favorite, contentDescription = "Favorites", tint = WarmWhite)
        }
        IconButton(onClick = onNotificationClick) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = WarmWhite)
        }
    }
}

// ── Filter Chips ──

@Composable
private fun FilterChipsRow(
    sports: List<Sport>,
    selectedSportType: SportType?,
    onSelectSport: (SportType?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedSportType == null,
                onClick = { onSelectSport(null) },
                label = {
                    Text(
                        "All",
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = USOpenGold,
                    selectedLabelColor = Navy900,
                    containerColor = Navy700,
                    labelColor = WarmWhite
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
        items(sports) { sport ->
            val visual = sportVisuals[sport.sportType]
            FilterChip(
                selected = selectedSportType == sport.sportType,
                onClick = { onSelectSport(sport.sportType) },
                label = {
                    Text(
                        "${visual?.emoji ?: ""} ${sport.sportType.displayName}",
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = USOpenGold,
                    selectedLabelColor = Navy900,
                    containerColor = Navy700,
                    labelColor = WarmWhite
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

// ── Sport Category Card ──

@Composable
private fun SportCategoryCard(
    sport: Sport,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visual = sportVisuals[sport.sportType]
        ?: SportVisual("\uD83C\uDFC0", SportBasketball, "Book now")

    Card(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            visual.color.copy(alpha = 0.35f),
                            Navy700
                        )
                    )
                )
        ) {
            // Large faded emoji in background (top-right)
            Text(
                text = visual.emoji,
                fontSize = 52.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp, top = 2.dp),
                color = Color.White.copy(alpha = 0.12f)
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Emoji + name row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = visual.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sport.sportType.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WarmWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Tagline
                Text(
                    text = visual.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = visual.color.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ── Section Header ──

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = WarmWhite,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

// ── Top Deals Horizontal Row ──

@Composable
private fun DealsRow(
    venues: List<Venue>,
    coaches: List<Coach>,
    onVenueClick: (Long) -> Unit,
    onCoachClick: (Long) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(venues, key = { "v-${it.id}" }) { venue ->
            DealCard(
                name = venue.name,
                subtitle = venue.city ?: venue.address,
                imageUrl = venue.primaryImageUrl,
                price = venue.pricePerHour,
                discountedPrice = venue.discountedPrice,
                discountText = venue.activeDiscount?.displayValue,
                sportColor = sportVisuals[venue.sportType]?.color ?: USOpenGold,
                onClick = { onVenueClick(venue.id) }
            )
        }
        items(coaches, key = { "c-${it.id}" }) { coach ->
            DealCard(
                name = coach.name,
                subtitle = coach.specialization ?: "",
                imageUrl = coach.primaryImageUrl,
                price = coach.pricePerHour,
                discountedPrice = coach.discountedPrice,
                discountText = coach.activeDiscount?.displayValue,
                sportColor = sportVisuals[coach.sportType]?.color ?: USOpenGold,
                onClick = { onCoachClick(coach.id) }
            )
        }
    }
}

@Composable
private fun DealCard(
    name: String,
    subtitle: String,
    imageUrl: String?,
    price: Double,
    discountedPrice: Double?,
    discountText: String?,
    sportColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Navy700),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(sportColor.copy(alpha = 0.4f), Navy700)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SportsBasketball,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = sportColor.copy(alpha = 0.6f)
                        )
                    }
                }

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Navy700)
                            )
                        )
                )

                // Discount badge
                if (discountText != null) {
                    DiscountBadge(
                        text = discountText,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
            }

            // Info section
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    color = WarmWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                PriceTag(price = price, discountedPrice = discountedPrice)
            }
        }
    }
}

// ── Find a Match CTA ──

@Composable
private fun FindMatchCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            USOpenGold.copy(alpha = 0.15f),
                            Navy600
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Find a Match",
                        style = MaterialTheme.typography.titleLarge,
                        color = USOpenGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Join pickup games or create your own",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmWhite.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Go",
                    tint = USOpenGold,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ── Generic Horizontal Card Row (for Venues / Coaches) ──

@Composable
private fun <T> HorizontalCardRow(
    items: List<T>,
    getId: (T) -> Long,
    getName: (T) -> String,
    getSubtitle: (T) -> String,
    getImageUrl: (T) -> String?,
    getPrice: (T) -> Double,
    getDiscountedPrice: (T) -> Double?,
    getRating: (T) -> Double,
    onClick: (T) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { getId(it) }) { item ->
            ItemCard(
                name = getName(item),
                subtitle = getSubtitle(item),
                imageUrl = getImageUrl(item),
                price = getPrice(item),
                discountedPrice = getDiscountedPrice(item),
                rating = getRating(item),
                onClick = { onClick(item) }
            )
        }
    }
}

@Composable
private fun ItemCard(
    name: String,
    subtitle: String,
    imageUrl: String?,
    price: Double,
    discountedPrice: Double?,
    rating: Double,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Navy700),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Image
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
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Navy600),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SportsBasketball,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = USOpenGold.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    color = WarmWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriceTag(price = price, discountedPrice = discountedPrice)
                    if (rating > 0) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = USOpenGold
                        )
                        Text(
                            text = String.format("%.1f", rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = USOpenGold,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Previews ──

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun PlayerHomeScreenPreview() {
    SportsBookTheme {
        PlayerHomeScreen(
            onSportClick = {},
            onVenueClick = {},
            onCoachClick = {},
            onNavigateToProfile = {},
            onNavigateToNotifications = {},
            onNavigateToSettings = {},
            onSignOut = {},
            onFindMatch = {},
            onNavigateToSearch = {},
            onNavigateToFavorites = {},
            onNavigateToFriends = {}
        )
    }
}
