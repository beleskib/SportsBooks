package com.example.sportsbook.ui.screens.player.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.ExperienceDuration
import com.example.sportsbook.domain.enums.SkillLevel
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.model.UserSportExpertise
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import com.example.sportsbook.ui.theme.OrangeAccent

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
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
    onNavigateToSettings: () -> Unit = {},
    onNavigateToChats: () -> Unit = {},
    onNavigateToBookingDetail: (Long) -> Unit = {},
    onNavigateToMyBookings: () -> Unit = {},
    viewModel: PlayerProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Profile saved!")
            viewModel.clearSaveSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        if (uiState.isLoading && uiState.user == null) {
            LoadingIndicator(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // ── Header ───────────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.size(36.dp)) // no back on own profile
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        // Edit button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .clickable { viewModel.startEditing() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text("✏ Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                        }
                    }
                }

                // ── Avatar + Name + XP ───────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.BottomEnd,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(GreenDark, GreenAccent))),
                                contentAlignment = Alignment.Center,
                            ) {
                                val initial = uiState.user?.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                                Text(initial, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            // Level badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFC107))
                                    .border(2.dp, DarkBg, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text("Lv.7", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.user?.displayName ?: "Player",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkTextPrimary,
                        )
                        Text(
                            text = "@${uiState.user?.email?.substringBefore("@") ?: "player"} • Skopje, MK",
                            fontSize = 13.sp,
                            color = DarkTextSecondary,
                            modifier = Modifier.padding(top = 2.dp),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // XP bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.72f)
                                .clickable(onClick = onNavigateToXpLevel),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Level 7 • 1,830 XP", fontSize = 11.sp, color = DarkTextSecondary)
                                Text("2,500 XP", fontSize = 11.sp, color = DarkTextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(DarkBorder),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.72f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Brush.horizontalGradient(listOf(GreenAccent, Color(0xFF81C784)))),
                                )
                            }
                        }
                    }
                }

                // ── Stats grid ───────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val stats: List<Pair<String, String>> = listOf(
                            (uiState.user?.totalMatchesPlayed?.toString() ?: "0") to "Matches",
                            uiState.pastBookings.size.toString() to "Bookings",
                            "12" to "Awards",
                            uiState.followers.toString() to "Friends",
                        )
                        stats.forEach { (value, label) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface)
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DarkTextPrimary)
                                Text(label, fontSize = 10.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }

                // ── Action buttons ───────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            Triple("👥 Add Friend", GreenAccent, {}),
                            Triple("💬 Message", DarkSurface, onNavigateToChats),
                            Triple("⚔ Challenge", DarkSurface, {}),
                        ).forEach { (label, bg, action) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .clickable { action() }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                // ── Sport Expertise ──────────────────────────────────────
                item {
                    SportExpertiseSection(
                        expertise = uiState.sportExpertise,
                        isEditing = uiState.isEditingExpertise,
                        editExpertise = uiState.editExpertise,
                        isSaving = uiState.isSavingExpertise,
                        interestedSports = uiState.user?.interestedSports ?: emptyList(),
                        onStartEditing = viewModel::startEditingExpertise,
                        onCancelEditing = viewModel::cancelEditingExpertise,
                        onSave = viewModel::saveExpertise,
                        onAddSport = viewModel::addExpertiseSport,
                        onRemoveSport = viewModel::removeExpertiseSport,
                        onSkillLevelChange = viewModel::updateExpertiseSkillLevel,
                        onExperienceChange = viewModel::updateExpertiseExperience,
                    )
                }

                // ── Achievements ─────────────────────────────────────────
                item {
                    ProfileSection(
                        title = "Achievements",
                        actionLabel = null,
                        onAction = onNavigateToAchievements,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("No achievements yet", fontSize = 13.sp, color = DarkTextSecondary)
                        }
                    }
                }

                // ── Recent Matches ───────────────────────────────────────
                item {
                    ProfileSection(
                        title = "Recent Matches",
                        actionLabel = "See All",
                        onAction = onNavigateToStats,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (uiState.pastBookings.isNotEmpty()) {
                                uiState.pastBookings.take(3).forEach { booking ->
                                    RecentMatchRow(booking = booking, onClick = { onNavigateToBookingDetail(booking.id) })
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkSurface)
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("No recent matches", fontSize = 13.sp, color = DarkTextSecondary)
                                }
                            }
                        }
                    }
                }

                // ── Quick nav ────────────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        ProfileNavRow("📅", "My Bookings", onClick = onNavigateToMyBookings)
                        ProfileNavRow("💰", "Payments", onClick = onNavigateToPayments)
                        ProfileNavRow("👥", "Friends", onClick = onNavigateToFriends)
                        ProfileNavRow("📊", "My Stats", onClick = onNavigateToStats)
                        ProfileNavRow("⚙", "Settings", onClick = onNavigateToSettings)
                    }
                }

                // ── Sign out ─────────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A1B1B))
                            .clickable {
                                viewModel.signOut()
                                onSignOut()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color(0xFFF44336), modifier = Modifier.size(18.dp))
                            Text("Sign Out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF44336))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun ProfileSection(
    title: String,
    actionLabel: String?,
    onAction: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            if (actionLabel != null) {
                Text(actionLabel, fontSize = 13.sp, color = GreenAccent, modifier = Modifier.clickable(onClick = onAction))
            }
        }
        content()
    }
}

@Composable
private fun SportExpertiseSection(
    expertise: List<UserSportExpertise>,
    isEditing: Boolean,
    editExpertise: List<EditableSportExpertise>,
    isSaving: Boolean,
    interestedSports: List<SportType>,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onSave: () -> Unit,
    onAddSport: (SportType) -> Unit,
    onRemoveSport: (SportType) -> Unit,
    onSkillLevelChange: (SportType, SkillLevel) -> Unit,
    onExperienceChange: (SportType, ExperienceDuration) -> Unit,
) {
    ProfileSection(
        title = "Sport Expertise",
        actionLabel = if (!isEditing) "Edit" else null,
        onAction = onStartEditing,
    ) {
        if (!isEditing) {
            // ── View mode ────────────────────────────────────────────
            if (expertise.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏅", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No expertise set yet", fontSize = 14.sp, color = DarkTextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(GreenAccent)
                                .clickable(onClick = onStartEditing)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text("Add Your Skills", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    expertise.forEach { entry ->
                        ExpertiseCard(entry)
                    }
                }
            }
        } else {
            // ── Edit mode ────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                editExpertise.forEach { entry ->
                    EditableExpertiseCard(
                        entry = entry,
                        onRemove = { onRemoveSport(entry.sportType) },
                        onSkillLevelChange = { onSkillLevelChange(entry.sportType, it) },
                        onExperienceChange = { onExperienceChange(entry.sportType, it) },
                    )
                }

                // Add sport buttons — show sports not yet added
                val availableSports = interestedSports.filter { sport ->
                    editExpertise.none { it.sportType == sport }
                }
                if (availableSports.isNotEmpty()) {
                    Text("Add a sport:", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        availableSports.forEach { sport ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable { onAddSport(sport) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    "+ ${sportEmoji(sport)} ${sport.displayName}",
                                    fontSize = 12.sp,
                                    color = GreenAccent,
                                )
                            }
                        }
                    }
                }

                // Save / Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .clickable(enabled = !isSaving, onClick = onCancelEditing)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Cancel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSaving) Color(0xFF2A3D2B) else GreenAccent)
                            .clickable(enabled = !isSaving, onClick = onSave)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (isSaving) "Saving..." else "Save",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpertiseCard(entry: UserSportExpertise) {
    val skillPct = skillLevelToProgress(entry.skillLevel)
    val barColor = skillLevelToColor(entry.skillLevel)
    Column(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            "${sportEmoji(entry.sportType)} ${entry.sportType.displayName}",
            fontSize = 13.sp,
            color = DarkTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DarkBorder),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(skillPct)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(entry.skillLevel.displayName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = barColor)
        Text(entry.experienceDuration.displayName, fontSize = 10.sp, color = DarkTextSecondary)
    }
}

@Composable
private fun EditableExpertiseCard(
    entry: EditableSportExpertise,
    onRemove: () -> Unit,
    onSkillLevelChange: (SkillLevel) -> Unit,
    onExperienceChange: (ExperienceDuration) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${sportEmoji(entry.sportType)} ${entry.sportType.displayName}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF5350).copy(alpha = 0.15f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Text("✕", fontSize = 12.sp, color = Color(0xFFEF5350))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Skill Level
        Text("Skill Level", fontSize = 11.sp, color = DarkTextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SkillLevel.entries.forEach { level ->
                val selected = entry.skillLevel == level
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) GreenAccent else Color.Transparent)
                        .border(
                            1.dp,
                            if (selected) GreenAccent else DarkBorder,
                            RoundedCornerShape(8.dp),
                        )
                        .clickable { onSkillLevelChange(level) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        level.displayName,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color.White else DarkTextSecondary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Experience
        Text("Experience", fontSize = 11.sp, color = DarkTextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ExperienceDuration.entries.forEach { exp ->
                val selected = entry.experienceDuration == exp
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) GreenAccent else Color.Transparent)
                        .border(
                            1.dp,
                            if (selected) GreenAccent else DarkBorder,
                            RoundedCornerShape(8.dp),
                        )
                        .clickable { onExperienceChange(exp) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        exp.displayName,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color.White else DarkTextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(emoji: String, name: String, bg: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(name, fontSize = 10.sp, color = DarkTextSecondary, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun RecentMatchRow(booking: Booking, onClick: () -> Unit) {
    val sportEmoji = when (booking.venue?.sportType) {
        SportType.BASKETBALL -> "🏀"
        SportType.FOOTBALL -> "⚽"
        SportType.TENNIS -> "🎾"
        SportType.PADDLE -> "🏓"
        SportType.VOLLEYBALL -> "🏐"
        else -> "🏟"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1B3A1E)),
            contentAlignment = Alignment.Center,
        ) {
            Text(sportEmoji, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(booking.matchTitle ?: booking.venue?.name ?: "Match", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(booking.createdAt?.take(10) ?: "", fontSize = 11.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 1.dp))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1B3A1E))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text("W", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
        }
    }
}


@Composable
private fun ProfileNavRow(icon: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary, modifier = Modifier.weight(1f))
        Text("›", fontSize = 20.sp, color = DarkTextSecondary)
    }
    Spacer(modifier = Modifier.height(2.dp))
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun sportEmoji(sportType: SportType): String = when (sportType) {
    SportType.BASKETBALL -> "🏀"
    SportType.FOOTBALL -> "⚽"
    SportType.TENNIS -> "🎾"
    SportType.PADDLE -> "🏓"
    SportType.VOLLEYBALL -> "🏐"
    SportType.BADMINTON -> "🏸"
    SportType.TABLE_TENNIS -> "🏓"
    SportType.SWIMMING -> "🏊"
    SportType.BOXING -> "🥊"
    SportType.GOLF -> "⛳"
    SportType.RUNNING -> "🏃"
    SportType.CYCLING -> "🚴"
    SportType.MMA -> "🥋"
    SportType.YOGA -> "🧘"
    SportType.PILATES -> "🤸"
    SportType.CROSSFIT -> "🏋️"
    SportType.HANDBALL -> "🤾"
    SportType.BASEBALL -> "⚾"
    SportType.CRICKET -> "🏏"
}

private fun skillLevelToProgress(level: SkillLevel): Float = when (level) {
    SkillLevel.NEWBIE -> 0.15f
    SkillLevel.BEGINNER -> 0.35f
    SkillLevel.INTERMEDIATE -> 0.55f
    SkillLevel.SEMI_PRO -> 0.75f
    SkillLevel.PRO -> 0.95f
}

private fun skillLevelToColor(level: SkillLevel): Color = when (level) {
    SkillLevel.NEWBIE -> Color(0xFF9E9E9E)
    SkillLevel.BEGINNER -> Color(0xFFFF9800)
    SkillLevel.INTERMEDIATE -> Color(0xFF4CAF50)
    SkillLevel.SEMI_PRO -> Color(0xFF2196F3)
    SkillLevel.PRO -> Color(0xFFE040FB)
}


// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PlayerProfileScreenPreview() {
    PlayerProfileScreen(onSignOut = {})
}
