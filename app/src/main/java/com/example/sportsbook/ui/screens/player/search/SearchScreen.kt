package com.example.sportsbook.ui.screens.player.search

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.DarkTextTertiary
import com.example.sportsbook.ui.theme.GreenAccent

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onVenueClick: (Long) -> Unit,
    onCoachClick: (Long) -> Unit,
    onMatchClick: (Long) -> Unit,
    onBack: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Search header ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))

            // Search bar
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🔍", fontSize = 16.sp, color = DarkTextSecondary)
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(fontSize = 15.sp, color = DarkTextPrimary),
                    cursorBrush = SolidColor(GreenAccent),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { }),
                    decorationBox = { inner ->
                        if (uiState.query.isEmpty()) {
                            Text("Search venues, coaches, players...", fontSize = 15.sp, color = DarkTextTertiary)
                        }
                        inner()
                    },
                )
                if (uiState.query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clickable { viewModel.onQueryChange("") },
                    ) {
                        Text("✕", fontSize = 16.sp, color = DarkTextTertiary)
                    }
                }
            }
        }

        // ── Filter chips ──────────────────────────────────────────────────
        val filterOptions = listOf("All", "Venues", "Coaches", "Players", "Matches")
        val activeFilter = 0 // Could be viewModel state
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            filterOptions.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (index == 0 && uiState.query.isEmpty() || (uiState.query.isNotEmpty() && index == 0)) GreenAccent else DarkSurface)
                        .clickable { }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextPrimary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Content ───────────────────────────────────────────────────────
        when {
            uiState.isSearching -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }

            uiState.hasSearched && (uiState.venues.isNotEmpty() || uiState.coaches.isNotEmpty() || uiState.matches.isNotEmpty()) -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = """Results for "${uiState.query}"""",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }

                    items(uiState.venues) { venue ->
                        SearchResultItem(
                            icon = "🎾",
                            iconBg = GreenAccent.copy(alpha = 0.15f),
                            name = venue.name,
                            meta = "📍 ${venue.address} • ★ ${venue.avgRating}",
                            typeLabel = "Venue",
                            typeBg = GreenAccent.copy(alpha = 0.15f),
                            typeColor = GreenAccent,
                            onClick = { onVenueClick(venue.id) },
                        )
                    }

                    items(uiState.coaches) { coach ->
                        SearchResultItem(
                            icon = "🏋️",
                            iconBg = Color(0xFF2196F3).copy(alpha = 0.15f),
                            name = coach.name,
                            meta = "${coach.sportType.displayName} • ★ ${coach.avgRating} • ${coach.totalReviews} sessions",
                            typeLabel = "Coach",
                            typeBg = Color(0xFF2196F3).copy(alpha = 0.15f),
                            typeColor = Color(0xFF2196F3),
                            onClick = { onCoachClick(coach.id) },
                        )
                    }

                    items(uiState.matches) { match ->
                        SearchResultItem(
                            icon = "⚡",
                            iconBg = Color(0xFF9C27B0).copy(alpha = 0.15f),
                            name = match.title,
                            meta = "${match.sportType.displayName} • ${match.currentPlayers}/${match.maxPlayers} players",
                            typeLabel = "Match",
                            typeBg = Color(0xFF9C27B0).copy(alpha = 0.15f),
                            typeColor = Color(0xFFAB47BC),
                            onClick = { onMatchClick(match.id) },
                        )
                    }
                }
            }

            else -> {
                // Empty state — show trending + recent
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = "Trending",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    item {
                        val trending = listOf("🔥 Football 5v5", "🏀 Basketball", "🏸 Paddle", "🎾 Tennis doubles", "🏐 Beach volleyball")
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            // Two-line wrap manually
                        }
                        // Use a flow-like approach with chunked
                        val chunked = trending.chunked(3)
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            chunked.forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(DarkSurface)
                                                .clickable { viewModel.onQueryChange(tag.removeRange(0, 2)) }
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                        ) {
                                            Text(tag, fontSize = 13.sp, color = Color.White.copy(alpha = 0.67f))
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Text(
                            text = "Popular searches",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }

                    val popularSearches = listOf("🎾 Tennis courts near me", "🏀 Basketball pickup", "⚽ 5-a-side football", "🏋️ Basketball coach")
                    items(popularSearches) { search ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onQueryChange(search.removeRange(0, 2)) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("🕐", fontSize = 16.sp, color = DarkTextTertiary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(search, fontSize = 14.sp, color = Color.White.copy(alpha = 0.67f), modifier = Modifier.weight(1f))
                            Text("✕", fontSize = 14.sp, color = DarkTextTertiary)
                        }
                    }
                }
            }
        }
    }
}

// ── Result item ───────────────────────────────────────────────────────────────

@Composable
private fun SearchResultItem(
    icon: String,
    iconBg: Color,
    name: String,
    meta: String,
    typeLabel: String,
    typeBg: Color,
    typeColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .border(
                width = 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(0.dp),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            Text(meta, fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(typeBg)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(typeLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = typeColor)
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1A1A1A)))
}

// ── Preview ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SearchScreenPreview() {
    Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg),
        ) {
            // Search header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("←", fontSize = 18.sp, color = DarkTextPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Row(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(DarkSurface).border(1.dp, DarkBorder, RoundedCornerShape(14.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🔍", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("tennis", fontSize = 15.sp, color = DarkTextPrimary)
                }
            }
            // Filter row
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("All", "Venues", "Coaches", "Players", "Matches").forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (index == 0) GreenAccent else DarkSurface).padding(horizontal = 16.dp, vertical = 8.dp),
                    ) { Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("""Results for "tennis"""", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTextSecondary, letterSpacing = 0.5.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            SearchResultItem(icon = "🎾", iconBg = GreenAccent.copy(0.15f), name = "Champions Tennis Club", meta = "📍 1.2 km • ★ 4.8 • 1,000 MKD/h", typeLabel = "Venue", typeBg = GreenAccent.copy(0.15f), typeColor = GreenAccent, onClick = {})
            SearchResultItem(icon = "🏋️", iconBg = Color(0xFF2196F3).copy(0.15f), name = "Coach Maria Santos", meta = "Tennis • ★ 4.9 • 200+ sessions", typeLabel = "Coach", typeBg = Color(0xFF2196F3).copy(0.15f), typeColor = Color(0xFF2196F3), onClick = {})
            SearchResultItem(icon = "⚡", iconBg = Color(0xFF9C27B0).copy(0.15f), name = "Tennis Doubles — Jun 8", meta = "Champions Club • 2/4 players", typeLabel = "Match", typeBg = Color(0xFF9C27B0).copy(0.15f), typeColor = Color(0xFFAB47BC), onClick = {})
        }
}
