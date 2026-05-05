package com.example.sportsbook.ui.screens.player.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.TextSecondary
import com.example.sportsbook.ui.theme.BorderGray
import com.example.sportsbook.ui.theme.LightBg
import com.example.sportsbook.ui.theme.CardWhite
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SportsIFollowScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit = {},
    viewModel: SportsIFollowViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Sports updated!")
            viewModel.clearSaveSuccess()
            onSaved()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = NavBarBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sports I Follow",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavBarBg
                )
            )
        },
        bottomBar = {
            if (uiState.hasChanges) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardWhite)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = viewModel::save,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = NavBarBg
                        )
                    ) {
                        Text(
                            text = if (uiState.isSaving) "Saving..." else "Save Changes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose the sports you're interested in. This personalises your home feed, play suggestions, and friend availability.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Text(
                text = "${uiState.selectedSports.size} selected",
                style = MaterialTheme.typography.labelMedium,
                color = GoldAccent,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                uiState.allSports.forEach { sport ->
                    val isSelected = sport in uiState.selectedSports
                    SportToggleChip(
                        sport = sport,
                        isSelected = isSelected,
                        onClick = { viewModel.toggleSport(sport) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SportToggleChip(
    sport: SportType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) GoldAccent.copy(alpha = 0.15f) else LightBg,
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) GoldAccent else BorderGray,
        label = "chipBorder"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) GoldAccent else TextPrimary.copy(alpha = 0.7f),
        label = "chipText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        if (isSelected) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = GoldAccent
                )
                Text(
                    text = sport.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Text(
                text = sport.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = textColor
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun SportsIFollowScreenPreview() {
    SportsBookTheme {
        SportsIFollowScreen(onBack = {})
    }
}
