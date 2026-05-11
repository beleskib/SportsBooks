package com.example.sportsbook.ui.screens.player.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.enums.PartnerType
import com.example.sportsbook.domain.enums.UserRole
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.screens.player.settings.PlusBadge
import com.example.sportsbook.ui.theme.SportsBookTheme

// ── Light-theme design tokens ──
private val LightBg = Color(0xFFF9FAFB)
private val CardBg = Color.White
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val TextTertiary = Color(0xFF9CA3AF)
private val GoldAccent = Color(0xFFFDE047)
private val BannerDark = Color(0xFF111827)
private val BannerMid = Color(0xFF1F2937)
private val BannerLight = Color(0xFF374151)
private val EmeraldBar = Color(0xFF34D399)
private val BlueBar = Color(0xFF60A5FA)
private val YellowBar = Color(0xFFFDE047)

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
            snackbarHostState.showSnackbar("Profile updated successfully")
            viewModel.clearSaveSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(LightBg)) {
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null && uiState.user == null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadProfile
            )
            else -> {
                val user = uiState.user ?: return@Box
                val isPartner = user.role == UserRole.PARTNER

                // Real data from the database (via UserDto → User)
                val matchCount = user.totalMatchesPlayed
                val streak = uiState.followers // reuse follow count as proxy until streak API exists
                val skillRating = user.avgPlayerSkillRating.toFloat()
                val sportsmanshipRating = user.avgPlayerSportsmanshipRating.toFloat()
                val punctualityRating = user.avgPlayerPunctualityRating.toFloat()
                val avgRating = if (user.totalPlayerRatings > 0) {
                    ((skillRating + sportsmanshipRating + punctualityRating) / 3f)
                } else {
                    0f
                }
                val reviewCount = user.totalPlayerRatings

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ── 1. Dark gradient banner with settings gear ──
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(BannerDark, BannerMid, BannerLight)
                                    )
                                )
                        ) {
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // ── 2. Avatar section (overlapping banner by -40dp) ──
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-40).dp)
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Circular avatar with white ring
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .border(4.dp, CardBg, CircleShape)
                                        .clip(CircleShape)
                                ) {
                                    if (user.photoUrl != null) {
                                        AsyncImage(
                                            model = user.photoUrl,
                                            contentDescription = "Profile photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(BannerMid),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Profile",
                                                modifier = Modifier.size(40.dp),
                                                tint = GoldAccent
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Name + subtitle
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.displayName ?: "No name set",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = TextPrimary
                                        )
                                        if (user.isPlus) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            PlusBadge()
                                        }
                                    }
                                    Text(
                                        text = "@${user.email.substringBefore("@")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    if (isPartner) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(GoldAccent.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = when (user.partnerType) {
                                                    PartnerType.VENUE_OWNER -> "Venue Owner"
                                                    PartnerType.COACH -> "Coach"
                                                    else -> "Partner"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF92400E),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Edit button
                                if (!uiState.isEditing) {
                                    OutlinedButton(
                                        onClick = viewModel::startEditing,
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = CardBg,
                                            contentColor = TextPrimary
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                    ) {
                                        Text(
                                            text = "Edit",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                        // Compensate for the negative offset so subsequent items don't overlap
                        Spacer(modifier = Modifier.height(0.dp))
                    }

                    // ── 3. Bio text ──
                    item {
                        if (!user.bio.isNullOrBlank()) {
                            Text(
                                text = user.bio,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFF374151),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 4.dp, bottom = 12.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // ── 4. Edit form (when editing) ──
                    if (uiState.isEditing) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
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
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = TextPrimary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Save,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (uiState.isSaving) "Saving..." else "Save")
                                    }
                                    OutlinedButton(
                                        onClick = viewModel::cancelEditing,
                                        modifier = Modifier.weight(1f),
                                        shape = RectangleShape
                                    ) {
                                        Text("Cancel", color = TextPrimary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    // ── 5. Stats row — 3 cards ──
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                label = "MATCHES",
                                value = "$matchCount",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = "AVG RATING",
                                value = "%.1f ⭐".format(avgRating),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = "STREAK",
                                value = "$streak 🔥",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // ── 6. Player ratings card ──
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PLAYER RATINGS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            letterSpacing = 0.8.sp
                                        ),
                                        color = TextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "from $reviewCount reviews",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp
                                        ),
                                        color = TextTertiary
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                RatingBar(
                                    label = "Skill",
                                    score = skillRating,
                                    barColor = YellowBar
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                RatingBar(
                                    label = "Sportsmanship",
                                    score = sportsmanshipRating,
                                    barColor = EmeraldBar
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                RatingBar(
                                    label = "Punctuality",
                                    score = punctualityRating,
                                    barColor = BlueBar
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // ── 7. Friends card ──
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { onNavigateToFriends() },
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "FRIENDS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            letterSpacing = 0.8.sp
                                        ),
                                        color = TextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Friends icon in a tinted circle
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFEF9C3)), // yellow-100
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.People,
                                            contentDescription = "Friends",
                                            tint = Color(0xFFCA8A04), // yellow-600
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "My Friends",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${uiState.followers} followers · ${uiState.following} following",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "View friends",
                                        tint = TextTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // ── 8. Chats card ──
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { onNavigateToChats() },
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CHATS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            letterSpacing = 0.8.sp
                                        ),
                                        color = TextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFDBEAFE)), // blue-100
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = "Chats",
                                            tint = Color(0xFF1D4ED8), // blue-700
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "My Chats",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Booking chats & match chats",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "View chats",
                                        tint = TextTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // ── 9. Past Bookings card ──
                    if (uiState.pastBookings.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = "Past Bookings",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "PAST BOOKINGS",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    letterSpacing = 0.8.sp
                                                ),
                                                color = TextSecondary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    uiState.pastBookings.forEachIndexed { index, booking ->
                                        PastBookingRow(
                                            booking = booking,
                                            onClick = { onNavigateToBookingDetail(booking.id) }
                                        )
                                        if (index < uiState.pastBookings.lastIndex) {
                                            HorizontalDivider(
                                                color = Color(0xFFF3F4F6),
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = Color(0xFFF3F4F6))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onNavigateToMyBookings() },
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "View All",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF1D4ED8)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = Color(0xFF1D4ED8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // ── Partner tools ──
                    if (isPartner) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "PARTNER TOOLS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            letterSpacing = 0.8.sp
                                        ),
                                        color = TextSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 16.dp,
                                            bottom = 4.dp
                                        )
                                    )
                                    LightMenuRow(
                                        icon = Icons.Default.Dashboard,
                                        label = "Partner Dashboard",
                                        onClick = onNavigateToDashboard,
                                        iconTint = GoldAccent
                                    )
                                    HorizontalDivider(
                                        color = Color(0xFFF3F4F6),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    LightMenuRow(
                                        icon = Icons.Default.Schedule,
                                        label = "Manage Time Slots",
                                        onClick = onManageTimeSlots
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // ── Sign out ──
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.signOut()
                                    onSignOut()
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
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

// ── Past booking row (inside the Past Bookings card) ──
@Composable
private fun PastBookingRow(
    booking: Booking,
    onClick: () -> Unit
) {
    val displayName = booking.venue?.name ?: booking.coach?.name ?: "Booking #${booking.id}"
    val dateText = booking.timeSlot?.slotDate?.toDisplayDate() ?: ""
    val isCompleted = booking.status == BookingStatus.COMPLETED
    val statusColor = if (isCompleted) Color(0xFF16A34A) else Color(0xFF1D4ED8)
    val statusBg = if (isCompleted) Color(0xFFDCFCE7) else Color(0xFFDBEAFE)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            if (dateText.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${"%.0f".format(booking.totalPrice)} ден",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = booking.status.name,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = statusColor,
                modifier = androidx.compose.ui.Modifier
                    .background(color = statusBg, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ── Stat card (one of three in the row) ──
@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                ),
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Single rating bar row ──
@Composable
private fun RatingBar(
    label: String,
    score: Float,
    barColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(96.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE5E7EB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((score / 5f).coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "%.1f".format(score),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End
        )
    }
}

// ── Light-theme menu row (used inside partner tools card) ──
@Composable
private fun LightMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconTint: Color = TextSecondary
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
            modifier = Modifier.size(20.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.bodyLarge,
            color = TextTertiary,
            fontSize = 20.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun PlayerProfileScreenPreview() {
    SportsBookTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBg)
        ) {
            // Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(BannerDark, BannerMid, BannerLight)
                        )
                    )
            ) {
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = GoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Avatar row (offset simulated with negative padding in preview)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(4.dp, CardBg, CircleShape)
                            .clip(CircleShape)
                            .background(BannerMid),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = GoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Alex Johnson",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            "@alexj",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    OutlinedButton(
                        onClick = {},
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("Edit", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                    }
                }
            }

            Box(modifier = Modifier.offset(y = (-28).dp)) {
                Column {
                    Text(
                        text = "Basketball & tennis enthusiast.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFF374151),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(label = "MATCHES", value = "42", modifier = Modifier.weight(1f))
                        StatCard(label = "AVG RATING", value = "4.8 ⭐", modifier = Modifier.weight(1f))
                        StatCard(label = "STREAK", value = "12 🔥", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "PLAYER RATINGS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.8.sp),
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "from 38 reviews",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextTertiary
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            RatingBar(label = "Skill", score = 4.7f, barColor = YellowBar)
                            Spacer(modifier = Modifier.height(10.dp))
                            RatingBar(label = "Sportsmanship", score = 4.9f, barColor = EmeraldBar)
                            Spacer(modifier = Modifier.height(10.dp))
                            RatingBar(label = "Punctuality", score = 5.0f, barColor = BlueBar)
                        }
                    }
                }
            }
        }
    }
}
