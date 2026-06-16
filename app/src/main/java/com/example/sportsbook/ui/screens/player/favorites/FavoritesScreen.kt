package com.example.sportsbook.ui.screens.player.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.FavoriteEntityType
import com.example.sportsbook.ui.common.EmptyStateView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.OrangeAccent

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val tabs = listOf("Venues", "Coaches", "Matches")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Favorites", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }

        // ── Tab bar ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(4.dp),
        ) {
            tabs.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (uiState.selectedTab == index) GreenAccent else Color.Transparent)
                        .clickable { viewModel.selectTab(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (uiState.selectedTab == index) Color.White else DarkTextSecondary,
                    )
                }
            }
        }

        // ── Content ──────────────────────────────────────────────────
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.fillMaxSize())
            else -> {
                val entityType = when (uiState.selectedTab) {
                    0 -> FavoriteEntityType.VENUE
                    1 -> FavoriteEntityType.COACH
                    else -> FavoriteEntityType.MATCH
                }
                val filtered = uiState.favorites.filter { it.entityType == entityType }

                if (filtered.isEmpty()) {
                    val (title, subtitle) = when (uiState.selectedTab) {
                        0 -> "No favorite venues" to "Venues you save will appear here"
                        1 -> "No favorite coaches" to "Coaches you save will appear here"
                        else -> "No favorite matches" to "Matches you save will appear here"
                    }
                    EmptyStateView(
                        title = title,
                        subtitle = subtitle,
                        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered) { fav ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkSurface)
                                    .padding(16.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1B3A1E)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(if (entityType == FavoriteEntityType.COACH) "🧑‍🏫" else "🏟", fontSize = 24.sp)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Favorite #${fav.entityId}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                                        Text(fav.entityType.value.replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 4.dp))
                                    }
                                    Text("❤️", fontSize = 20.sp, modifier = Modifier.clickable { viewModel.removeFavorite(fav.entityType.value, fav.entityId) })
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

// ── Sample card types ─────────────────────────────────────────────────────────

private data class SampleVenueFav(val id: Int, val emoji: String, val name: String, val meta: String, val statusLabel: String, val isOpen: Boolean, val price: String)
private data class SampleCoachFav(val id: Int, val emoji: String, val name: String, val sport: String, val rating: String, val sessions: String, val price: String)

@Composable
private fun FavVenueCard(item: SampleVenueFav, onBook: () -> Unit, onRemove: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface),
    ) {
        // Hero image area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Brush.linearGradient(listOf(Color(0xFF1B3A1E), Color(0xFF0D1F0E)))),
            contentAlignment = Alignment.Center,
        ) {
            Text(item.emoji, fontSize = 48.sp)
            // Heart button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Text("❤️", fontSize = 16.sp)
            }
        }
        // Info
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                Text("★ ${item.rating}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFD700))
            }
            Text(item.meta, fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (item.isOpen) GreenAccent.copy(alpha = 0.15f) else OrangeAccent.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text(item.statusLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (item.isOpen) GreenAccent else OrangeAccent)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(item.price, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(GreenAccent)
                        .clickable(onClick = onBook)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Text("Book", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FavCoachCard(item: SampleCoachFav, onBook: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF2196F3)))),
            contentAlignment = Alignment.Center,
        ) {
            Text(item.emoji, fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            Text(item.sport, fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("★ ${item.rating}", fontSize = 12.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.SemiBold)
                Text("${item.sessions} sessions", fontSize = 12.sp, color = DarkTextSecondary)
            }
            Text(item.price, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2196F3), modifier = Modifier.padding(top = 4.dp))
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("❤️", fontSize = 18.sp, modifier = Modifier.clickable(onClick = onRemove))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2196F3))
                    .clickable(onClick = onBook)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text("Book", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

// ── Sample data ───────────────────────────────────────────────────────────────

private val sampleVenueFavs = listOf(
    SampleVenueFav(1, "🎾", "Champions Tennis Club", "📍 1.2 km • 6 courts • Indoor/Outdoor", "Open Now", true, "From 500 MKD/h"),
    SampleVenueFav(2, "⚽", "City Football Arena", "📍 2.8 km • 3 fields • Outdoor", "Busy Today", false, "From 750 MKD/h"),
    SampleVenueFav(3, "🏀", "Downtown Basketball", "📍 0.8 km • 2 courts • Indoor", "Open Now", true, "From 350 MKD/h"),
)

private val sampleCoachFavs = listOf(
    SampleCoachFav(1, "🏀", "Coach Dragan M.", "Basketball • Pro Coach", "4.9", "120", "From 800 MKD/h"),
    SampleCoachFav(2, "🎾", "Coach Ana K.", "Tennis • Certified", "4.7", "85", "From 600 MKD/h"),
)

// Needed for SampleVenueFav - add rating field
private val SampleVenueFav.rating: String get() = when (id) { 1 -> "4.8"; 2 -> "4.6"; else -> "4.3" }

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun FavoritesScreenPreview() {
    FavoritesScreen(onBack = {})
}
