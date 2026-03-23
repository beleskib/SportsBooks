package com.example.sportsbook.ui.screens.player.profile

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.PartnerType
import com.example.sportsbook.domain.enums.UserRole
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.USOpenGold

@Composable
fun PlayerProfileScreen(
    onSignOut: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onManageTimeSlots: () -> Unit = {},
    onNavigateToXpLevel: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile header
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        if (user.photoUrl != null) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Profile photo",
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = user.displayName ?: "No name set",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Role badge for partners
                        if (isPartner) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(USOpenGold.copy(alpha = 0.15f))
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (user.partnerType) {
                                        PartnerType.VENUE_OWNER -> "Venue Owner"
                                        PartnerType.COACH -> "Coach"
                                        else -> "Partner"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = USOpenGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Profile fields
                    if (uiState.isEditing) {
                        item {
                            OutlinedTextField(
                                value = uiState.editDisplayName,
                                onValueChange = viewModel::onDisplayNameChange,
                                label = { Text("Display Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        item {
                            OutlinedTextField(
                                value = uiState.editPhoneNumber,
                                onValueChange = viewModel::onPhoneNumberChange,
                                label = { Text("Phone Number") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        item {
                            OutlinedTextField(
                                value = uiState.editBio,
                                onValueChange = viewModel::onBioChange,
                                label = { Text("Bio") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    } else {
                        item {
                            ProfileField(label = "Display Name", value = user.displayName ?: "Not set")
                            ProfileField(label = "Phone", value = user.phoneNumber ?: "Not set")
                            ProfileField(label = "Bio", value = user.bio ?: "No bio yet")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Partner quick-access — dashboard & time slots
                    if (isPartner) {
                        item {
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Partner Tools",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        item {
                            Button(
                                onClick = onNavigateToDashboard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = USOpenGold,
                                    contentColor = Navy900
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Visit Partner Dashboard", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onManageTimeSlots,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Manage Time Slots")
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Gamification section
                    item {
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Gamification",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        Button(
                            onClick = onNavigateToXpLevel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = USOpenGold,
                                contentColor = Navy900
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("XP & Levels", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onNavigateToAchievements,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Achievements")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onNavigateToStats,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Player Stats")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Action buttons
                    item {
                        if (uiState.isEditing) {
                            Button(
                                onClick = viewModel::saveProfile,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isSaving
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(if (uiState.isSaving) "Saving..." else "Save Changes")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = viewModel::cancelEditing,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel")
                            }
                        } else {
                            Button(
                                onClick = viewModel::startEditing,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Edit Profile")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.signOut()
                                onSignOut()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Sign Out", color = MaterialTheme.colorScheme.error)
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

@Composable
private fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerProfileScreenPreview() {
    MaterialTheme {
        PlayerProfileScreen(onSignOut = {})
    }
}
