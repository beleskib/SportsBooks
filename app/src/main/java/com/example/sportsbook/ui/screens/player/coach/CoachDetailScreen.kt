package com.example.sportsbook.ui.screens.player.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.CoachCertification
import com.example.sportsbook.domain.model.Review
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.BlueAccent
import com.example.sportsbook.ui.theme.BlueDarkAccent
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkNavBar
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.DarkTextTertiary
import com.example.sportsbook.ui.theme.GreenAccent

// ── Local design tokens ──────────────────────────────────────────────────────
private val CoachBlueLight = Color(0xFF64B5F6)
private val GoldStar = Color(0xFFFFC107)

// ── Screen ───────────────────────────────────────────────────────────────────

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
            val coach = uiState.selectedCoach ?: return
            Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
                CoachDetailContent(
                    coach = coach,
                    reviews = uiState.reviews,
                    onBack = onBack,
                    modifier = Modifier.padding(bottom = 80.dp),
                )
                CoachBottomBar(
                    coach = coach,
                    onBookClick = onBookClick,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

// ── Content ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoachDetailContent(
    coach: Coach,
    reviews: List<Review>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedDateIndex by remember { mutableIntStateOf(0) }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        // ── Hero ─────────────────────────────────────────────────────────
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Gradient hero
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0D47A1),
                                    BlueAccent,
                                    CoachBlueLight,
                                )
                            )
                        ),
                ) {
                    // Overlay buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Back
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                                .clickable(onClick = onBack),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HeroCircleButton("♡")
                            HeroCircleButton("↗")
                        }
                    }
                }

                // Avatar overlapping hero
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 24.dp, y = 40.dp)
                        .zIndex(2f),
                ) {
                    val avatarSize = 80.dp
                    if (!coach.primaryImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = coach.primaryImageUrl,
                            contentDescription = coach.name,
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .border(3.dp, DarkBg, CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .background(BlueDarkAccent)
                                .border(3.dp, DarkBg, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = coach.name.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }

                    // Verified badge
                    if (coach.avgRating >= 4.5) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(GreenAccent)
                                .border(2.dp, DarkBg, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("✓", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // ── Coach Info ───────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 50.dp, bottom = 16.dp),
            ) {
                Text(
                    text = coach.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkTextPrimary,
                )
                if (!coach.specialization.isNullOrBlank()) {
                    Text(
                        text = coach.specialization!!,
                        fontSize = 13.sp,
                        color = CoachBlueLight,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp),
                ) {
                    Text("⭐⭐⭐⭐⭐", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${coach.avgRating} (${coach.totalReviews} reviews)",
                        fontSize = 13.sp,
                        color = DarkTextSecondary,
                    )
                }
            }
        }

        // ── Stats Row ────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard("${coach.experienceYears}+", "Years Exp", Modifier.weight(1f))
                StatCard("340", "Sessions", Modifier.weight(1f))
                StatCard("${coach.totalReviews}", "Reviews", Modifier.weight(1f))
                StatCard("98%", "Response", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Action Buttons ───────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Book Session
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BlueAccent)
                        .clickable { }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📅 Book Session", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                // Message
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .clickable { }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("💬 Message", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                }
                // Call
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .clickable { }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📞 Call", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── About ────────────────────────────────────────────────────────
        item {
            SectionHeader("About")
            Text(
                text = coach.bio ?: "No bio available",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.67f),
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── Sports ───────────────────────────────────────────────────────
        item {
            SectionHeader("Sports")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val sportEmoji = sportTypeEmoji(coach.sportType.name)
                SportTagChip("$sportEmoji ${coach.sportType.displayName}")
                SportTagChip("🏋️ Fitness")
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── Certifications ───────────────────────────────────────────────
        item {
            SectionHeader("Certifications")
        }

        if (coach.certifications.isEmpty()) {
            item {
                Text(
                    text = "No certifications listed",
                    fontSize = 13.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            items(coach.certifications, key = { it.id }) { cert ->
                CertificationCard(cert)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // ── Available Slots ──────────────────────────────────────────────
        item {
            SectionHeader("Available Slots")

            // Date strip
            val dates = listOf("Sun" to "25", "Mon" to "26", "Tue" to "27", "Wed" to "28", "Thu" to "29")
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                dates.forEachIndexed { index, (dayName, dayNum) ->
                    val isActive = index == selectedDateIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) BlueAccent else DarkSurface)
                            .clickable { selectedDateIndex = index }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 10.sp,
                            color = if (isActive) Color.White.copy(alpha = 0.8f) else DarkTextSecondary,
                        )
                        Text(
                            text = dayNum,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time slots
            val slots = listOf(
                "09:00" to SlotState.AVAILABLE,
                "10:00" to SlotState.BOOKED,
                "11:00" to SlotState.SELECTED,
                "14:00" to SlotState.AVAILABLE,
                "15:00" to SlotState.AVAILABLE,
                "16:00" to SlotState.BOOKED,
                "17:00" to SlotState.AVAILABLE,
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                slots.forEach { (time, state) ->
                    TimeSlotChip(time = time, state = state)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── Reviews ──────────────────────────────────────────────────────
        item { SectionHeader("Reviews") }

        if (reviews.isEmpty()) {
            item {
                Text(
                    text = "No reviews yet",
                    fontSize = 13.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        } else {
            items(reviews, key = { it.id }) { review ->
                CoachReviewCard(review)
            }
        }

        // Bottom spacing for bar
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun HeroCircleButton(label: String) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 16.sp, color = Color.White)
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(12.dp),
    ) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BlueAccent)
        Text(label, fontSize = 10.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = DarkTextPrimary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun SportTagChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label, fontSize = 13.sp, color = DarkTextPrimary)
    }
}

@Composable
private fun CertificationCard(cert: CoachCertification) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BlueDarkAccent),
            contentAlignment = Alignment.Center,
        ) {
            Text("🏆", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(cert.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            Text(
                text = "${cert.issuingBody ?: "Unknown"} · ${cert.yearObtained ?: ""}",
                fontSize = 11.sp,
                color = DarkTextSecondary,
            )
        }
    }
}

private enum class SlotState { AVAILABLE, BOOKED, SELECTED }

@Composable
private fun TimeSlotChip(time: String, state: SlotState) {
    val bg = when (state) {
        SlotState.SELECTED -> BlueAccent
        else -> DarkSurface
    }
    val textColor = when (state) {
        SlotState.BOOKED -> DarkTextTertiary
        else -> DarkTextPrimary
    }
    val decoration = if (state == SlotState.BOOKED) TextDecoration.LineThrough else TextDecoration.None

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = time,
            fontSize = 13.sp,
            color = textColor,
            textDecoration = decoration,
        )
    }
}

@Composable
private fun CoachReviewCard(review: Review) {
    val avatarColors = listOf(
        Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFF2196F3),
        Color(0xFF9C27B0), Color(0xFF4CAF50),
    )
    val avatarBg = avatarColors[(review.id % avatarColors.size).toInt()]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (review.playerName?.firstOrNull() ?: '?').uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = review.playerName ?: "Anonymous",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary,
                modifier = Modifier.weight(1f),
            )
            val stars = "⭐".repeat(review.rating.coerceIn(1, 5))
            Text(stars, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (!review.comment.isNullOrBlank()) {
            Text(
                text = review.comment!!,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.67f),
                lineHeight = 18.sp,
            )
        }
    }
}

// ── Bottom bar ───────────────────────────────────────────────────────────────

@Composable
private fun CoachBottomBar(coach: Coach, onBookClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkNavBar)
            .border(width = 1.dp, color = DarkBorder)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            val displayPrice = coach.discountedPrice ?: coach.pricePerHour
            if (coach.discountedPrice != null) {
                Text(
                    text = "${coach.pricePerHour.toInt()} MKD",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    textDecoration = TextDecoration.LineThrough,
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${displayPrice.toInt()} MKD",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BlueAccent,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "/session (1h)",
                    fontSize = 12.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(BlueAccent)
                .clickable(onClick = onBookClick)
                .padding(horizontal = 32.dp, vertical = 14.dp),
        ) {
            Text("Book Now", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun sportTypeEmoji(sportTypeName: String): String = when (sportTypeName.uppercase()) {
    "BASKETBALL" -> "🏀"
    "FOOTBALL" -> "⚽"
    "TENNIS" -> "🎾"
    "PADDLE" -> "🏓"
    "VOLLEYBALL" -> "🏐"
    "SWIMMING" -> "🏊"
    "BOXING" -> "🥊"
    "MMA" -> "🥋"
    "YOGA" -> "🧘"
    "PILATES" -> "🤸"
    "CROSSFIT" -> "💪"
    "RUNNING" -> "🏃"
    "CYCLING" -> "🚴"
    "GOLF" -> "⛳"
    "BADMINTON" -> "🏸"
    "TABLE_TENNIS" -> "🏓"
    "HANDBALL" -> "🤾"
    "BASEBALL" -> "⚾"
    "CRICKET" -> "🏏"
    else -> "🏅"
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CoachDetailContentPreview() {
    val sampleCoach = Coach(
        id = 1L,
        name = "Aleksandar Petrov",
        bio = "Former professional basketball player with 8+ years of coaching experience. Specialized in youth development, shooting mechanics, and game strategy.",
        specialization = "Professional Basketball Coach",
        experienceYears = 8,
        pricePerHour = 1200.0,
        avgRating = 4.9,
        totalReviews = 89,
        certifications = listOf(
            CoachCertification(id = 1L, coachId = 1L, name = "FIBA Licensed Coach", issuingBody = "Level 2", yearObtained = 2019),
            CoachCertification(id = 2L, coachId = 1L, name = "Youth Development Specialist", issuingBody = "National Basketball Federation", yearObtained = null),
        ),
    )
    val sampleReviews = listOf(
        Review(id = 1L, playerId = 10L, coachId = 1L, rating = 5, comment = "Incredible coach! My shooting improved dramatically.", playerName = "Kristina V."),
        Review(id = 2L, playerId = 11L, coachId = 1L, rating = 5, comment = "Best coach in Skopje. Very professional.", playerName = "Dimitar P."),
    )
    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        CoachDetailContent(
            coach = sampleCoach,
            reviews = sampleReviews,
            onBack = {},
            modifier = Modifier.padding(bottom = 80.dp),
        )
        CoachBottomBar(
            coach = sampleCoach,
            onBookClick = {},
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CoachReviewCardPreview() {
    Column(modifier = Modifier.background(DarkBg).padding(16.dp)) {
        CoachReviewCard(
            review = Review(
                id = 1L,
                playerId = 10L,
                coachId = 1L,
                rating = 5,
                comment = "Great coach, very patient and knows the game inside out.",
                playerName = "Kristina V.",
            ),
        )
    }
}
