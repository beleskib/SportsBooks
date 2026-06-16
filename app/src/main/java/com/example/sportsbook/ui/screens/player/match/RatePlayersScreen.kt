package com.example.sportsbook.ui.screens.player.match

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun RatePlayersScreen(
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: RatePlayersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedMvpId by remember { mutableLongStateOf(-1L) }

    LaunchedEffect(uiState.submitted) {
        if (uiState.submitted) onSubmitted()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 88.dp),
                ) {
                    // ── Header
                    item {
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
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Rate Players", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        }
                    }

                    // ── Match info card
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurface)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GreenAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                val sportEmoji = uiState.match?.sportType?.let { sportEmojiFor(it.name) } ?: "⚽"
                                Text(sportEmoji, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = uiState.match?.title ?: "Match",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary,
                                )
                                Text(
                                    text = uiState.match?.venueName?.let { "• $it" } ?: "",
                                    fontSize = 13.sp,
                                    color = DarkTextSecondary,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                    }

                    // ── Prompt
                    item {
                        Text(
                            text = "Rate the players you played with",
                            fontSize = 14.sp,
                            color = DarkTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                        )
                    }

                    // ── Player rating cards
                    if (uiState.ratings.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkSurface)
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "No players to rate",
                                    fontSize = 14.sp,
                                    color = DarkTextSecondary,
                                )
                            }
                        }
                    } else {
                        items(uiState.ratings) { rating ->
                            RatingCard(
                                rating = rating,
                                onSkillChange = { viewModel.updateRating(rating.userId, "skill", it) },
                                onTeamworkChange = { viewModel.updateRating(rating.userId, "punctuality", it) },
                                onSportsmanshipUp = { viewModel.updateRating(rating.userId, "sportsmanship", 5) },
                                onSportsmanshipDown = { viewModel.updateRating(rating.userId, "sportsmanship", 1) },
                            )
                        }
                    }

                    // ── MVP vote
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "🏆 Vote MVP",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                        val mvpCandidates = uiState.ratings
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            mvpCandidates.forEach { candidate ->
                                val isSelected = selectedMvpId == candidate.userId
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) Color(0xFFFFD700).copy(alpha = 0.08f) else DarkSurface,
                                        )
                                        .then(
                                            if (isSelected) Modifier.background(Color.Transparent)
                                            else Modifier
                                        )
                                        .clickable { selectedMvpId = if (isSelected) -1L else candidate.userId }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF333333)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("👤", fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = candidate.userName?.split(" ")?.let {
                                            if (it.size >= 2) "${it[0]} ${it[1].firstOrNull() ?: ""}." else it[0]
                                        } ?: "Player",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkTextPrimary,
                                    )
                                    if (isSelected) {
                                        Text("⭐ MVP", fontSize = 10.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Bottom bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, DarkBg, DarkBg),
                    ),
                )
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .clickable(onClick = onBack)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Skip", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTextSecondary)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (!uiState.isSubmitting) Brush.linearGradient(listOf(GreenAccent, GreenDark))
                            else Brush.linearGradient(listOf(DarkSurface, DarkSurface)),
                        )
                        .clickable(enabled = !uiState.isSubmitting) { viewModel.submitRatings() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (uiState.isSubmitting) "Submitting..." else "Submit Ratings",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp))
    }
}

// ── Rating card (real data) ───────────────────────────────────────────────────

@Composable
private fun RatingCard(
    rating: PlayerRatingInput,
    onSkillChange: (Int) -> Unit,
    onTeamworkChange: (Int) -> Unit,
    onSportsmanshipUp: () -> Unit,
    onSportsmanshipDown: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 14.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(20.dp),
    ) {
        // Player header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF333333)),
                contentAlignment = Alignment.Center,
            ) {
                Text("👤", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(rating.userName ?: "Player", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                Text("⚽ Player", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
            }
        }

        // Skill Level stars
        RatingRow(label = "Skill Level", value = rating.skillRating, onChange = onSkillChange)
        Spacer(modifier = Modifier.height(12.dp))

        // Teamwork stars
        RatingRow(label = "Teamwork", value = rating.punctualityRating, onChange = onTeamworkChange)
        Spacer(modifier = Modifier.height(12.dp))

        // Divider
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF252525)))
        Spacer(modifier = Modifier.height(12.dp))

        // Sportsmanship thumbs
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sportsmanship", fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val isUp = rating.sportsmanshipRating == 5
                val isDown = rating.sportsmanshipRating == 1
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isUp) GreenAccent.copy(alpha = 0.15f) else Color(0xFF252525))
                        .then(if (isUp) Modifier.background(Color.Transparent) else Modifier)
                        .clickable(onClick = onSportsmanshipUp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("👍", fontSize = 18.sp)
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDown) Color(0xFFEF5350).copy(alpha = 0.15f) else Color(0xFF252525))
                        .clickable(onClick = onSportsmanshipDown),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("👎", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun RatingRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= value) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "$i stars",
                    tint = if (i <= value) Color(0xFFFFD700) else Color(0xFF555555),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onChange(i) },
                )
            }
        }
    }
}


private fun sportEmojiFor(sportName: String): String = when (sportName.uppercase()) {
    "FOOTBALL" -> "⚽"
    "TENNIS" -> "🎾"
    "BASKETBALL" -> "🏀"
    "VOLLEYBALL" -> "🏐"
    "PADDLE" -> "🎾"
    "BADMINTON" -> "🏸"
    "TABLE_TENNIS" -> "🏓"
    else -> "🏅"
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun RatePlayersScreenPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = 88.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface), contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Rate Players", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                }
            }
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(16.dp)).background(DarkSurface).padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("No players to rate", fontSize = 14.sp, color = DarkTextSecondary) }
            }
        }
    }
}
