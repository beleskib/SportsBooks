package com.example.sportsbook.ui.screens.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark

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
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ── Logo circle ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(GreenAccent, GreenDark))),
                contentAlignment = Alignment.Center,
            ) {
                Text("⚽", fontSize = 48.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── App name ─────────────────────────────────────────────────
            Text(
                text = "SportsBooks",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GreenAccent,
            )

            // ── Tagline ──────────────────────────────────────────────────
            Text(
                text = "Book  •  Play  •  Compete",
                fontSize = 14.sp,
                color = DarkTextSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Loading bar ──────────────────────────────────────────────
            if (isLoading) {
                val infiniteTransition = rememberInfiniteTransition(label = "splash_bar")
                val progress by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1400, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                    label = "bar_progress",
                )

                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DarkSurface),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.linearGradient(listOf(GreenAccent, GreenDark))
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Version ──────────────────────────────────────────────────
            Text(
                text = "v2.0.0",
                fontSize = 12.sp,
                color = Color(0xFF444444),
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SplashScreenLoadingPreview() {
    SplashScreenContent(isLoading = true)
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SplashScreenIdlePreview() {
    SplashScreenContent(isLoading = false)
}
