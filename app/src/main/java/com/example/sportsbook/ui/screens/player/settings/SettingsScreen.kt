package com.example.sportsbook.ui.screens.player.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.CoolGray
import com.example.sportsbook.ui.theme.Navy600
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToSportsIFollow: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        color = WarmWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WarmWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy900
                )
            )
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
        ) {
            // ── User header card ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.photoUrl != null) {
                    AsyncImage(
                        model = uiState.photoUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Navy600),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(28.dp),
                            tint = USOpenGold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.displayName ?: "No name set",
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = uiState.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolGray
                    )
                }
            }

            HorizontalDivider(color = Navy600.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(8.dp))

            // ── Sports I Follow preview ──
            SectionHeader("SPORTS I FOLLOW")

            if (uiState.interestedSports.isEmpty()) {
                Text(
                    text = "No sports selected. Tap to pick your favourites.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoolGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToSportsIFollow)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            } else {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToSportsIFollow)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.interestedSports.forEach { sport ->
                        SportChip(sport = sport)
                    }
                }
            }

            SettingsMenuButton(
                icon = Icons.Default.SportsSoccer,
                label = "Edit Sports I Follow",
                onClick = onNavigateToSportsIFollow
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Navy600.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // ── Account section ──
            SectionHeader("ACCOUNT")

            SettingsMenuButton(
                icon = Icons.Default.Person,
                label = "Edit Profile",
                onClick = onNavigateToEditProfile
            )

            SettingsMenuButton(
                icon = Icons.Default.Notifications,
                label = "Notification Preferences",
                onClick = { /* TODO Phase 3 */ }
            )

            SettingsMenuButton(
                icon = Icons.Default.Visibility,
                label = "Profile Visibility",
                subtitle = "Control who can see your profile",
                onClick = { /* TODO Phase 2 privacy */ }
            )

            SettingsMenuButton(
                icon = Icons.Default.Lock,
                label = "Security & Password",
                onClick = { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Navy600.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // ── Sign Out ──
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.signOut()
                        onSignOut()
                    },
                    shape = RectangleShape
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = CoolGray,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5f
    )
}

@Composable
private fun SettingsMenuButton(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = WarmWhite
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = WarmWhite
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = CoolGray
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = WarmWhite.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun SportChip(sport: SportType) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Navy600)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = sport.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = USOpenGold,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun SettingsScreenPreview() {
    SportsBookTheme {
        SettingsScreen(onBack = {})
    }
}
