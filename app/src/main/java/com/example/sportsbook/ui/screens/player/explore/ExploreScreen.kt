package com.example.sportsbook.ui.screens.player.explore

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkSurfaceLight
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.DarkTextTertiary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.SportsBookTheme

// ── Placeholder data models ───────────────────────────────────────────────────

private data class VenueItem(
    val id: Long,
    val name: String,
    val location: String,
    val sport: String,
    val price: String,
    val rating: Double,
    val emoji: String,
    val discount: String? = null,
)

private data class CoachItem(
    val id: Long,
    val name: String,
    val sport: String,
    val rating: Double,
    val price: String,
    val initial: String,
    val avatarColor: Color,
)

private data class PopularItem(
    val id: Long,
    val name: String,
    val detail: String,
    val price: String,
    val emoji: String,
)

private val sampleVenues = listOf(
    VenueItem(1, "Ace Sports Center", "Skopje, Center", "Tennis, Paddle", "50 MKD/hr", 4.8, "🎾", discount = "20% OFF"),
    VenueItem(2, "Downtown Tennis Club", "Skopje, Aerodrom", "Tennis", "45 MKD/hr", 4.7, "🎾"),
    VenueItem(3, "Metro Arena", "Skopje, Karpos", "Basketball, Volleyball", "60 MKD/hr", 4.6, "🏀", discount = "15% OFF"),
    VenueItem(4, "Sunset Courts", "Skopje, Gjorce", "Tennis, Paddle", "40 MKD/hr", 4.5, "🏸"),
)

private val sampleCoaches = listOf(
    CoachItem(1, "Coach Mario", "Tennis", 4.9, "80 MKD/hr", "M", Color(0xFF4CAF50)),
    CoachItem(2, "Coach Elena", "Yoga", 4.8, "60 MKD/hr", "E", Color(0xFFAB47BC)),
    CoachItem(3, "Coach Alex", "Basketball", 4.7, "70 MKD/hr", "A", Color(0xFFFF6B35)),
    CoachItem(4, "Coach Sofia", "Paddle", 4.9, "65 MKD/hr", "S", Color(0xFF2196F3)),
)

private val samplePopular = listOf(
    PopularItem(1, "Ace Sports Center", "Tennis • 4.8 ⭐ • Skopje", "50 MKD/hr", "🎾"),
    PopularItem(2, "Metro Arena", "Basketball • 4.6 ⭐ • Karpos", "60 MKD/hr", "🏀"),
    PopularItem(3, "Sunset Courts", "Paddle • 4.5 ⭐ • Gjorce", "40 MKD/hr", "🏸"),
    PopularItem(4, "City Swim Club", "Swimming • 4.4 ⭐ • Center", "55 MKD/hr", "🏊"),
    PopularItem(5, "Power Gym", "Gym • 4.3 ⭐ • Aerodrom", "35 MKD/hr", "🏋️"),
)

// ── Category pill data ────────────────────────────────────────────────────────

private val categoryPills = listOf(
    "🔥 All",
    "🏟️ Venues",
    "🏋️ Coaches",
    "🏷️ Deals",
    "⭐ Top Rated",
)

// ── Sport grid data ───────────────────────────────────────────────────────────

private data class SportItem(val emoji: String, val name: String, val color: Color)

private val sportGrid = listOf(
    SportItem("🏀", "Basketball", Color(0xFFFF6B35)),
    SportItem("⚽", "Football", Color(0xFF4CAF50)),
    SportItem("🎾", "Tennis", Color(0xFF2196F3)),
    SportItem("🏸", "Paddle", Color(0xFFAB47BC)),
    SportItem("🏐", "Volleyball", Color(0xFFE53935)),
    SportItem("🏊", "Swimming", Color(0xFF00ACC1)),
    SportItem("🏋️", "Gym", Color(0xFFFF5722)),
    SportItem("➕", "More", Color(0xFF607D8B)),
)

// ── Scatter-dot positions for map banner ─────────────────────────────────────

private data class MapDot(val xFraction: Float, val yFraction: Float, val size: Int, val alpha: Float)

private val mapDots = listOf(
    MapDot(0.15f, 0.30f, 6, 0.9f),
    MapDot(0.30f, 0.55f, 4, 0.6f),
    MapDot(0.48f, 0.25f, 7, 1.0f),
    MapDot(0.62f, 0.60f, 5, 0.7f),
    MapDot(0.75f, 0.35f, 6, 0.85f),
    MapDot(0.88f, 0.50f, 4, 0.5f),
    MapDot(0.22f, 0.70f, 5, 0.65f),
    MapDot(0.55f, 0.75f, 4, 0.55f),
    MapDot(0.40f, 0.45f, 8, 1.0f),
)

// ── Root composable ───────────────────────────────────────────────────────────

/**
 * Explore tab — browse sports categories, popular venues, top coaches.
 * Matches mockup P02-Explore v3.
 */
@Composable
fun ExploreScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onSportClick: (String) -> Unit = {},
    onVenueClick: (Long) -> Unit = {},
    onCoachClick: (Long) -> Unit = {},
    onSeeAllVenues: () -> Unit = {},
    onSeeAllCoaches: () -> Unit = {},
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCategory = uiState.selectedCategory

    // Map domain models to local display models
    val venueItems = uiState.venues.map { v ->
        VenueItem(
            id = v.id,
            name = v.name,
            location = listOfNotNull(v.city, v.country).joinToString(", ").ifBlank { v.address },
            sport = v.sportType.displayName,
            price = "${v.pricePerHour.toInt()} MKD/hr",
            rating = v.avgRating,
            emoji = sportEmoji(v.sportType.name),
            discount = v.activeDiscount?.let {
                when {
                    it.discountPercent != null -> "${it.discountPercent.toInt()}% OFF"
                    it.discountAmount != null -> "${it.discountAmount.toInt()} OFF"
                    else -> null
                }
            }
        )
    }
    val coachItems = uiState.coaches.map { c ->
        CoachItem(
            id = c.id,
            name = c.name,
            sport = c.sportType.displayName,
            rating = c.avgRating,
            price = "${c.pricePerHour.toInt()} MKD/hr",
            initial = c.name.firstOrNull()?.uppercase() ?: "?",
            avatarColor = sportColor(c.sportType.name),
        )
    }
    val popularItems = uiState.venues.sortedByDescending { it.avgRating }.take(5).map { v ->
        PopularItem(
            id = v.id,
            name = v.name,
            detail = "${v.sportType.displayName} • ${v.avgRating} ⭐ • ${v.city ?: v.address}",
            price = "${v.pricePerHour.toInt()} MKD/hr",
            emoji = sportEmoji(v.sportType.name),
        )
    }

    // Category filtering: 0=All, 1=Venues, 2=Coaches, 3=Deals, 4=TopRated
    val showVenues = selectedCategory == 0 || selectedCategory == 1 || selectedCategory == 4
    val showCoaches = selectedCategory == 0 || selectedCategory == 2
    val showDeals = selectedCategory == 0 || selectedCategory == 3
    val showPopular = selectedCategory == 0 || selectedCategory == 4

    val filteredVenues = when (selectedCategory) {
        3 -> venueItems.filter { it.discount != null } // Deals only
        4 -> venueItems.sortedByDescending { it.rating } // Top Rated
        else -> venueItems
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // 1. Top Bar
        item {
            ExploreTopBar()
        }

        // 2. Search Bar
        item {
            ExploreSearchBar(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onClick = onNavigateToSearch,
            )
        }

        // 3. Category Pills
        item {
            CategoryPillsRow(
                pills = categoryPills,
                selectedIndex = selectedCategory,
                onSelect = { viewModel.selectCategory(it) },
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
            )
        }

        // 4. Map Banner
        item {
            MapBanner(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                onClick = onNavigateToMap,
            )
        }

        // 5. Browse by Sport (always visible)
        if (selectedCategory == 0) {
            item {
                BrowzeBySportSection(
                    modifier = Modifier.padding(top = 8.dp),
                    onSportClick = onSportClick,
                )
            }
        }

        // 6. Top Deals Banner
        if (showDeals) {
            item {
                TopDealsBanner(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }

        // 7. Featured Venues
        if (showVenues) {
            item {
                FeaturedVenuesSection(
                    venues = filteredVenues,
                    onVenueClick = onVenueClick,
                    onSeeAll = onSeeAllVenues,
                )
            }
        }

        // 8. Top Coaches
        if (showCoaches) {
            item {
                TopCoachesSection(
                    coaches = coachItems,
                    onCoachClick = onCoachClick,
                    onSeeAll = onSeeAllCoaches,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        // 9. Popular Near You
        if (showPopular) {
            item {
                PopularNearYouSection(
                    items = popularItems,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

private fun sportEmoji(sportName: String): String = when (sportName.uppercase()) {
    "BASKETBALL" -> "🏀"
    "FOOTBALL" -> "⚽"
    "TENNIS" -> "🎾"
    "PADDLE" -> "🏓"
    "VOLLEYBALL" -> "🏐"
    "BADMINTON" -> "🏸"
    "BOXING" -> "🥊"
    "RUNNING" -> "🏃"
    "SWIMMING" -> "🏊"
    else -> "🏟️"
}

private fun sportColor(sportName: String): Color = when (sportName.uppercase()) {
    "BASKETBALL" -> Color(0xFFFF6B35)
    "FOOTBALL" -> Color(0xFF4CAF50)
    "TENNIS" -> Color(0xFF2196F3)
    "PADDLE" -> Color(0xFFAB47BC)
    "VOLLEYBALL" -> Color(0xFFE53935)
    "SWIMMING" -> Color(0xFF00ACC1)
    else -> Color(0xFF607D8B)
}

// ── Section: Top Bar ─────────────────────────────────────────────────────────

@Composable
private fun ExploreTopBar(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Text(
            text = "Explore",
            fontSize = 28.sp,
            fontWeight = FontWeight(800),
            color = DarkTextPrimary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Discover venues, coaches & deals near you",
            fontSize = 13.sp,
            color = DarkTextPrimary.copy(alpha = 0.4f),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun ExploreTopBarPreview() {
    SportsBookTheme { ExploreTopBar() }
}

// ── Section: Search Bar ───────────────────────────────────────────────────────

@Composable
private fun ExploreSearchBar(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Search venues, coaches, sports...",
            fontSize = 14.sp,
            color = DarkTextTertiary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun ExploreSearchBarPreview() {
    SportsBookTheme { ExploreSearchBar(onClick = {}, modifier = Modifier.padding(16.dp)) }
}

// ── Section: Category Pills ───────────────────────────────────────────────────

@Composable
private fun CategoryPillsRow(
    pills: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(pills) { index, label ->
            val active = index == selectedIndex
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(
                        if (active) GreenAccent.copy(alpha = 0.15f)
                        else Color.White.copy(alpha = 0.07f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (active) GreenAccent.copy(alpha = 0.30f)
                        else Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(17.dp),
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 14.dp),
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) GreenAccent else Color.White.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CategoryPillsRowPreview() {
    SportsBookTheme {
        CategoryPillsRow(
            pills = categoryPills,
            selectedIndex = 0,
            onSelect = {},
        )
    }
}

// ── Section: Map Banner ───────────────────────────────────────────────────────

@Composable
private fun MapBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1A2A1A), Color(0xFF1A2030)),
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        // Scattered map dots
        mapDots.forEach { dot ->
            val dotColor = GreenAccent.copy(alpha = dot.alpha)
            // Glow halo
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = (dot.xFraction * 320).dp,
                        y = (dot.yFraction * 140).dp,
                    )
                    .size((dot.size * 3).dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = dot.alpha * 0.18f)),
            )
            // Core dot
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = (dot.xFraction * 320 + dot.size).dp,
                        y = (dot.yFraction * 140 + dot.size).dp,
                    )
                    .size(dot.size.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }

        // Bottom content overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC1A2030)),
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "14 venues near Skopje",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to explore on map",
                    fontSize = 12.sp,
                    color = DarkTextPrimary.copy(alpha = 0.5f),
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(GreenAccent.copy(alpha = 0.20f))
                    .border(1.dp, GreenAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    text = "📍 Open Map",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenAccent,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun MapBannerPreview() {
    SportsBookTheme { MapBanner(onClick = {}, modifier = Modifier.padding(16.dp)) }
}

// ── Section: Browse by Sport ──────────────────────────────────────────────────

@Composable
private fun BrowzeBySportSection(
    onSportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Browse by Sport",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextPrimary,
        )
        Spacer(modifier = Modifier.height(14.dp))

        // 4-column grid, 2 rows
        val rows = sportGrid.chunked(4)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowItems.forEach { sport ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSportClick(sport.name.lowercase()) }
                            .padding(vertical = 8.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(sport.color.copy(alpha = 0.12f)),
                        ) {
                            Text(sport.emoji, fontSize = 26.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = sport.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = DarkTextSecondary,
                            maxLines = 1,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun BrowzeBySportSectionPreview() {
    SportsBookTheme { BrowzeBySportSection(onSportClick = {}) }
}

// ── Section: Top Deals Banner ─────────────────────────────────────────────────

@Composable
private fun TopDealsBanner(modifier: Modifier = Modifier) {
    val pink = Color(0xFFE91E63)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2D0F1A), Color(0xFF2D1A0A)),
                )
            )
            .border(1.dp, pink.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(pink.copy(alpha = 0.18f)),
        ) {
            Text("🔥", fontSize = 26.sp)
        }

        // Text block
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Top Deals This Week",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Up to 40% off at 6 venues near you",
                fontSize = 12.sp,
                color = DarkTextPrimary.copy(alpha = 0.55f),
            )
        }

        // CTA
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(pink.copy(alpha = 0.18f))
                .border(1.dp, pink.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                text = "View",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = pink,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun TopDealsBannerPreview() {
    SportsBookTheme { TopDealsBanner(modifier = Modifier.padding(16.dp)) }
}

// ── Section: Featured Venues ──────────────────────────────────────────────────

@Composable
private fun FeaturedVenuesSection(
    venues: List<VenueItem>,
    onVenueClick: (Long) -> Unit,
    onSeeAll: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = "Featured Venues",
            actionLabel = "See All",
            onAction = onSeeAll,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (venues.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("No featured venues yet", fontSize = 13.sp, color = DarkTextSecondary)
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(venues) { _, venue ->
                    FeaturedVenueCard(
                        venue = venue,
                        onClick = { onVenueClick(venue.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedVenueCard(venue: VenueItem, onClick: () -> Unit) {
    val blue = Color(0xFF64B5F6)
    val orange = Color(0xFFFF9800)

    Column(
        modifier = Modifier
            .width(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        // Image area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkSurfaceLight, DarkSurface),
                    )
                ),
        ) {
            // Sport emoji centered
            Text(
                text = venue.emoji,
                fontSize = 42.sp,
                modifier = Modifier.align(Alignment.Center),
            )
            // Discount badge top-left
            if (venue.discount != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(orange.copy(alpha = 0.90f))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = venue.discount,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
            // Rating badge top-right
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC1E1E1E))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "⭐ ${venue.rating}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextPrimary,
                )
            }
        }

        // Card body
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = venue.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "📍 ${venue.location}",
                fontSize = 12.sp,
                color = DarkTextSecondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Tags row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TagChip(label = venue.sport.split(",").first().trim(), color = GreenAccent)
                TagChip(label = venue.price, color = blue)
                if (venue.discount != null) {
                    TagChip(label = "Deal", color = orange)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun FeaturedVenuesSectionPreview() {
    SportsBookTheme {
        FeaturedVenuesSection(venues = sampleVenues, onVenueClick = {})
    }
}

// ── Section: Top Coaches ──────────────────────────────────────────────────────

@Composable
private fun TopCoachesSection(
    coaches: List<CoachItem>,
    onCoachClick: (Long) -> Unit,
    onSeeAll: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val blue = Color(0xFF64B5F6)

    Column(modifier = modifier) {
        SectionHeader(
            title = "Top Coaches",
            actionLabel = "See All",
            onAction = onSeeAll,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (coaches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("No coaches available yet", fontSize = 13.sp, color = DarkTextSecondary)
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(coaches) { _, coach ->
                    CoachCard(
                        coach = coach,
                        priceColor = blue,
                        onClick = { onCoachClick(coach.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CoachCard(coach: CoachItem, priceColor: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        // Avatar circle with initial
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(coach.avatarColor.copy(alpha = 0.18f))
                .border(2.dp, coach.avatarColor.copy(alpha = 0.35f), CircleShape),
        ) {
            Text(
                text = coach.initial,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = coach.avatarColor,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = coach.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextPrimary,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = coach.sport,
            fontSize = 12.sp,
            color = DarkTextSecondary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "⭐ ${coach.rating}",
            fontSize = 12.sp,
            color = DarkTextPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Price badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(priceColor.copy(alpha = 0.12f))
                .border(1.dp, priceColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = coach.price,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = priceColor,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun TopCoachesSectionPreview() {
    SportsBookTheme {
        TopCoachesSection(coaches = sampleCoaches, onCoachClick = {})
    }
}

// ── Section: Popular Near You ─────────────────────────────────────────────────

@Composable
private fun PopularNearYouSection(
    items: List<PopularItem>,
    modifier: Modifier = Modifier,
) {
    val gold = Color(0xFFFFD700)
    val silver = Color(0xFFC0C0C0)
    val bronze = Color(0xFFCD7F32)

    Column(modifier = modifier) {
        SectionHeader(
            title = "🔥 Popular Near You",
            actionLabel = "See All",
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("No popular venues yet", fontSize = 13.sp, color = DarkTextSecondary)
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items.forEachIndexed { index, item ->
                    val rank = index + 1
                    val rankColor = when (rank) {
                        1 -> gold
                        2 -> silver
                        3 -> bronze
                        else -> DarkTextTertiary
                    }
                    PopularRow(
                        rank = rank,
                        rankColor = rankColor,
                        item = item,
                    )
                }
            }
        }
    }
}

@Composable
private fun PopularRow(rank: Int, rankColor: Color, item: PopularItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Rank number
        Text(
            text = "$rank",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = rankColor,
            modifier = Modifier.width(20.dp),
        )

        // Thumbnail
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f)),
        ) {
            Text(item.emoji, fontSize = 26.sp)
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.detail,
                fontSize = 12.sp,
                color = DarkTextSecondary,
                maxLines = 1,
            )
        }

        // Price
        Text(
            text = item.price,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = GreenAccent,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PopularNearYouSectionPreview() {
    SportsBookTheme {
        PopularNearYouSection(items = samplePopular)
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextPrimary,
        )
        Text(
            text = actionLabel,
            fontSize = 13.sp,
            color = Color(0xFF64B5F6),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onAction() },
        )
    }
}

@Composable
private fun TagChip(label: String, color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

// ── Root Preview ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212, showSystemUi = true)
@Composable
private fun ExploreScreenPreview() {
    SportsBookTheme {
        ExploreScreen()
    }
}
