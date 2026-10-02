package com.example.sportsbook.ui.screens.player.venue

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Discount
import com.example.sportsbook.domain.model.Review
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.model.VenueEquipment
import com.example.sportsbook.domain.model.VenueImage
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.BlueAccent
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkNavBar
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.DarkTextTertiary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import com.example.sportsbook.ui.theme.SportsBookTheme

import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.TimeSlot
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

// ─── Equipment emoji map ──────────────────────────────────────────────────────
private fun equipmentEmoji(name: String): String {
    val lower = name.lowercase()
    return when {
        "locker" in lower -> "🔒"
        "parking" in lower -> "🅿️"
        "wifi" in lower || "wi-fi" in lower -> "📶"
        "shower" in lower -> "🚿"
        "changing" in lower -> "👔"
        "ball" in lower -> "🏀"
        "net" in lower -> "🥅"
        "light" in lower -> "💡"
        "ac" in lower || "air" in lower -> "❄️"
        "towel" in lower -> "🧺"
        "water" in lower -> "💧"
        "first aid" in lower || "medical" in lower -> "🩺"
        "cafeteria" in lower || "cafe" in lower || "snack" in lower -> "☕"
        "restroom" in lower || "toilet" in lower -> "🚻"
        "wheelchair" in lower || "accessible" in lower -> "♿"
        else -> "✅"
    }
}

// ─── Sport emoji helper ───────────────────────────────────────────────────────
private fun SportType.emoji(): String = when (this) {
    SportType.BASKETBALL -> "🏀"
    SportType.FOOTBALL -> "⚽"
    SportType.TENNIS -> "🎾"
    SportType.PADDLE -> "🏓"
    SportType.VOLLEYBALL -> "🏐"
    SportType.SWIMMING -> "🏊"
    SportType.BOXING -> "🥊"
    SportType.MMA -> "🥋"
    SportType.YOGA -> "🧘"
    SportType.PILATES -> "🤸"
    SportType.CROSSFIT -> "💪"
    SportType.RUNNING -> "🏃"
    SportType.CYCLING -> "🚴"
    SportType.GOLF -> "⛳"
    SportType.BADMINTON -> "🏸"
    SportType.TABLE_TENNIS -> "🏓"
    SportType.HANDBALL -> "🤾"
    SportType.BASEBALL -> "⚾"
    SportType.CRICKET -> "🏏"
}

// ─── Avatar background colors (cycled by index) ──────────────────────────────
private val AVATAR_COLORS = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800),
    Color(0xFFAB47BC), Color(0xFFE91E63), Color(0xFF00BCD4)
)

// ═════════════════════════════════════════════════════════════════════════════
// PUBLIC ENTRY POINT
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun VenueDetailScreen(
    venueId: Long,
    onBookClick: () -> Unit,
    onBrowseLobbies: () -> Unit,
    onBack: () -> Unit,
    viewModel: VenueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(venueId) {
        viewModel.loadVenueDetail(venueId)
    }

    when {
        uiState.isLoading -> LoadingIndicator(modifier = Modifier.fillMaxSize())
        uiState.error != null -> ErrorView(
            message = uiState.error!!,
            onRetry = viewModel::retry,
            modifier = Modifier.fillMaxSize()
        )
        else -> {
            val venue = uiState.selectedVenue
            if (venue != null) {
                VenueDetailScaffold(
                    venue = venue,
                    reviews = uiState.reviews,
                    timeSlots = uiState.timeSlots,
                    venueMatches = uiState.venueMatches,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = viewModel::selectDate,
                    onBookClick = onBookClick,
                    onBrowseLobbies = onBrowseLobbies,
                    onBack = onBack
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SCAFFOLD + BOTTOM BAR
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun VenueDetailScaffold(
    venue: Venue,
    reviews: List<Review>,
    timeSlots: List<TimeSlot>,
    venueMatches: List<Match>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onBookClick: () -> Unit,
    onBrowseLobbies: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        VenueDetailContent(
            venue = venue,
            reviews = reviews,
            timeSlots = timeSlots,
            venueMatches = venueMatches,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            onBack = onBack,
            onBookClick = onBookClick,
            onBrowseLobbies = onBrowseLobbies,
            modifier = Modifier.padding(bottom = 80.dp)
        )
        StickyBookingBar(
            venue = venue,
            onBookClick = onBookClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun StickyBookingBar(venue: Venue, onBookClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkNavBar)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Price section
            Column {
                Text(
                    text = "From",
                    fontSize = 11.sp,
                    color = DarkTextTertiary,
                    fontWeight = FontWeight.Normal
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (venue.discountedPrice != null) {
                        Text(
                            text = "€${venue.pricePerHour.toInt()}",
                            fontSize = 14.sp,
                            color = DarkTextSecondary,
                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = "€${(venue.discountedPrice ?: venue.pricePerHour).toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenAccent
                    )
                    Text(
                        text = " /hour",
                        fontSize = 13.sp,
                        color = DarkTextSecondary
                    )
                }
            }

            // Book button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onBookClick)
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Book Now",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(800),
                    color = Color.White
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// MAIN CONTENT — LazyColumn with all sections
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun VenueDetailContent(
    venue: Venue,
    reviews: List<Review>,
    timeSlots: List<TimeSlot>,
    venueMatches: List<Match>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onBack: () -> Unit,
    onBookClick: () -> Unit,
    onBrowseLobbies: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // 1. Hero
        item {
            val heroContext = LocalContext.current
            HeroSection(
                venue = venue,
                onBack = onBack,
                onFavorite = { Toast.makeText(heroContext, "Favorites coming soon", Toast.LENGTH_SHORT).show() },
                onShare = { Toast.makeText(heroContext, "Share coming soon", Toast.LENGTH_SHORT).show() },
            )
        }

        // 2. Venue Header (overlaps hero by 20dp via negative padding handled in hero)
        item {
            VenueHeaderSection(venue = venue)
        }

        // Section divider
        item { SectionDivider() }

        // 3. Action Buttons
        item {
            ActionButtonsRow(
                onBookClick = onBookClick,
                phoneNumber = venue.phoneNumber
            )
        }

        // 3b. Browse Lobbies
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .clickable { onBrowseLobbies() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🏟️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Browse Lobbies",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkTextPrimary
                        )
                        Text(
                            "Find or create a group booking at this venue",
                            fontSize = 12.sp,
                            color = DarkTextSecondary
                        )
                    }
                    Text("›", fontSize = 22.sp, color = GreenAccent, fontWeight = FontWeight.Bold)
                }
            }
        }

        item { SectionDivider() }

        // 4. Quick Info Grid
        item {
            QuickInfoGrid(venue = venue)
        }

        item { SectionDivider() }

        // 5. Available Slots
        item {
            AvailableSlotsSection(
                timeSlots = timeSlots,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
            )
        }

        item { SectionDivider() }

        // 6. Sports Available
        item {
            SportsAvailableSection(venue = venue)
        }

        item { SectionDivider() }

        // 7. Open Matches Here
        item {
            OpenMatchesSection(matches = venueMatches, venue = venue)
        }

        item { SectionDivider() }

        // 8. About
        item {
            AboutSection(description = venue.description)
        }

        // 9. Amenities
        val includedEquipment = venue.equipment.filter { it.isIncluded }
        if (includedEquipment.isNotEmpty()) {
            item { SectionDivider() }
            item {
                AmenitiesSection(equipment = includedEquipment)
            }
        }

        item { SectionDivider() }

        // 10. Location Map
        item {
            LocationMapSection(address = venue.address)
        }

        item { SectionDivider() }

        // 11. Reviews
        item {
            ReviewsSummarySection(reviews = reviews, venue = venue)
        }

        if (reviews.isNotEmpty()) {
            items(items = reviews, key = { it.id }) { review ->
                ReviewCardItem(review = review, index = reviews.indexOf(review))
            }
        }

        // Bottom padding to clear the sticky bar
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ─── Divider between sections ────────────────────────────────────────────────
@Composable
private fun SectionDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Color.White.copy(alpha = 0.02f))
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// SECTION 1 — HERO
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun HeroSection(venue: Venue, onBack: () -> Unit, onFavorite: () -> Unit = {}, onShare: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        // Background: gradient or image
        if (venue.primaryImageUrl != null) {
            AsyncImage(
                model = venue.primaryImageUrl,
                contentDescription = venue.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Bottom fade overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, DarkBg),
                            startY = 120f
                        )
                    )
            )
        } else {
            // Gradient background with sport emoji
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1A3A1A), Color(0xFF1A2A3A))
                        )
                    )
            )
            // Bottom fade to DarkBg
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, DarkBg),
                            startY = 120f
                        )
                    )
            )
            // Sport emoji centered
            Text(
                text = venue.sportType.emoji(),
                fontSize = 80.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(0.6f)
            )
        }

        // Top overlay controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "←", fontSize = 18.sp, color = Color.White)
            }

            // Favorite + Share buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroIconButton(emoji = "♡", onClick = onFavorite)
                HeroIconButton(emoji = "↗", onClick = onShare)
            }
        }

        // Discount badge (top-left below controls)
        if (venue.activeDiscount != null) {
            val discountText = venue.activeDiscount.discountPercent?.let {
                "−${it.toInt()}% This Week"
            } ?: venue.activeDiscount.displayValue
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 100.dp, start = 16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE91E63))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = discountText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Gallery badge (bottom-right)
        val photoCount = venue.images.size.takeIf { it > 0 } ?: 0
        if (photoCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "📷 $photoCount Photos",
                    fontSize = 11.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun HeroIconButton(emoji: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 16.sp, color = Color.White)
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SECTION 2 — VENUE HEADER
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun VenueHeaderSection(venue: Venue) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Venue name
        Text(
            text = venue.name,
            fontSize = 24.sp,
            fontWeight = FontWeight(800),
            color = DarkTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rating row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "⭐ ${"%.1f".format(venue.avgRating)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFFD700)
            )
            Text(
                text = "  (${venue.totalReviews} reviews)",
                fontSize = 13.sp,
                color = DarkTextSecondary
            )
            if (venue.avgRating >= 4.5) {
                Text(
                    text = "  · 🏆 Top Rated",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Location
        val locationText = buildString {
            append("📍 ")
            append(venue.address)
            if (!venue.city.isNullOrBlank()) append(" · ${venue.city}")
        }
        Text(
            text = locationText,
            fontSize = 13.sp,
            color = DarkTextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tags row
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sport tag (green)
            VenueTag(
                text = "${venue.sportType.emoji()} ${venue.sportType.displayName}",
                bgColor = GreenAccent.copy(alpha = 0.12f),
                textColor = GreenAccent
            )

            // Equipment-based tags
            val includedNames = venue.equipment.filter { it.isIncluded }.map { it.name.lowercase() }
            if (includedNames.any { "indoor" in it }) {
                VenueTag(
                    text = "🏠 Indoor",
                    bgColor = Color(0xFFAB47BC).copy(alpha = 0.12f),
                    textColor = Color(0xFFAB47BC)
                )
            }
            val hasEquipment = includedNames.any {
                "ball" in it || "net" in it || "racket" in it || "equipment" in it
            }
            if (hasEquipment) {
                VenueTag(
                    text = "🎽 Equipment",
                    bgColor = BlueAccent.copy(alpha = 0.12f),
                    textColor = BlueAccent
                )
            }
            if (includedNames.any { "parking" in it }) {
                VenueTag(
                    text = "🅿️ Parking",
                    bgColor = Color(0xFFFF9800).copy(alpha = 0.12f),
                    textColor = Color(0xFFFF9800)
                )
            }
            if (includedNames.any { "wifi" in it || "wi-fi" in it }) {
                VenueTag(
                    text = "📶 WiFi",
                    bgColor = Color(0xFF00BCD4).copy(alpha = 0.12f),
                    textColor = Color(0xFF00BCD4)
                )
            }
        }
    }
}

@Composable
private fun VenueTag(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SECTION 3 — ACTION BUTTONS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun ActionButtonsRow(onBookClick: () -> Unit, phoneNumber: String?) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Book Now — primary CTA
        Box(
            modifier = Modifier
                .weight(1.4f)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(listOf(GreenAccent, GreenDark))
                )
                .clickable { onBookClick() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📅 Book Now",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Call
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                .clickable {
                    phoneNumber?.let { phone ->
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    }
                }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📞 Call",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (phoneNumber != null) DarkTextPrimary else DarkTextPrimary.copy(alpha = 0.4f)
            )
        }

        // Chat
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                .clickable {
                    Toast.makeText(context, "Chat coming soon", Toast.LENGTH_SHORT).show()
                }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💬 Chat",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SECTION 4 — QUICK INFO GRID
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun QuickInfoGrid(venue: Venue) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Price
        QuickInfoCard(
            modifier = Modifier.weight(1f),
            icon = "💰",
            value = "€${(venue.discountedPrice ?: venue.pricePerHour).toInt()}",
            valueColor = GreenAccent,
            label = "FROM / HOUR"
        )

        // Open hours
        QuickInfoCard(
            modifier = Modifier.weight(1f),
            icon = "⏰",
            value = "09–22h",
            valueColor = BlueAccent,
            label = "OPEN HOURS"
        )

        // Courts (placeholder count based on equipment)
        val courtCount = venue.equipment.count { it.isIncluded }.coerceAtLeast(1)
        QuickInfoCard(
            modifier = Modifier.weight(1f),
            icon = venue.sportType.emoji(),
            value = "$courtCount",
            valueColor = Color(0xFFFFD700),
            label = "COURTS"
        )
    }
}

@Composable
private fun QuickInfoCard(
    modifier: Modifier,
    icon: String,
    value: String,
    valueColor: Color,
    label: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = DarkTextTertiary,
            letterSpacing = 0.5.sp
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SECTION 5 — AVAILABLE SLOTS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun AvailableSlotsSection(
    timeSlots: List<TimeSlot>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    val today = LocalDate.now()
    val dateChips = (0..4).map { today.plusDays(it.toLong()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🕐 Available Slots",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary
            )
            Text(
                text = "All Dates",
                fontSize = 13.sp,
                color = GreenAccent
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dateChips.forEach { date ->
                val isActive = date == selectedDate
                val label = if (date == today) "Today"
                else date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault()) +
                        " " + date.dayOfMonth + " " +
                        date.month.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isActive) GreenAccent.copy(alpha = 0.15f)
                            else Color.White.copy(alpha = 0.05f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isActive) GreenAccent.copy(alpha = 0.6f)
                            else Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { onDateSelected(date) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) GreenAccent else DarkTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (timeSlots.isEmpty()) {
            Text(
                text = "No slots available for this date",
                fontSize = 13.sp,
                color = DarkTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeSlots.forEach { slot ->
                    TimeSlotChip(slot = slot)
                }
            }
        }
    }
}

@Composable
private fun TimeSlotChip(slot: TimeSlot) {
    val bgColor = if (slot.isAvailable) Color.Transparent else Color.White.copy(alpha = 0.03f)
    val borderColor = if (slot.isAvailable) GreenAccent.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)
    val textColor = if (slot.isAvailable) GreenAccent else DarkTextTertiary

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .alpha(if (!slot.isAvailable) 0.3f else 1f)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = slot.startTime,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            style = if (!slot.isAvailable)
                TextStyle(textDecoration = TextDecoration.LineThrough)
            else TextStyle()
        )
        if (slot.priceOverride != null) {
            Text(
                text = "€${slot.priceOverride.toInt()}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = GreenAccent
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SECTION 6 — SPORTS AVAILABLE
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun SportsAvailableSection(venue: Venue) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "🏟️ Sports Available",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Primary sport for this venue
            SportAvailableCard(
                modifier = Modifier.weight(1f),
                emoji = venue.sportType.emoji(),
                name = venue.sportType.displayName,
                priceRange = "€${venue.pricePerHour.toInt()}–€${(venue.pricePerHour * 1.3).toInt()}",
                courts = "3 courts"
            )
            // Secondary placeholder sport
            val secondarySport = when (venue.sportType) {
                SportType.BASKETBALL -> SportType.VOLLEYBALL
                SportType.FOOTBALL -> SportType.HANDBALL
                SportType.TENNIS -> SportType.BADMINTON
                else -> SportType.VOLLEYBALL
            }
            SportAvailableCard(
                modifier = Modifier.weight(1f),
                emoji = secondarySport.emoji(),
                name = secondarySport.displayName,
                priceRange = "€${(venue.pricePerHour * 0.8).toInt()}–€${venue.pricePerHour.toInt()}",
                courts = "2 courts"
            )
        }
    }
}

@Composable
private fun SportAvailableCard(
    modifier: Modifier,
    emoji: String,
    name: String,
    priceRange: String,
    courts: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(text = emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = priceRange, fontSize = 12.sp, color = GreenAccent)
        Text(text = courts, fontSize = 11.sp, color = DarkTextTertiary)
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SECTION 7 — OPEN MATCHES HERE
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun OpenMatchesSection(matches: List<Match>, venue: Venue) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏆 Open Matches Here",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary
            )
            if (matches.isNotEmpty()) {
                Text(text = "See All", fontSize = 13.sp, color = GreenAccent)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (matches.isEmpty()) {
            Text(
                text = "No open matches at this venue right now",
                fontSize = 13.sp,
                color = DarkTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            matches.forEach { match ->
                MatchCardItem(match = match)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun MatchCardItem(match: Match) {
    val canJoin = !match.isFull
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(GreenAccent.copy(alpha = 0.06f))
            .border(1.dp, GreenAccent.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GreenAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = match.sportType.emoji(), fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = match.title.ifBlank { "${match.sportType.displayName} Match" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary
            )
            Text(
                text = "${match.matchDate} ${match.startTime} · ${match.currentPlayers}/${match.maxPlayers} spots",
                fontSize = 12.sp,
                color = DarkTextSecondary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (canJoin) GreenAccent.copy(alpha = 0.2f)
                    else Color.White.copy(alpha = 0.06f)
                )
                .border(
                    1.dp,
                    if (canJoin) GreenAccent.copy(alpha = 0.4f)
                    else Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text(
                text = if (canJoin) "Join" else "Full",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (canJoin) GreenAccent else DarkTextSecondary
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SECTION 8 — ABOUT
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun AboutSection(description: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "📝 About",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = description ?: "A premier sports venue offering top-class facilities for athletes of all levels. Book your session today and experience the best courts in the city.",
            fontSize = 14.sp,
            color = DarkTextSecondary.copy(alpha = 0.6f + 0.4f), // effectively DarkTextSecondary
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Read more",
            fontSize = 13.sp,
            color = GreenAccent
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SECTION 9 — AMENITIES GRID
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun AmenitiesSection(equipment: List<VenueEquipment>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "🎯 Amenities",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 2-column grid using chunked rows
        equipment.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { item ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = equipmentEmoji(item.name), fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.name,
                            fontSize = 13.sp,
                            color = DarkTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Fill empty cell in odd row
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SECTION 10 — LOCATION MAP PLACEHOLDER
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun LocationMapSection(address: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "📍 Location",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1A2A1A), Color(0xFF1A1A2A))
                    )
                )
        ) {
            // Map pin centered
            Text(
                text = "📍",
                fontSize = 40.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            // Address label bottom-left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = address,
                    fontSize = 11.sp,
                    color = DarkTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Directions button bottom-right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenAccent)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🧭 Directions",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SECTION 11 — REVIEWS SUMMARY + REVIEW CARDS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun ReviewsSummarySection(reviews: List<Review>, venue: Venue) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "⭐ Reviews",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Summary card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFFD700).copy(alpha = 0.05f))
                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Big rating number + stars
            Column(
                modifier = Modifier.weight(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "%.1f".format(venue.avgRating),
                    fontSize = 36.sp,
                    fontWeight = FontWeight(800),
                    color = Color(0xFFFFD700)
                )
                Row {
                    repeat(5) { i ->
                        Text(
                            text = if (i < venue.avgRating.toInt()) "★" else "☆",
                            fontSize = 14.sp,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
                Text(
                    text = "${venue.totalReviews} reviews",
                    fontSize = 11.sp,
                    color = DarkTextSecondary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Rating bars (placeholder percentages)
            Column(
                modifier = Modifier.weight(1.4f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val barData = listOf(
                    Pair("5★", 0.65f),
                    Pair("4★", 0.20f),
                    Pair("3★", 0.10f),
                    Pair("2★", 0.03f),
                    Pair("1★", 0.02f),
                )
                barData.forEach { (label, fraction) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = DarkTextSecondary,
                            modifier = Modifier.width(22.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFFFD700))
                            )
                        }
                    }
                }
            }
        }

        if (reviews.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No reviews yet. Be the first to review!",
                fontSize = 14.sp,
                color = DarkTextSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ReviewCardItem(review: Review, index: Int) {
    val avatarColor = AVATAR_COLORS[index % AVATAR_COLORS.size]
    val initial = (review.playerName ?: "?").firstOrNull()?.toString() ?: "?"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar circle with initial
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.playerName ?: "Anonymous",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
                if (!review.createdAt.isNullOrBlank()) {
                    Text(
                        text = review.createdAt,
                        fontSize = 11.sp,
                        color = DarkTextTertiary
                    )
                }
            }

            // Star rating
            Row {
                repeat(5) { i ->
                    Text(
                        text = if (i < review.rating) "★" else "☆",
                        fontSize = 12.sp,
                        color = Color(0xFFFFD700)
                    )
                }
            }
        }

        if (!review.comment.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment,
                fontSize = 13.sp,
                color = DarkTextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ═════════════════════════════════════════════════════════════════════════════

private fun sampleVenue() = Venue(
    id = 1L,
    ownerId = 1L,
    name = "Arena Sports Complex",
    description = "A premier multi-sport complex with professional courts, " +
            "fully equipped changing rooms, and on-site coaching staff. " +
            "Ideal for casual games and competitive leagues alike.",
    sportType = SportType.BASKETBALL,
    pricePerHour = 35.0,
    address = "45 Partizanska Street",
    city = "Skopje",
    country = "North Macedonia",
    avgRating = 4.8,
    totalReviews = 127,
    phoneNumber = "+389 2 123 4567",
    equipment = listOf(
        VenueEquipment(1L, 1L, "Basketballs", isIncluded = true),
        VenueEquipment(2L, 1L, "Lockers", isIncluded = true),
        VenueEquipment(3L, 1L, "Parking", isIncluded = true),
        VenueEquipment(4L, 1L, "WiFi", isIncluded = true),
        VenueEquipment(5L, 1L, "Showers", isIncluded = true),
        VenueEquipment(6L, 1L, "Changing Rooms", isIncluded = true),
    ),
    activeDiscount = Discount(
        id = 1L,
        venueId = 1L,
        title = "Weekend Deal",
        discountPercent = 25.0,
        isActive = true
    )
)

private fun sampleReviews() = listOf(
    Review(
        id = 1L, playerId = 10L, venueId = 1L, rating = 5,
        comment = "Absolutely top-tier courts. The floor is amazing, equipment is always fresh. Highly recommend!",
        playerName = "Jordan M.", createdAt = "2026-05-20"
    ),
    Review(
        id = 2L, playerId = 11L, venueId = 1L, rating = 4,
        comment = "Great place overall. Parking could be better on weekends but the court quality is excellent.",
        playerName = "Sara K.", createdAt = "2026-05-15"
    ),
    Review(
        id = 3L, playerId = 12L, venueId = 1L, rating = 5,
        comment = "Perfect for serious training sessions. Staff is very helpful.",
        playerName = "Bojan T.", createdAt = "2026-05-10"
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "VenueDetail Full Screen")
@Composable
private fun VenueDetailScreenPreview() {
    SportsBookTheme {
        VenueDetailScaffold(
            venue = sampleVenue(),
            reviews = sampleReviews(),
            timeSlots = emptyList(),
            venueMatches = emptyList(),
            selectedDate = LocalDate.now(),
            onDateSelected = {},
            onBookClick = {},
            onBrowseLobbies = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Review Card")
@Composable
private fun ReviewCardPreview() {
    SportsBookTheme {
        Column(
            modifier = Modifier
                .background(DarkBg)
                .padding(16.dp)
        ) {
            sampleReviews().forEachIndexed { index, review ->
                ReviewCardItem(review = review, index = index)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Time Slot Chips")
@Composable
private fun TimeSlotChipsPreview() {
    SportsBookTheme {
        Column(
            modifier = Modifier
                .background(DarkBg)
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeSlotChip(TimeSlot(id = 1, startTime = "10:00", isAvailable = true))
                TimeSlotChip(TimeSlot(id = 2, startTime = "11:00", isAvailable = false))
                TimeSlotChip(TimeSlot(id = 3, startTime = "12:00", isAvailable = true, priceOverride = 25.0))
            }
        }
    }
}
