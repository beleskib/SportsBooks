package com.example.sportsbook.ui.screens.player.gamification

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.example.sportsbook.ui.theme.BorderGray
import com.example.sportsbook.ui.theme.CardWhite
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.GoldDark
import com.example.sportsbook.ui.theme.LightBg
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.TextPrimary
import com.example.sportsbook.ui.theme.TextSecondary
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        containerColor = LightBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Player Stats",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavBarBg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null && uiState.stats == null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadData
            )
            else -> {
                val stats = uiState.stats ?: PlayerStats()
                val level = uiState.level

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Level summary banner
                    if (level != null) {
                        LevelSummaryBanner(level = level)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Stats grid
                    val statItems = buildStatItems(stats)
                    StatGrid(statItems = statItems)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Check achievements button
                    Button(
                        onClick = viewModel::checkAchievements,
                        enabled = !uiState.isChecking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavBarBg,
                            contentColor = GoldAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isChecking) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checking...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check New Achievements", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ─── Private Composables ─────────────────────────────────────────────────────

@Composable
private fun LevelSummaryBanner(level: PlayerLevel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = GoldDark.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Level ${level.currentLevel}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GoldAccent
                )
                Text(
                    text = "${level.totalXp} XP total  •  ${level.xpToNextLevel} XP to next level",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private data class StatItem(
    val icon: ImageVector,
    val value: String,
    val label: String
)

private fun buildStatItems(stats: PlayerStats): List<StatItem> = listOf(
    StatItem(
        icon = Icons.Default.SportsSoccer,
        value = "${stats.totalMatches}",
        label = "Total Matches"
    ),
    StatItem(
        icon = Icons.Default.CheckCircle,
        value = "${stats.completedBookings}",
        label = "Completed Bookings"
    ),
    StatItem(
        icon = Icons.Default.Timer,
        value = "${stats.totalHoursPlayed}h",
        label = "Hours Played"
    ),
    StatItem(
        icon = Icons.Default.RateReview,
        value = "${stats.totalReviews}",
        label = "Reviews Written"
    ),
    StatItem(
        icon = Icons.Default.Group,
        value = "${stats.totalFriends}",
        label = "Friends"
    ),
    StatItem(
        icon = Icons.Default.Favorite,
        value = "${stats.uniqueSportsBooked}",
        label = "Sports Played"
    ),
    StatItem(
        icon = Icons.Default.CalendarMonth,
        value = "${stats.memberSinceDays}d",
        label = "Member Since"
    ),
    StatItem(
        icon = Icons.Default.BookOnline,
        value = stats.favoriteSport?.replaceFirstChar { it.uppercase() } ?: "—",
        label = "Favorite Sport"
    )
)

@Composable
private fun StatGrid(statItems: List<StatItem>) {
    // Using a manual two-column layout to avoid nested scrollable containers
    val rows = statItems.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { item ->
                    StatCard(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Pad last row if odd number of items
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    item: StatItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = item.value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun PlayerStatsScreenPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LevelSummaryBanner(
                level = PlayerLevel(
                    currentLevel = 5,
                    totalXp = 2350,
                    xpToNextLevel = 150
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            StatGrid(
                statItems = buildStatItems(
                    PlayerStats(
                        totalMatches = 12,
                        completedBookings = 8,
                        totalHoursPlayed = 24,
                        totalReviews = 5,
                        totalFriends = 3,
                        uniqueSportsBooked = 4,
                        memberSinceDays = 90,
                        favoriteSport = "tennis"
                    )
                )
            )
        }
    }
}
