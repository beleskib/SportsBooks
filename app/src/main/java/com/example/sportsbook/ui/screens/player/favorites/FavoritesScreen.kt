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

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun FavoritesScreenPreview() {
    FavoritesScreen(onBack = {})
}
