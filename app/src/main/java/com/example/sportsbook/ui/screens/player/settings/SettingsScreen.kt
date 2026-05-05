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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.sportsbook.ui.theme.SportsBookTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── Light-theme design tokens ──
private val LightBg = Color(0xFFF9FAFB)
private val CardBg = Color.White
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val TextTertiary = Color(0xFF9CA3AF)
private val GoldAccent = Color(0xFFFDE047)
private val GoldDark = Color(0xFFEAB308)
private val NavBarBg = Color(0xFF111827)
private val RoseLight = Color(0xFFFECDD3)   // rose-200
private val RoseText = Color(0xFFBE123C)    // rose-700
private val EmeraldBg = Color(0xFFD1FAE5)   // emerald-100
private val EmeraldText = Color(0xFF047857) // emerald-700
private val PurpleBg = Color(0xFFF3E8FF)    // purple-100
private val PurpleText = Color(0xFF7C3AED)  // purple-700
private val BlueBg = Color(0xFFDBEAFE)      // blue-100
private val BlueText = Color(0xFF1D4ED8)    // blue-700
private val YellowBg = Color(0xFFFEF9C3)   // yellow-100 (icon bg)
private val YellowText = Color(0xFFA16207)  // yellow-700

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

    LaunchedEffect(uiState.toast) {
        uiState.toast?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    LaunchedEffect(uiState.checkoutUrl) {
        uiState.checkoutUrl?.let { url ->
            onOpenCheckoutUrl(url)
            viewModel.clearCheckoutUrl()
        }
    }

    Scaffold(
        containerColor = LightBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavBarBg
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
                .background(LightBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── SportsBooks+ card ──
            SportsBooksPlus(
                isPlus = uiState.isPlus,
                subscription = uiState.subscription,
                isLoading = uiState.subscriptionLoading,
                actionLoading = uiState.actionLoading,
                onUpgrade = viewModel::startCheckout,
                onCancel = viewModel::cancelSubscription,
                onReactivate = viewModel::reactivateSubscription
            )

            // ── Grouped card 1: Main settings ──
            SettingsCard {
                val sportsSubtitle = when {
                    uiState.interestedSports.isEmpty() -> "None picked"
                    else -> "${uiState.interestedSports.size} picked"
                }
                SettingsRow(
                    icon = Icons.Default.Person,
                    label = "Account",
                    onClick = onNavigateToEditProfile
                )
                CardDivider()
                SettingsRow(
                    icon = Icons.Default.SportsSoccer,
                    label = "Sports I follow",
                    subtitle = sportsSubtitle,
                    onClick = onNavigateToSportsIFollow
                )
                CardDivider()
                SettingsRow(
                    icon = Icons.Default.Notifications,
                    label = "Notifications",
                    onClick = { /* TODO Phase 3 */ }
                )
                CardDivider()
                SettingsRow(
                    icon = Icons.Default.CreditCard,
                    label = "Payment methods",
                    onClick = { /* TODO */ }
                )
                CardDivider()
                SettingsRow(
                    icon = Icons.Default.Lock,
                    label = "Privacy & visibility",
                    onClick = { /* TODO: navigate to dedicated privacy screen */ }
                )
                CardDivider()
                SettingsRow(
                    icon = Icons.Default.Language,
                    label = "Language",
                    subtitle = "English",
                    onClick = { /* TODO */ }
                )
            }

            // ── Grouped card 2: Preferences ──
            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Straighten,
                    label = "Units & distance",
                    subtitle = "km",
                    onClick = { /* TODO */ }
                )
                CardDivider()
                SettingsRow(
                    icon = Icons.Default.Star,
                    label = "Appearance",
                    subtitle = "System",
                    onClick = { /* TODO */ }
                )
            }

            // ── Grouped card 3: Legal ──
            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Info,
                    label = "Terms of service",
                    onClick = { /* TODO */ }
                )
                CardDivider()
                SettingsRow(
                    icon = Icons.Default.Shield,
                    label = "Privacy policy",
                    onClick = { /* TODO */ }
                )
                CardDivider()
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.Help,
                    label = "Help & support",
                    onClick = { /* TODO */ }
                )
            }

            // ── Profile Visibility (inline, light theme) ──
            VisibilitySection(
                isPlus = uiState.isPlus,
                profileVisibility = uiState.profileVisibility,
                actionLoading = uiState.actionLoading,
                onSetVisibility = viewModel::setVisibility
            )

            // ── Sports I Follow preview chips ──
            if (uiState.interestedSports.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBg)
                        .clickable(onClick = onNavigateToSportsIFollow)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "SPORTS I FOLLOW",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.interestedSports.forEach { sport ->
                            SportChip(sport = sport)
                        }
                    }
                }
            }

            // ── Sign out ──
            OutlinedButton(
                onClick = {
                    viewModel.signOut()
                    onSignOut()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardBg,
                    contentColor = RoseText
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoseLight)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = RoseText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sign out",
                    color = RoseText,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Profile visibility inline section ──
@Composable
private fun VisibilitySection(
    isPlus: Boolean,
    profileVisibility: ProfileVisibility,
    actionLoading: Boolean,
    onSetVisibility: (ProfileVisibility) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "PROFILE VISIBILITY",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isPlus) "Control who can see your profile."
            else "Upgrade to SportsBooks+ to unlock private & friends-only options.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ProfileVisibility.entries.forEachIndexed { index, visibility ->
            if (index > 0) Spacer(modifier = Modifier.height(8.dp))
            VisibilityOption(
                visibility = visibility,
                isSelected = profileVisibility == visibility,
                isLocked = !isPlus && visibility != ProfileVisibility.PUBLIC,
                isDisabled = actionLoading,
                onClick = { onSetVisibility(visibility) }
            )
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
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(NavBarBg)
    ) {
        // Header region
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = "SportsBooks",
                style = MaterialTheme.typography.labelMedium,
                color = GoldAccent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Plus +",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Play more, pay less. Match faster.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD1D5DB) // gray-300
            )
        }

        // White inner card with features + CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardBg)
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
                        color = GoldDark,
                        strokeWidth = 2.dp
                    )
                }
            } else if (isPlus) {
                // ── Active subscriber view ──
                PlusActiveView(
                    subscription = subscription,
                    actionLoading = actionLoading,
                    onCancel = onCancel,
                    onReactivate = onReactivate
                )
            } else {
                // ── Upsell feature list + CTA ──
                PlusUpsellView(
                    actionLoading = actionLoading,
                    onUpgrade = onUpgrade
                )
            }
        }
    }
}

@Composable
private fun PlusUpsellView(
    actionLoading: Boolean,
    onUpgrade: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PlusFeatureRow(
            iconBg = YellowBg,
            iconTint = YellowText,
            icon = Icons.Default.SportsScore,
            title = "Priority matchmaking",
            description = "Jump the queue and get matched faster with top players."
        )
        PlusFeatureRow(
            iconBg = EmeraldBg,
            iconTint = EmeraldText,
            icon = Icons.Default.CreditCard,
            title = "Zero service fees",
            description = "Keep 100% of your booking price — no platform cut."
        )
        PlusFeatureRow(
            iconBg = PurpleBg,
            iconTint = PurpleText,
            icon = Icons.Default.Star,
            title = "Advanced stats",
            description = "Deep performance insights and trend analysis."
        )
        PlusFeatureRow(
            iconBg = BlueBg,
            iconTint = BlueText,
            icon = Icons.Default.Check,
            title = "Free cancel up to 1h",
            description = "Cancel within 1 hour of kick-off at no charge."
        )
        PlusFeatureRow(
            iconBg = YellowBg,
            iconTint = YellowText,
            icon = Icons.Default.Person,
            title = "+ Member badge",
            description = "Stand out with an exclusive gold member badge."
        )

        Spacer(modifier = Modifier.height(4.dp))

        // CTA button — gold gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(GoldAccent, Color(0xFFFACC15), GoldDark)
                    )
                )
                .clickable(enabled = !actionLoading, onClick = onUpgrade)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (actionLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Starting checkout...",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    "Try free for 7 days →",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PlusActiveView(
    subscription: com.example.sportsbook.domain.model.v2.SubscriptionInfo?,
    actionLoading: Boolean,
    onCancel: () -> Unit,
    onReactivate: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Welcome + active pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Welcome to Plus",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldDark,
                    fontWeight = FontWeight.Bold
                )
                subscription?.let { sub ->
                    val planLabel = "Monthly · 499 ден"
                    val dateText = when {
                        sub.status == SubscriptionStatus.TRIALING && sub.trialEnd != null ->
                            "Trial ends ${formatDate(sub.trialEnd)}"
                        sub.cancelAtPeriodEnd ->
                            "Access until ${formatDate(sub.currentPeriodEnd)}"
                        else ->
                            "Renews ${formatDate(sub.currentPeriodEnd)}"
                    }
                    Text(
                        text = planLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
            // "Active" pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(GoldAccent)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        HorizontalDivider(color = Color(0xFFF3F4F6))

        // Active perks
        val perks = listOf(
            "Priority matchmaking",
            "Zero service fees on all bookings",
            "Advanced player stats",
            "Free cancel up to 1h before",
            "Profile visibility controls",
            "+ Member badge on profile"
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            perks.forEach { perk ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GoldDark)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = perk,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFF3F4F6))

        // Actions
        if (subscription?.cancelAtPeriodEnd == true) {
            Button(
                onClick = onReactivate,
                enabled = !actionLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (actionLoading) "Processing..." else "Reactivate subscription",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: switch to annual */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB))
                ) {
                    Text(
                        "Switch to annual",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(
                    onClick = onCancel,
                    enabled = !actionLoading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseText),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (actionLoading) "..." else "Cancel renewal",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PlusFeatureRow(
    iconBg: Color,
    iconTint: Color,
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = iconTint
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

// ── Visibility option radio row ──
@Composable
private fun VisibilityOption(
    visibility: ProfileVisibility,
    isSelected: Boolean,
    isLocked: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    val ringColor by animateColorAsState(
        targetValue = if (isSelected) TextPrimary else Color(0xFFD1D5DB),
        label = "visibilityRing"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (isSelected) Color(0xFFD1D5DB) else Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFF9FAFB) else Color.White)
            .then(
                if (isDisabled || isLocked) Modifier
                else Modifier.clickable(onClick = onClick)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = visibility.displayLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isLocked) TextTertiary else TextPrimary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                if (isLocked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(YellowBg)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Plus",
                            style = MaterialTheme.typography.labelSmall,
                            color = YellowText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            Text(
                text = visibility.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Radio circle indicator
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 0.dp else 2.dp,
                    color = ringColor,
                    shape = CircleShape
                )
                .background(if (isSelected) TextPrimary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GoldAccent)
                )
            }
        }
    }
}

// ── Plus badge chip (gold pill, "+ Member") ──
@Composable
fun PlusBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Brush.linearGradient(listOf(GoldAccent, Color(0xFFFACC15), GoldDark)))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "+ Member",
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
    }
}

// ── Shared card container ──
@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
    ) {
        content()
    }
}

@Composable
private fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color(0xFFF3F4F6),
        thickness = 1.dp
    )
}

// ── Settings row (icon + label + optional subtitle + chevron) ──
@Composable
private fun SettingsRow(
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
        // Icon container
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LightBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = TextSecondary
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Normal
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    fontSize = 12.sp
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = TextTertiary
        )
    }
}

// ── Sport chip ──
@Composable
private fun SportChip(sport: SportType) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(LightBg)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = sport.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
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

@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun SettingsScreenPreview() {
    SportsBookTheme {
        SettingsScreen(onBack = {})
    }
}
