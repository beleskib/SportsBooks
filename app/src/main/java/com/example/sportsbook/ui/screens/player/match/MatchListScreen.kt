package com.example.sportsbook.ui.screens.player.match

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sportsbook.domain.enums.MatchStatus
import com.example.sportsbook.domain.enums.MatchType
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.DarkTextTertiary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import com.example.sportsbook.ui.theme.OrangeAccent
import com.example.sportsbook.ui.common.toPrettyDate

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun SportType.emoji(): String = when (this) {
    SportType.BASKETBALL -> "🏀"
    SportType.FOOTBALL -> "⚽"
    SportType.TENNIS -> "🎾"
    SportType.PADDLE -> "🏸"
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

private fun SportType.bgColor(): Color = when (this) {
    SportType.BASKETBALL -> Color(0xFFFF6B35)
    SportType.FOOTBALL -> Color(0xFF2D6A4F)
    SportType.TENNIS -> Color(0xFF95D55D)
    SportType.PADDLE -> Color(0xFF4361EE)
    SportType.VOLLEYBALL -> Color(0xFF2EC4B6)
    SportType.SWIMMING -> Color(0xFF0096C7)
    SportType.BOXING -> Color(0xFFE63946)
    SportType.MMA -> Color(0xFF9B2335)
    SportType.YOGA -> Color(0xFF7B2D8E)
    SportType.PILATES -> Color(0xFFFF6B9D)
    SportType.CROSSFIT -> Color(0xFFFF4500)
    SportType.RUNNING -> Color(0xFF48CAE4)
    SportType.CYCLING -> Color(0xFF52B788)
    SportType.GOLF -> Color(0xFF1B4332)
    SportType.BADMINTON -> Color(0xFF80ED99)
    SportType.TABLE_TENNIS -> Color(0xFFFF5400)
    SportType.HANDBALL -> Color(0xFF264653)
    SportType.BASEBALL -> Color(0xFFBC6C25)
    SportType.CRICKET -> Color(0xFF606C38)
}

// Deterministic avatar colors for the player avatar stack
private val avatarPalette = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800),
    Color(0xFFE91E63), Color(0xFF9C27B0)
)

// ── Main Screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchListScreen(
    onMatchClick: (Long) -> Unit,
    onCreateMatch: () -> Unit,
    onCreateParty: () -> Unit = {},
    onShowMap: () -> Unit = {},
    onBrowseAvailablePlayers: () -> Unit = {},
    viewModel: MatchListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },
        state = pullToRefreshState,
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── 1. Top Bar ────────────────────────────────────────────────
            item {
                MatchListTopBar(
                    onSearchClick = {},
                    onMapClick = onShowMap
                )
            }

            // ── 2. Mode Toggle ────────────────────────────────────────────
            item {
                ModeToggleRow()
            }

            // ── 3. Create Match CTA ───────────────────────────────────────
            item {
                CreateMatchCta(onClick = onCreateMatch)
            }

            // ── 4. My Active Matches ──────────────────────────────────────
            val activeStatuses = setOf(MatchStatus.OPEN, MatchStatus.FULL, MatchStatus.IN_PROGRESS)
            val myActive = uiState.myMatches.filter { it.status in activeStatuses }

            item {
                SectionHeader(
                    icon = "🎮",
                    title = "My Active Matches",
                    actionLabel = "See All",
                    onAction = {}
                )
            }

            if (myActive.isEmpty()) {
                item {
                    EmptySection(
                        message = "No active matches yet",
                        hint = "Create one or join an open match below"
                    )
                }
            } else {
                items(myActive, key = { "my_${it.id}" }) { match ->
                    MyMatchRow(
                        match = match,
                        onClick = { onMatchClick(match.id) }
                    )
                }
            }

            // ── 5. Open Matches Header + Filters ──────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    icon = "🔥",
                    title = "Open Matches",
                    actionLabel = null,
                    onAction = {}
                )
            }

            item {
                NearbyFilterRow()
            }

            item {
                SportFilterPills(
                    selected = uiState.selectedSport,
                    onSelect = { viewModel.selectSport(it) }
                )
            }

            // ── 6. Match Cards ────────────────────────────────────────────
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading...", color = DarkTextSecondary, fontSize = 14.sp)
                    }
                }
            } else if (uiState.error != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (uiState.displayedMatches.isEmpty()) {
                item {
                    EmptySection(
                        message = "No matches found",
                        hint = "Try a different sport or create your own match!"
                    )
                }
            } else {
                itemsIndexed(uiState.displayedMatches, key = { _, m -> "open_${m.id}" }) { index, match ->
                    MatchItemCard(
                        match = match,
                        isFeatured = index == 0,
                        onClick = { onMatchClick(match.id) }
                    )
                }
            }
        }
    }
}

// ── Section Composables ───────────────────────────────────────────────────────

@Composable
private fun MatchListTopBar(
    onSearchClick: () -> Unit,
    onMapClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Play",
            color = DarkTextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight(800),
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DarkSurface)
                .clickable(onClick = onSearchClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Search, "Search", tint = DarkTextSecondary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DarkSurface)
                .clickable(onClick = onMapClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.LocationOn, "Map", tint = DarkTextSecondary, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun ModeToggleRow() {
    // Visual-only toggle — "Find Matches" is always shown as active per mockup
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Active toggle
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(GreenAccent.copy(alpha = 0.15f))
                .border(1.dp, GreenAccent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎯 Find Matches",
                color = GreenAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        // Inactive toggle
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.07f))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📅 Book Venue",
                color = DarkTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CreateMatchCta(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(GreenDark, GreenAccent)
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "⚡",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Create a Match",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Pick a sport, set rules, invite players",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: String,
    title: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$icon $title",
            color = DarkTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                color = GreenAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

@Composable
private fun EmptySection(message: String, hint: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = DarkTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = hint, color = DarkTextTertiary, fontSize = 12.sp)
    }
}

@Composable
private fun MyMatchRow(match: Match, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sport icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(match.sportType.bgColor().copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = match.sportType.emoji(), fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = match.title,
                color = DarkTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            val detail = buildString {
                if (match.startTime.isNotBlank()) append(match.startTime)
                if (match.venueName != null) {
                    if (isNotEmpty()) append(" · ")
                    append(match.venueName)
                }
                if (isNotEmpty()) append(" · ")
                append("${match.currentPlayers}/${match.maxPlayers} players")
            }
            Text(
                text = detail,
                color = DarkTextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Status badge
        when (match.status) {
            MatchStatus.IN_PROGRESS -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GreenAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "● Live",
                        color = GreenAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            else -> {
                val daysText = if (match.matchDate.isNotBlank()) "Soon" else "Upcoming"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2196F3).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = daysText,
                        color = Color(0xFF2196F3),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbyFilterRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "Nearby" active
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(GreenAccent.copy(alpha = 0.15f))
                .border(1.dp, GreenAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text(text = "Nearby", color = GreenAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        // "Today" inactive
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.07f))
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text(text = "Today", color = DarkTextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SportFilterPills(
    selected: SportType?,
    onSelect: (SportType?) -> Unit
) {
    // Fixed order of common sports for the pills
    val pills: List<Pair<SportType?, String>> = listOf(
        null to "All",
        SportType.BASKETBALL to "🏀 Basketball",
        SportType.FOOTBALL to "⚽ Football",
        SportType.TENNIS to "🎾 Tennis",
        SportType.PADDLE to "🏸 Paddle",
        SportType.VOLLEYBALL to "🏐 Volleyball"
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        items(pills, key = { it.first?.name ?: "ALL" }) { (sport, label) ->
            val isActive = selected == sport
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isActive) GreenAccent.copy(alpha = 0.15f)
                        else Color.White.copy(alpha = 0.07f)
                    )
                    .then(
                        if (isActive) Modifier.border(
                            1.dp,
                            GreenAccent.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        ) else Modifier
                    )
                    .clickable { onSelect(sport) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    color = if (isActive) GreenAccent else DarkTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun MatchItemCard(
    match: Match,
    isFeatured: Boolean,
    onClick: () -> Unit
) {
    val cardBorder = if (isFeatured)
        Color.White.copy(alpha = 0.12f)
    else
        Color.White.copy(alpha = 0.06f)

    val cardBg = if (isFeatured)
        Color.White.copy(alpha = 0.07f)
    else
        Color.White.copy(alpha = 0.05f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        // ── Top row: sport icon + title + match type + status badge ──────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(match.sportType.bgColor().copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = match.sportType.emoji(), fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = match.title,
                        color = DarkTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = match.matchType.displayName,
                    color = DarkTextTertiary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status badge
            MatchStatusBadge(match)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Skill row ─────────────────────────────────────────────────────
        val skillLevel = match.minSkillLevel ?: 2
        SkillRow(skillLevel = skillLevel.coerceIn(1, 5))

        Spacer(modifier = Modifier.height(12.dp))

        // ── Details row ───────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            DetailsLine(icon = "📍", text = match.displayLocation)
            val timeText = buildString {
                if (match.matchDate.isNotBlank()) append(match.matchDate.toPrettyDate())
                if (match.startTime.isNotBlank()) {
                    if (isNotEmpty()) append("  •  ")
                    append(match.startTime)
                }
            }.ifBlank { "Time TBD" }
            DetailsLine(icon = "🕐", text = timeText)
            if (match.startTime.isNotBlank() && match.endTime.isNotBlank()) {
                DetailsLine(icon = "⏱️", text = "${match.startTime} – ${match.endTime}")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Bottom row: avatars + price + action button ───────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar stack
            AvatarStack(count = match.currentPlayers)

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${match.currentPlayers}/${match.maxPlayers}",
                color = DarkTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Price tag
            if (match.isFree) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GreenAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "Free", color = GreenAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Text(
                    text = match.displayCost,
                    color = DarkTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action button
            MatchActionButton(match = match, onClick = onClick)
        }
    }
}

@Composable
private fun MatchStatusBadge(match: Match) {
    val (label, bg, textColor) = when {
        match.status == MatchStatus.IN_PROGRESS -> Triple("● Live", GreenAccent.copy(alpha = 0.15f), GreenAccent)
        match.status == MatchStatus.FULL -> Triple("Full", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFEF4444))
        match.status == MatchStatus.CANCELLED -> Triple("Cancelled", DarkBorder, DarkTextTertiary)
        match.spotsLeft == 1 -> Triple("1 spot left", OrangeAccent.copy(alpha = 0.15f), OrangeAccent)
        match.status == MatchStatus.OPEN -> Triple("Open", GreenAccent.copy(alpha = 0.15f), GreenAccent)
        else -> Triple(match.status.displayName, DarkBorder, DarkTextSecondary)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SkillRow(skillLevel: Int) {
    val label = when (skillLevel) {
        1 -> "Newbie"
        2 -> "Beginner"
        3 -> "Intermediate"
        4 -> "Semi Pro"
        5 -> "Pro"
        else -> "Any"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Skill:", color = DarkTextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < skillLevel) GreenAccent
                            else Color.White.copy(alpha = 0.15f)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = DarkTextTertiary, fontSize = 12.sp)
    }
}

@Composable
private fun DetailsLine(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = DarkTextSecondary,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AvatarStack(count: Int) {
    val display = count.coerceIn(0, 5)
    Box(
        modifier = Modifier
            .height(28.dp)
            .width((28 + (display - 1).coerceAtLeast(0) * 20).dp)
    ) {
        repeat(display) { index ->
            val initial = ('A' + index).toString()
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = (index * 20).dp)
                    .clip(CircleShape)
                    .background(avatarPalette[index % avatarPalette.size])
                    .border(1.5.dp, DarkBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MatchActionButton(match: Match, onClick: () -> Unit) {
    val (label, bg, textColor) = when {
        match.status == MatchStatus.FULL -> Triple("View", Color.White.copy(alpha = 0.1f), DarkTextSecondary)
        match.status == MatchStatus.IN_PROGRESS -> Triple("View", Color.White.copy(alpha = 0.1f), DarkTextSecondary)
        match.status == MatchStatus.CANCELLED -> Triple("View", Color.White.copy(alpha = 0.1f), DarkTextSecondary)
        else -> Triple("Join", GreenAccent, Color.White)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun MatchListScreenPreview() {
    val sampleMatches = listOf(
        Match(
            id = 1L,
            hostId = 42L,
            title = "Saturday Pickup Basketball",
            sportType = SportType.BASKETBALL,
            status = MatchStatus.OPEN,
            venueName = "City Arena",
            matchDate = "2026-06-07",
            startTime = "18:00",
            endTime = "20:00",
            maxPlayers = 10,
            currentPlayers = 6,
            isFree = false,
            pricePerPlayer = 200.0,
            currency = "MKD",
            paymentType = "split",
            minSkillLevel = 3
        ),
        Match(
            id = 2L,
            hostId = 99L,
            title = "Evening Football 5v5",
            sportType = SportType.FOOTBALL,
            status = MatchStatus.FULL,
            venueName = "Green Park",
            matchDate = "2026-06-08",
            startTime = "19:00",
            endTime = "21:00",
            maxPlayers = 10,
            currentPlayers = 10,
            isFree = true,
            minSkillLevel = 2
        ),
        Match(
            id = 3L,
            hostId = 7L,
            title = "Tennis Singles Practice",
            sportType = SportType.TENNIS,
            status = MatchStatus.IN_PROGRESS,
            venueName = "Tennis Club",
            matchDate = "2026-06-02",
            startTime = "10:00",
            endTime = "11:00",
            maxPlayers = 2,
            currentPlayers = 1,
            isFree = false,
            pricePerPlayer = 500.0,
            currency = "MKD",
            paymentType = "split",
            minSkillLevel = 4
        )
    )

    val myActive = sampleMatches.filter { it.status in setOf(MatchStatus.OPEN, MatchStatus.IN_PROGRESS) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { MatchListTopBar(onSearchClick = {}, onMapClick = {}) }
            item { ModeToggleRow() }
            item { CreateMatchCta(onClick = {}) }
            item { SectionHeader(icon = "🎮", title = "My Active Matches", actionLabel = "See All", onAction = {}) }
            items(myActive, key = { "prev_my_${it.id}" }) { match ->
                MyMatchRow(match = match, onClick = {})
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SectionHeader(icon = "🔥", title = "Open Matches", actionLabel = null, onAction = {}) }
            item { NearbyFilterRow() }
            item { SportFilterPills(selected = null, onSelect = {}) }
            itemsIndexed(sampleMatches, key = { _, m -> "prev_open_${m.id}" }) { index, match ->
                MatchItemCard(match = match, isFeatured = index == 0, onClick = {})
            }
        }
    }
}
