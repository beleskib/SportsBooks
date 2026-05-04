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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.v2.ProfileVisibility
import com.example.sportsbook.domain.model.v2.SubscriptionStatus
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.CoolGray
import com.example.sportsbook.ui.theme.Navy600
import com.example.sportsbook.ui.theme.Navy700
import com.example.sportsbook.ui.theme.Navy800
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.SportGreen
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// SportsBooks+ brand colors (indigo → purple gradient like the web)
private val PlusIndigo = Color(0xFF4F46E5)
private val PlusPurple = Color(0xFF7C3AED)
private val PlusGoldBadge = Color(0xFFFDE68A)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToSportsIFollow: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onOpenCheckoutUrl: (String) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show toast via snackbar
    LaunchedEffect(uiState.toast) {
        uiState.toast?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    // Open checkout URL in browser when available
    LaunchedEffect(uiState.checkoutUrl) {
        uiState.checkoutUrl?.let { url ->
            onOpenCheckoutUrl(url)
            viewModel.clearCheckoutUrl()
        }
    }

    Scaffold(
        containerColor = Navy900,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.displayName ?: "No name set",
                            style = MaterialTheme.typography.titleMedium,
                            color = WarmWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        // Plus badge next to name
                        if (uiState.isPlus) {
                            Spacer(modifier = Modifier.width(8.dp))
                            PlusBadge()
                        }
                    }
                    Text(
                        text = uiState.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolGray
                    )
                }
            }

            HorizontalDivider(color = Navy600.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(8.dp))

            // ── SportsBooks+ Card ──
            SportsBooksPlus(
                isPlus = uiState.isPlus,
                subscription = uiState.subscription,
                isLoading = uiState.subscriptionLoading,
                actionLoading = uiState.actionLoading,
                onUpgrade = viewModel::startCheckout,
                onCancel = viewModel::cancelSubscription,
                onReactivate = viewModel::reactivateSubscription
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Navy600.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // ── Profile Visibility ──
            SectionHeader("PROFILE VISIBILITY")

            Text(
                text = if (uiState.isPlus) "Control who can see your profile."
                else "Upgrade to SportsBooks+ to unlock private & friends-only options.",
                style = MaterialTheme.typography.bodySmall,
                color = CoolGray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            ProfileVisibility.entries.forEach { visibility ->
                VisibilityOption(
                    visibility = visibility,
                    isSelected = uiState.profileVisibility == visibility,
                    isLocked = !uiState.isPlus && visibility != ProfileVisibility.PUBLIC,
                    isDisabled = uiState.actionLoading,
                    onClick = { viewModel.setVisibility(visibility) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
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

// ── SportsBooks+ subscription card ──
@Composable
private fun SportsBooksPlus(
    isPlus: Boolean,
    subscription: com.example.sportsbook.domain.model.v2.SubscriptionInfo?,
    isLoading: Boolean,
    actionLoading: Boolean,
    onUpgrade: () -> Unit,
    onCancel: () -> Unit,
    onReactivate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    Brush.horizontalGradient(listOf(PlusIndigo, PlusPurple))
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👑", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SportsBooks+",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            isPlus && subscription?.status == SubscriptionStatus.TRIALING -> "Free trial active"
                            isPlus -> "You're a Plus member"
                            else -> "Unlock premium features"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(Navy800)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PlusIndigo,
                        strokeWidth = 2.dp
                    )
                }
            } else if (isPlus) {
                // Active status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SportGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = SportGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = SportGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    subscription?.let { sub ->
                        val dateText = when {
                            sub.status == SubscriptionStatus.TRIALING && sub.trialEnd != null ->
                                "Trial ends ${formatDate(sub.trialEnd)}"
                            sub.cancelAtPeriodEnd ->
                                "Access until ${formatDate(sub.currentPeriodEnd)}"
                            else ->
                                "Renews ${formatDate(sub.currentPeriodEnd)}"
                        }
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = CoolGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Features
                PlusFeatureList()

                Spacer(modifier = Modifier.height(12.dp))

                // Cancel / Reactivate
                if (subscription?.cancelAtPeriodEnd == true) {
                    Button(
                        onClick = onReactivate,
                        enabled = !actionLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PlusIndigo
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (actionLoading) "Processing..." else "Reactivate Subscription",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    TextButton(
                        onClick = onCancel,
                        enabled = !actionLoading
                    ) {
                        Text(
                            "Cancel subscription",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoolGray
                        )
                    }
                }
            } else {
                // Upsell
                Text(
                    text = "Upgrade for a better experience. Start with a 7-day free trial.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoolGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                PlusFeatureList()

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onUpgrade,
                    enabled = !actionLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(listOf(PlusIndigo, PlusPurple))
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (actionLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Starting checkout...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            Text(
                                "Start 7-day free trial",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlusFeatureList() {
    val features = listOf(
        "0% service fee on all bookings",
        "Priority matchmaking ranking",
        "24h cancellation window (vs 48h)",
        "Advanced player stats & insights",
        "Profile visibility controls",
        "SportsBooks+ badge on profile"
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        features.forEach { feature ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = PlusIndigo
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmWhite.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ── Visibility option radio button ──
@Composable
private fun VisibilityOption(
    visibility: ProfileVisibility,
    isSelected: Boolean,
    isLocked: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PlusIndigo else Navy600,
        label = "borderColor"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PlusIndigo.copy(alpha = 0.1f) else Navy800,
        label = "bgColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor)
            .then(
                if (isDisabled || isLocked) Modifier
                else Modifier.clickable(onClick = onClick)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        val icon = when (visibility) {
            ProfileVisibility.PUBLIC -> Icons.Default.Visibility
            ProfileVisibility.FRIENDS_ONLY -> Icons.Default.Person
            ProfileVisibility.PRIVATE -> Icons.Default.VisibilityOff
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) PlusIndigo.copy(alpha = 0.15f) else Navy700),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) PlusIndigo else CoolGray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Label + description
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = visibility.displayLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) WarmWhite else WarmWhite.copy(alpha = if (isLocked) 0.5f else 0.85f),
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                if (isLocked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PlusIndigo.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Plus",
                            style = MaterialTheme.typography.labelSmall,
                            color = PlusIndigo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            Text(
                text = visibility.description,
                style = MaterialTheme.typography.bodySmall,
                color = CoolGray.copy(alpha = if (isLocked) 0.5f else 1f),
                fontSize = 11.sp
            )
        }

        // Selection indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(PlusIndigo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
            }
        }
    }
}

// ── Plus badge chip ──
@Composable
fun PlusBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.horizontalGradient(listOf(PlusIndigo, PlusPurple))
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("👑", fontSize = 10.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "PLUS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// ── Shared composables ──

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

private fun formatDate(isoDate: String): String {
    return try {
        val date = LocalDate.parse(isoDate.substringBefore("T"))
        date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } catch (_: Exception) {
        isoDate.substringBefore("T")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun SettingsScreenPreview() {
    SportsBookTheme {
        SettingsScreen(onBack = {})
    }
}
