package com.example.sportsbook.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRoleSelection: () -> Unit,
    onNavigateToPlayerOnboarding: () -> Unit,
    onNavigateToPlayerHome: () -> Unit,
    onNavigateToPartnerDashboard: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (uiState) {
            is SplashUiState.NavigateToLogin -> onNavigateToLogin()
            is SplashUiState.NavigateToRoleSelection -> onNavigateToRoleSelection()
            is SplashUiState.NavigateToPlayerOnboarding -> onNavigateToPlayerOnboarding()
            is SplashUiState.NavigateToPlayerHome -> onNavigateToPlayerHome()
            is SplashUiState.NavigateToPartnerDashboard -> onNavigateToPartnerDashboard()
            is SplashUiState.Loading -> Unit
        }
    }

    SplashScreenContent(isLoading = uiState is SplashUiState.Loading)
}

@Composable
private fun SplashScreenContent(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Navy900),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = "SportsBook app icon",
                modifier = Modifier.size(80.dp),
                tint = USOpenGold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SportsBook",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = WarmWhite
            )
            Spacer(modifier = Modifier.height(32.dp))
            if (isLoading) {
                CircularProgressIndicator(
                    color = USOpenGold,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenLoadingPreview() {
    SportsBookTheme {
        SplashScreenContent(isLoading = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenIdlePreview() {
    SportsBookTheme {
        SplashScreenContent(isLoading = false)
    }
}
