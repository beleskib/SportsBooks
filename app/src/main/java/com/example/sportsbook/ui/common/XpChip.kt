package com.example.sportsbook.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.PlayerLevel
import com.example.sportsbook.domain.repository.GamificationRepository
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.NavBarBg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Compact XP/level chip shown in TopAppBar actions on screens where progress
 * visibility matters (Search, Favorites, Notifications). Self-contained —
 * fetches its own state via a dedicated ViewModel so hosting screens don't
 * need to inject GamificationRepository.
 *
 * Design: small gold capsule with a bolt icon + `Lv N · XP` label on NavBarBg,
 * matching the existing gamification screens' palette.
 */
@Composable
fun XpChip(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: XpChipViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val level = state.level ?: return // hide chip until data loads — avoids UI jitter

    Row(
        modifier = modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(50))
            .background(NavBarBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = GoldAccent,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Lv ${level.currentLevel}",
            color = GoldAccent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "·",
            color = GoldAccent.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${level.totalXp} XP",
            color = GoldAccent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

data class XpChipState(
    val level: PlayerLevel? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class XpChipViewModel @Inject constructor(
    private val gamificationRepository: GamificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(XpChipState())
    val state: StateFlow<XpChipState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            gamificationRepository.getMyLevel()
                .onSuccess { level ->
                    _state.value = XpChipState(level = level, isLoading = false)
                }
                .onFailure {
                    // Silent-fail: leave state.level null so the chip stays hidden.
                    _state.value = _state.value.copy(isLoading = false)
                }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun XpChipPreview() {
    MaterialTheme {
        // Preview can't use hiltViewModel — inline the visual instead.
        Row(
            modifier = Modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(50))
                .background(NavBarBg)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Lv 4",
                color = GoldAccent,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "·", color = GoldAccent.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "1,720 XP",
                color = GoldAccent,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
