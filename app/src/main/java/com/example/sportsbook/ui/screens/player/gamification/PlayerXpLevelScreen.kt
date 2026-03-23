package com.example.sportsbook.ui.screens.player.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.PlayerLevel
import com.example.sportsbook.domain.model.XpTransaction
import com.example.sportsbook.domain.repository.GamificationRepository
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.toDisplayDateTime
import com.example.sportsbook.ui.theme.GoldDark
import com.example.sportsbook.ui.theme.GoldLight
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.SportGreen
import com.example.sportsbook.ui.theme.CoralRed
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite
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
class PlayerXpLevelViewModel @Inject constructor(
    private val gamificationRepository: GamificationRepository
) : ViewModel() {

    data class UiState(
        val level: PlayerLevel? = null,
        val xpHistory: List<XpTransaction> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val levelDeferred = async { gamificationRepository.getMyLevel() }
            val historyDeferred = async { gamificationRepository.getXpHistory() }
            val levelResult = levelDeferred.await()
            val historyResult = historyDeferred.await()
            _uiState.update {
                it.copy(
                    level = levelResult.getOrNull(),
                    xpHistory = historyResult.getOrElse { emptyList() },
                    isLoading = false,
                    error = levelResult.exceptionOrNull()?.message
                )
            }
        }
    }
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerXpLevelScreen(
    onBack: () -> Unit,
    viewModel: PlayerXpLevelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "XP & Levels",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900)
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null && uiState.level == null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadData
            )
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Level badge
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        LevelBadge(level = uiState.level?.currentLevel ?: 1)
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // XP progress card
                    item {
                        XpProgressCard(level = uiState.level)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // XP History header
                    item {
                        HorizontalDivider(color = WarmWhite.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "XP History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = WarmWhite,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (uiState.xpHistory.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No XP transactions yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WarmWhite.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        items(uiState.xpHistory, key = { it.id }) { transaction ->
                            XpTransactionItem(transaction = transaction)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ─── Private Composables ─────────────────────────────────────────────────────

@Composable
private fun LevelBadge(level: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(GoldLight.copy(alpha = 0.25f), GoldDark.copy(alpha = 0.05f))
                )
            )
            .border(
                width = 4.dp,
                brush = Brush.sweepGradient(
                    colors = listOf(GoldDark, USOpenGold, GoldLight, USOpenGold, GoldDark)
                ),
                shape = CircleShape
            )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = USOpenGold,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "$level",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp
                ),
                color = USOpenGold
            )
            Text(
                text = "Level",
                style = MaterialTheme.typography.labelSmall,
                color = WarmWhite.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun XpProgressCard(level: PlayerLevel?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (level != null) "Level ${level.currentLevel}" else "Level 1",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = WarmWhite
                )
                Text(
                    text = if (level != null) "Level ${level.currentLevel + 1}" else "Level 2",
                    style = MaterialTheme.typography.titleMedium,
                    color = WarmWhite.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { level?.progressFraction ?: 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = USOpenGold,
                trackColor = WarmWhite.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (level != null) {
                    "${level.totalXp} XP total  •  ${level.xpToNextLevel} XP to next level"
                } else {
                    "0 XP total"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = WarmWhite.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun XpTransactionItem(transaction: XpTransaction) {
    val icon: ImageVector = when (transaction.sourceType.lowercase()) {
        "booking" -> Icons.Default.Sports
        "match" -> Icons.Default.SportsSoccer
        "review" -> Icons.Default.RateReview
        "achievement" -> Icons.Default.EmojiEvents
        else -> Icons.Default.Star
    }

    val amountColor = if (transaction.amount >= 0) SportGreen else CoralRed
    val amountPrefix = if (transaction.amount >= 0) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = USOpenGold,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description
                        ?: transaction.sourceType.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = WarmWhite
                )
                Text(
                    text = transaction.createdAt.toDisplayDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmWhite.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "$amountPrefix${transaction.amount} XP",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = amountColor
            )
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun PlayerXpLevelScreenPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LevelBadge(level = 5)
            Spacer(modifier = Modifier.height(20.dp))
            XpProgressCard(
                level = PlayerLevel(
                    currentLevel = 5,
                    totalXp = 2350,
                    xpToNextLevel = 150
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            XpTransactionItem(
                transaction = XpTransaction(
                    id = 1,
                    amount = 50,
                    sourceType = "booking",
                    description = "Completed booking at Arena Sport",
                    createdAt = "2026-03-10T10:00:00Z"
                )
            )
        }
    }
}
