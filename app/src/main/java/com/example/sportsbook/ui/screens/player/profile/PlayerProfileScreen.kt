package com.example.sportsbook.ui.screens.player.profile

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.PartnerType
import com.example.sportsbook.domain.enums.UserRole
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.Navy600
import com.example.sportsbook.ui.theme.Navy700
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite

@Composable
fun PlayerProfileScreen(
    onSignOut: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onManageTimeSlots: () -> Unit = {},
    onNavigateToXpLevel: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToPayments: () -> Unit = {},
    onNavigateToFriends: () -> Unit = {},
    viewModel: PlayerProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Profile updated successfully")
            viewModel.clearSaveSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null && uiState.user == null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadProfile
            )
            else -> {
                val user = uiState.user ?: return@Box
                val isPartner = user.role == UserRole.PARTNER

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ── Profile Header (Instagram-style) ──
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Profile picture — left aligned, bigger
                            if (user.photoUrl != null) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Profile photo",
                                    modifier = Modifier
                                        .size(86.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(86.dp)
                                        .clip(CircleShape)
                                        .background(Navy600),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        modifier = Modifier.size(42.dp),
                                        tint = USOpenGold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Stats: Followers / Following
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatColumn(
                                    count = uiState.followers,
                                    label = "Followers",
                                    onClick = onNavigateToFriends
                                )
                                StatColumn(
                                    count = uiState.following,
                                    label = "Following",
                                    onClick = onNavigateToFriends
                                )
                            }
                        }
                    }

                    // ── Name & Bio ──
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = user.displayName ?: "No name set",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WarmWhite
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmWhite.copy(alpha = 0.6f)
                            )
                            if (!user.bio.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = user.bio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WarmWhite.copy(alpha = 0.85f)
                                )
                            }

                            // Partner badge
                            if (isPartner) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(USOpenGold.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (user.partnerType) {
                                            PartnerType.VENUE_OWNER -> "Venue Owner"
                                            PartnerType.COACH -> "Coach"
                                            else -> "Partner"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = USOpenGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // ── Edit Profile fields (when editing) ──
                    if (uiState.isEditing) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                OutlinedTextField(
                                    value = uiState.editDisplayName,
                                    onValueChange = viewModel::onDisplayNameChange,
                                    label = { Text("Display Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = uiState.editPhoneNumber,
                                    onValueChange = viewModel::onPhoneNumberChange,
                                    label = { Text("Phone Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = uiState.editBio,
                                    onValueChange = viewModel::onBioChange,
                                    label = { Text("Bio") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    maxLines = 5
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = viewModel::saveProfile,
                                        modifier = Modifier.weight(1f),
                                        enabled = !uiState.isSaving,
                                        shape = RectangleShape
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (uiState.isSaving) "Saving..." else "Save")
                                    }
                                    OutlinedButton(
                                        onClick = viewModel::cancelEditing,
                                        modifier = Modifier.weight(1f),
                                        shape = RectangleShape
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // ── Divider ──
                    item {
                        HorizontalDivider(color = Navy600.copy(alpha = 0.5f))
                    }

                    // ── Menu Buttons (full width, square corners, left-aligned) ──
                    if (!uiState.isEditing) {
                        item {
                            ProfileMenuButton(
                                icon = Icons.Default.Edit,
                                label = "Edit Profile",
                                onClick = viewModel::startEditing
                            )
                        }
                    }

                    item {
                        ProfileMenuButton(
                            icon = Icons.Default.Payment,
                            label = "Payment History",
                            onClick = onNavigateToPayments
                        )
                    }

                    item {
                        ProfileMenuButton(
                            icon = Icons.Default.Star,
                            label = "XP & Levels",
                            onClick = onNavigateToXpLevel
                        )
                    }

                    item {
                        ProfileMenuButton(
                            icon = Icons.Default.EmojiEvents,
                            label = "Achievements",
                            onClick = onNavigateToAchievements
                        )
                    }

                    item {
                        ProfileMenuButton(
                            icon = Icons.Default.BarChart,
                            label = "Player Stats",
                            onClick = onNavigateToStats
                        )
                    }

                    // Partner tools
                    if (isPartner) {
                        item {
                            HorizontalDivider(color = Navy600.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Partner Tools",
                                style = MaterialTheme.typography.labelMedium,
                                color = WarmWhite.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }

                        item {
                            ProfileMenuButton(
                                icon = Icons.Default.Dashboard,
                                label = "Partner Dashboard",
                                onClick = onNavigateToDashboard,
                                accentColor = USOpenGold
                            )
                        }

                        item {
                            ProfileMenuButton(
                                icon = Icons.Default.Schedule,
                                label = "Manage Time Slots",
                                onClick = onManageTimeSlots
                            )
                        }
                    }

                    // ── Sign Out (centered, below everything) ──
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Navy600.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))

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

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ── Follower/Following stat column ──
@Composable
private fun StatColumn(
    count: Int,
    label: String,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = WarmWhite
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = WarmWhite.copy(alpha = 0.6f)
        )
    }
}

// ── Full-width menu button with square corners ──
@Composable
private fun ProfileMenuButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    accentColor: Color = WarmWhite
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
            tint = accentColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = WarmWhite,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = WarmWhite.copy(alpha = 0.4f)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun PlayerProfileScreenPreview() {
    SportsBookTheme {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(Navy600),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(42.dp), tint = USOpenGold)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatColumn(count = 24, label = "Followers")
                        StatColumn(count = 18, label = "Following")
                    }
                }
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Alex Johnson", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = WarmWhite)
                    Text("alex@email.com", style = MaterialTheme.typography.bodySmall, color = WarmWhite.copy(alpha = 0.6f))
                    Text("Basketball & tennis enthusiast", style = MaterialTheme.typography.bodyMedium, color = WarmWhite.copy(alpha = 0.85f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Navy600.copy(alpha = 0.5f))
            }
            item { ProfileMenuButton(icon = Icons.Default.Edit, label = "Edit Profile", onClick = {}) }
            item { ProfileMenuButton(icon = Icons.Default.Payment, label = "Payment History", onClick = {}) }
            item { ProfileMenuButton(icon = Icons.Default.Star, label = "XP & Levels", onClick = {}) }
            item { ProfileMenuButton(icon = Icons.Default.EmojiEvents, label = "Achievements", onClick = {}) }
            item { ProfileMenuButton(icon = Icons.Default.BarChart, label = "Player Stats", onClick = {}) }
        }
    }
}
