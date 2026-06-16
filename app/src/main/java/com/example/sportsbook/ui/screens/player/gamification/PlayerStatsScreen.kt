package com.example.sportsbook.ui.screens.player.gamification

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.PlayerAchievement
import com.example.sportsbook.domain.model.PlayerLevel
import com.example.sportsbook.domain.model.PlayerStats
import com.example.sportsbook.domain.repository.GamificationRepository
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class PlayerStatsViewModel @Inject constructor(
    private val gamificationRepository: GamificationRepository
) : ViewModel() {

    data class UiState(
        val stats: PlayerStats? = null,
        val level: PlayerLevel? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val isChecking: Boolean = false,
        val newAchievements: List<PlayerAchievement> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val statsDeferred = async { gamificationRepository.getMyStats() }
            val levelDeferred = async { gamificationRepository.getMyLevel() }
            val statsResult = statsDeferred.await()
            val levelResult = levelDeferred.await()
            _uiState.update {
                it.copy(
                    stats = statsResult.getOrNull(),
                    level = levelResult.getOrNull(),
                    isLoading = false,
                    error = statsResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun checkAchievements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true) }
            gamificationRepository.checkAndAwardAchievements()
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            newAchievements = result.newlyEarned,
                            level = result.level
                        )
                    }
                }
                .onFailure {
                    _uiState.update { s -> s.copy(isChecking = false) }
                }
        }
    }

    fun clearNewAchievements() {
        _uiState.update { it.copy(newAchievements = emptyList()) }
    }
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun PlayerStatsScreen(
    onBack: () -> Unit,
    viewModel: PlayerStatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.newAchievements) {
        if (uiState.newAchievements.isNotEmpty()) {
            val names = uiState.newAchievements.joinToString(", ") { it.name }
            snackbarHostState.showSnackbar("New achievements unlocked: $names")
            viewModel.clearNewAchievements()
        }
    }

    val stats = uiState.stats ?: PlayerStats()
    val level = uiState.level

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            uiState.error != null && uiState.stats == null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadData,
            )
            else -> {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(DarkSurface)
                                    .clickable(onClick = onBack),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("My Stats", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text("This Month ▾", fontSize = 12.sp, color = DarkTextSecondary)
                            }
                        }
                    }

                    // Level banner
                    if (level != null) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(androidx.compose.ui.graphics.Color(0xFF1B3A1E), DarkSurface)))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(GreenAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("⚡", fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Level ${level.currentLevel}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = GreenAccent)
                                    Text("${level.totalXp} XP • ${level.xpToNextLevel} XP to next level", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(DarkBorder),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(level.progressFraction)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(GreenAccent),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Overview cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                Triple("${stats.totalMatches}", "Matches Played", GreenAccent),
                                Triple("${if (stats.completedMatches > 0 && stats.totalMatches > 0) (stats.completedMatches * 100 / stats.totalMatches) else 0}%", "Win Rate", androidx.compose.ui.graphics.Color(0xFF2196F3)),
                                Triple("${stats.totalHoursPlayed}h", "Hours Played", androidx.compose.ui.graphics.Color(0xFFFF9800)),
                                Triple("${stats.uniqueSportsBooked}", "Sports Active", androidx.compose.ui.graphics.Color(0xFF9C27B0)),
                            ).forEach { (value, label, color) ->
                                Column(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(DarkSurface).padding(vertical = 12.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
                                    Text(label, fontSize = 9.sp, color = DarkTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }

                    // Streak cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf("🔥" to "Current Streak", "⚡" to "Best Streak", "📅" to "This Week").forEach { (icon, label) ->
                                Column(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(DarkSurface).padding(vertical = 14.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(icon, fontSize = 22.sp)
                                    Text(label, fontSize = 10.sp, color = DarkTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
                                }
                            }
                        }
                    }

                    // By sport
                    item {
                        Text("By Sport", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(14.dp)).background(DarkSurface),
                        ) {
                            listOf(
                                Triple("🎾", "Tennis", "18W - 6L • 24 matches"),
                                Triple("⚽", "Football", "10W - 5L • 15 matches"),
                                Triple("🏀", "Basketball", "8W - 0L • 8 matches"),
                            ).forEachIndexed { i, (emoji, name, record) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                                        Text(record, fontSize = 11.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                                    }
                                }
                                if (i < 2) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
                            }
                        }
                    }

                    // Check achievements button
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GreenAccent)
                                .clickable(enabled = !uiState.isChecking) { viewModel.checkAchievements() }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (uiState.isChecking) "Checking..." else "🏆 Check New Achievements",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = androidx.compose.ui.graphics.Color.White,
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter))
    }
}

// ─── Private Composables ─────────────────────────────────────────────────────

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PlayerStatsScreenPreview() {
    PlayerStatsScreen(onBack = {})
}
