package com.example.sportsbook.ui.screens.player.gamification

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.SportGreen
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.DarkTextTertiary
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

@Composable
fun PlayerAchievementsScreen(
    onBack: () -> Unit,
    viewModel: PlayerAchievementsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.newlyEarned) {
        if (uiState.newlyEarned.isNotEmpty()) {
            val names = uiState.newlyEarned.joinToString(", ") { it.name }
            snackbarHostState.showSnackbar("New achievements unlocked: $names")
            viewModel.clearNewlyEarned()
        }
    }

    val earned = uiState.earnedAchievements.size
    val total = uiState.allAchievements.size.coerceAtLeast(28) // mockup shows 28

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        when {
            uiState.isLoading && uiState.allAchievements.isEmpty() -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.error != null && uiState.allAchievements.isEmpty() -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadData,
            )
            else -> {
                val filtered = if (uiState.selectedCategory == "all") {
                    uiState.allAchievements
                } else {
                    uiState.allAchievements.filter { it.category.lowercase() == uiState.selectedCategory }
                }
                val earnedIds = uiState.earnedAchievements.map { it.achievementId }.toSet()
                // Fall back to sample data if no API data yet
                val displaySamples = filtered.isEmpty()

                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
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
                        Spacer(Modifier.width(12.dp))
                        Text("Achievements", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GreenAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Text("$earned/$total", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
                        }
                    }

                    // XP level banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(androidx.compose.ui.graphics.Color(0xFF1B3A1E), DarkSurface)))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("🌟", fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Level 7 — Rising Player", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            Text("1,830 / 2,500 XP to Level 8", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                            Spacer(Modifier.height(6.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(DarkBorder)) {
                                Box(modifier = Modifier.fillMaxWidth(0.73f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(GreenAccent))
                            }
                        }
                    }

                    // Category tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CATEGORIES.forEach { category ->
                            val isActive = uiState.selectedCategory == category
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isActive) GreenAccent else DarkSurface)
                                    .clickable { viewModel.selectCategory(category) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = category.replaceFirstChar { it.uppercase() },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isActive) androidx.compose.ui.graphics.Color.White else DarkTextSecondary,
                                )
                            }
                        }
                    }

                    // Grid
                    if (displaySamples) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(sampleAchievements) { sample ->
                                SampleAchievementCard(sample = sample)
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(filtered, key = { it.id }) { achievement ->
                                val earnedEntry = uiState.earnedAchievements.find { it.achievementId == achievement.id }
                                AchievementCard(achievement = achievement, isEarned = achievement.id in earnedIds, earnedAt = earnedEntry?.earnedAt)
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "$earned of $total Achievements Earned",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DarkBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(GreenAccent)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${(progress * 100).toInt()}% complete",
            fontSize = 10.sp,
            color = DarkTextSecondary,
        )
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
    val borderColor = if (isEarned) GreenAccent else DarkBorder
    val iconTint = if (isEarned) GreenAccent else DarkTextTertiary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isEarned) GreenDark.copy(alpha = 0.08f) else DarkSurface
            )
            .border(
                width = if (isEarned) 1.5.dp else 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
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
                            .background(DarkSurface, shape = RoundedCornerShape(50))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = DarkTextTertiary,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = achievement.name,
                fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = if (isEarned) DarkTextPrimary else DarkTextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = achievement.description,
                fontSize = 12.sp,
                color = DarkTextTertiary,
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
                        if (isEarned) GreenAccent.copy(alpha = 0.2f)
                        else DarkBorder
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "+${achievement.xpReward} XP",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = if (isEarned) GreenDark else DarkTextTertiary
                )
            }
            if (isEarned && !earnedAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = earnedAt.toDisplayDateTime(),
                    fontSize = 10.sp,
                    color = DarkTextTertiary,
                    textAlign = TextAlign.Center
                )
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

// ─── Sample data & composable ────────────────────────────────────────────────

private data class SampleAchievement(
    val emoji: String,
    val name: String,
    val desc: String,
    val xp: Int,
    val earned: Boolean,
    val category: String,
)

private val sampleAchievements = listOf(
    SampleAchievement("🏆", "First Booking", "Complete your first booking", 50, true, "booking"),
    SampleAchievement("⚽", "Match Starter", "Join your first match", 75, true, "match"),
    SampleAchievement("👥", "Social Butterfly", "Add 5 friends", 100, true, "social"),
    SampleAchievement("🔥", "On Fire", "Book 5 sessions in a week", 150, true, "booking"),
    SampleAchievement("🌟", "Rising Star", "Reach Level 5", 200, true, "general"),
    SampleAchievement("🎯", "Sharpshooter", "Win 10 matches", 250, false, "match"),
    SampleAchievement("🏅", "Veteran", "Play 50 matches", 300, false, "match"),
    SampleAchievement("💬", "Chatterbox", "Send 100 messages", 80, false, "social"),
    SampleAchievement("📅", "Consistent", "Book every week for a month", 200, false, "booking"),
    SampleAchievement("🚀", "Overachiever", "Earn 20 achievements", 500, false, "general"),
)

@Composable
private fun SampleAchievementCard(sample: SampleAchievement) {
    val borderColor = if (sample.earned) GreenAccent else DarkBorder
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (sample.earned) androidx.compose.ui.graphics.Color(0xFF1B3A1E) else DarkSurface)
            .border(if (sample.earned) 1.5.dp else 0.5.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (sample.earned) GreenAccent.copy(alpha = 0.2f) else DarkBorder.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(sample.emoji, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = sample.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (sample.earned) DarkTextPrimary else DarkTextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = sample.desc,
            fontSize = 10.sp,
            color = DarkTextTertiary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (sample.earned) GreenAccent.copy(alpha = 0.2f) else DarkBorder)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = "+${sample.xp} XP",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (sample.earned) GreenAccent else DarkTextTertiary,
            )
        }
        if (sample.earned) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("✓ Earned", fontSize = 10.sp, color = GreenAccent, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PlayerAchievementsScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
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
