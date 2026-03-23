package com.example.sportsbook.ui.screens.player.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Achievement
import com.example.sportsbook.domain.model.PlayerAchievement
import com.example.sportsbook.domain.repository.GamificationRepository
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.toDisplayDateTime
import com.example.sportsbook.ui.theme.GoldDark
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.SportGreen
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── ViewModel ───────────────────────────────────────────────────────────────

private val CATEGORIES = listOf("all", "booking", "match", "social", "general")

@HiltViewModel
class PlayerAchievementsViewModel @Inject constructor(
    private val gamificationRepository: GamificationRepository
) : ViewModel() {

    data class UiState(
        val allAchievements: List<Achievement> = emptyList(),
        val earnedAchievements: List<PlayerAchievement> = emptyList(),
        val selectedCategory: String = "all",
        val isLoading: Boolean = false,
        val error: String? = null,
        val isChecking: Boolean = false,
        val newlyEarned: List<PlayerAchievement> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val allResult = gamificationRepository.getAllAchievements()
            val earnedResult = gamificationRepository.getMyAchievements()
            _uiState.update {
                it.copy(
                    allAchievements = allResult.getOrElse { emptyList() },
                    earnedAchievements = earnedResult.getOrElse { emptyList() },
                    isLoading = false,
                    error = allResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun checkAchievements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true) }
            gamificationRepository.checkAndAwardAchievements()
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            newlyEarned = result.newlyEarned
                        )
                    }
                    if (result.newlyEarned.isNotEmpty()) {
                        loadData()
                    }
                }
                .onFailure {
                    _uiState.update { s -> s.copy(isChecking = false) }
                }
        }
    }

    fun clearNewlyEarned() {
        _uiState.update { it.copy(newlyEarned = emptyList()) }
    }
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlayerAchievementsScreen(
    onBack: () -> Unit,
    viewModel: PlayerAchievementsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when newly earned achievements arrive
    LaunchedEffect(uiState.newlyEarned) {
        if (uiState.newlyEarned.isNotEmpty()) {
            val names = uiState.newlyEarned.joinToString(", ") { it.name }
            snackbarHostState.showSnackbar("New achievements unlocked: $names")
            viewModel.clearNewlyEarned()
        }
    }

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Achievements",
                        color = WarmWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WarmWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::checkAchievements,
                        enabled = !uiState.isChecking
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Check new achievements",
                            tint = if (uiState.isChecking) WarmWhite.copy(alpha = 0.4f) else USOpenGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null && uiState.allAchievements.isEmpty() -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadData
            )
            else -> {
                val filtered = if (uiState.selectedCategory == "all") {
                    uiState.allAchievements
                } else {
                    uiState.allAchievements.filter {
                        it.category.lowercase() == uiState.selectedCategory
                    }
                }
                val earnedIds = uiState.earnedAchievements.map { it.achievementId }.toSet()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Summary card
                    AchievementSummaryCard(
                        earned = uiState.earnedAchievements.size,
                        total = uiState.allAchievements.size,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    // Category filter chips
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CATEGORIES.forEach { category ->
                            FilterChip(
                                selected = uiState.selectedCategory == category,
                                onClick = { viewModel.selectCategory(category) },
                                label = {
                                    Text(
                                        text = category.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = USOpenGold,
                                    selectedLabelColor = Navy900,
                                    containerColor = WarmWhite.copy(alpha = 0.08f),
                                    labelColor = WarmWhite.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No achievements in this category",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WarmWhite.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { achievement ->
                                val earnedEntry = uiState.earnedAchievements
                                    .find { it.achievementId == achievement.id }
                                AchievementCard(
                                    achievement = achievement,
                                    isEarned = achievement.id in earnedIds,
                                    earnedAt = earnedEntry?.earnedAt
                                )
                            }
                            // bottom spacing item
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ─── Private Composables ─────────────────────────────────────────────────────

@Composable
private fun AchievementSummaryCard(
    earned: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) earned.toFloat() / total else 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = USOpenGold,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$earned of $total Achievements Earned",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = WarmWhite
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = USOpenGold,
                trackColor = WarmWhite.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}% complete",
                style = MaterialTheme.typography.labelSmall,
                color = WarmWhite.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    isEarned: Boolean,
    earnedAt: String?,
    modifier: Modifier = Modifier
) {
    val cardAlpha = if (isEarned) 1f else 0.5f
    val borderColor = if (isEarned) USOpenGold else WarmWhite.copy(alpha = 0.1f)
    val iconTint = if (isEarned) USOpenGold else WarmWhite.copy(alpha = 0.4f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .border(
                width = if (isEarned) 1.5.dp else 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEarned)
                GoldDark.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEarned) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Icon(
                    imageVector = achievementIcon(achievement.icon),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(36.dp)
                )
                if (isEarned) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Earned",
                        tint = SportGreen,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .background(Navy900, shape = RoundedCornerShape(50))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = WarmWhite.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isEarned) WarmWhite else WarmWhite.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = WarmWhite.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            // XP badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isEarned) USOpenGold.copy(alpha = 0.2f)
                        else WarmWhite.copy(alpha = 0.05f)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "+${achievement.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isEarned) USOpenGold else WarmWhite.copy(alpha = 0.4f)
                )
            }
            if (isEarned && !earnedAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = earnedAt.toDisplayDateTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmWhite.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun achievementIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "emoji_events", "trophy" -> Icons.Default.EmojiEvents
        "sports_soccer", "match" -> Icons.Default.SportsSoccer
        "book_online", "booking" -> Icons.Default.BookOnline
        "group", "social", "friends" -> Icons.Default.Group
        "auto_awesome", "general" -> Icons.Default.AutoAwesome
        else -> Icons.Default.EmojiEvents
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun PlayerAchievementsScreenPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            AchievementSummaryCard(earned = 4, total = 12)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AchievementCard(
                    achievement = Achievement(
                        id = 1,
                        name = "First Booking",
                        description = "Complete your first booking",
                        icon = "book_online",
                        category = "booking",
                        xpReward = 50
                    ),
                    isEarned = true,
                    earnedAt = "2026-03-10T10:00:00Z",
                    modifier = Modifier.weight(1f)
                )
                AchievementCard(
                    achievement = Achievement(
                        id = 2,
                        name = "Social Butterfly",
                        description = "Add 5 friends",
                        icon = "group",
                        category = "social",
                        xpReward = 100
                    ),
                    isEarned = false,
                    earnedAt = null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
