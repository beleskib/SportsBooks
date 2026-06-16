package com.example.sportsbook.ui.screens.player.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent

// ── Screen ───────────────────────────────────────────────────────────────────

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

    // Local toggle states (UI-only preferences)
    var pushNotificationsOn by remember { mutableStateOf(true) }
    var chatNotificationsOn by remember { mutableStateOf(true) }
    var dealAlertsOn by remember { mutableStateOf(false) }
    var emailNotificationsOn by remember { mutableStateOf(true) }
    var showLocationOn by remember { mutableStateOf(true) }
    var showActivityOn by remember { mutableStateOf(true) }

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

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // ── Header ───────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                }
            }

            // ── Account ──────────────────────────────────────────────────
            item { SectionLabel("Account") }
            item {
                SettingsGroup {
                    SettingsNavRow(
                        icon = "👤", iconBg = Color(0xFF1B3A1E),
                        title = "Edit Profile", subtitle = "Name, photo, bio",
                        onClick = onNavigateToEditProfile,
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = "📧", iconBg = Color(0xFF1B2D3A),
                        title = "Email", subtitle = uiState.email.ifBlank { "Not set" },
                        onClick = {},
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = "🔒", iconBg = Color(0xFF3A2E1B),
                        title = "Password", subtitle = "Change password",
                        onClick = {},
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = "💳", iconBg = Color(0xFF3A1B3A),
                        title = "Payment Methods", subtitle = "Manage cards & wallets",
                        onClick = {},
                    )
                }
            }

            // ── Notifications ─────────────────────────────────────────────
            item { SectionLabel("Notifications") }
            item {
                SettingsGroup {
                    SettingsToggleRow(
                        icon = "🔔", iconBg = Color(0xFF1B3A1E),
                        title = "Push Notifications",
                        checked = pushNotificationsOn,
                        onCheckedChange = { pushNotificationsOn = it },
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = "💬", iconBg = Color(0xFF1B2D3A),
                        title = "Match Chat Notifications",
                        checked = chatNotificationsOn,
                        onCheckedChange = { chatNotificationsOn = it },
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = "🔥", iconBg = Color(0xFF3A2E1B),
                        title = "Deal Alerts",
                        checked = dealAlertsOn,
                        onCheckedChange = { dealAlertsOn = it },
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = "📧", iconBg = Color(0xFF1E1E1E),
                        title = "Email Notifications",
                        checked = emailNotificationsOn,
                        onCheckedChange = { emailNotificationsOn = it },
                    )
                }
            }

            // ── Preferences ───────────────────────────────────────────────
            item { SectionLabel("Preferences") }
            item {
                SettingsGroup {
                    SettingsNavRow(
                        icon = "🌍", iconBg = Color(0xFF1E1E1E),
                        title = "Language", value = "English",
                        onClick = {},
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = "🌙", iconBg = Color(0xFF1E1E1E),
                        title = "Appearance", value = "Dark",
                        onClick = {},
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = "📍", iconBg = Color(0xFF1E1E1E),
                        title = "Location", subtitle = "Skopje, North Macedonia",
                        onClick = {},
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = "🏀", iconBg = Color(0xFF1E1E1E),
                        title = "Sports I Follow",
                        subtitle = if (uiState.interestedSports.isNotEmpty())
                            uiState.interestedSports.take(3).joinToString(", ") { it.displayName }
                        else "Basketball, Football, Tennis",
                        onClick = onNavigateToSportsIFollow,
                    )
                }
            }

            // ── Privacy ───────────────────────────────────────────────────
            item { SectionLabel("Privacy") }
            item {
                SettingsGroup {
                    SettingsNavRow(
                        icon = "👁", iconBg = Color(0xFF1E1E1E),
                        title = "Profile Visibility",
                        value = uiState.profileVisibility.displayLabel,
                        onClick = {},
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = "📍", iconBg = Color(0xFF1E1E1E),
                        title = "Show Location",
                        checked = showLocationOn,
                        onCheckedChange = { showLocationOn = it },
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = "📊", iconBg = Color(0xFF1E1E1E),
                        title = "Show Activity Status",
                        checked = showActivityOn,
                        onCheckedChange = { showActivityOn = it },
                    )
                }
            }

            // ── Support ───────────────────────────────────────────────────
            item { SectionLabel("Support") }
            item {
                SettingsGroup {
                    SettingsNavRow(icon = "❓", iconBg = Color(0xFF1E1E1E), title = "Help & FAQ", onClick = {})
                    SettingsDivider()
                    SettingsNavRow(icon = "💬", iconBg = Color(0xFF1E1E1E), title = "Contact Us", onClick = {})
                    SettingsDivider()
                    SettingsNavRow(icon = "📄", iconBg = Color(0xFF1E1E1E), title = "Terms & Privacy", onClick = {})
                }
            }

            // ── Danger zone ───────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .clickable {
                                viewModel.signOut()
                                onSignOut()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Log Out", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF44336))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Transparent)
                            .clickable { /* delete account */ }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Delete Account", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF44336))
                    }
                }
            }

            // ── Version ───────────────────────────────────────────────────
            item {
                Text(
                    text = "SportsBooks v2.0.0 (Build 42)",
                    fontSize = 12.sp,
                    color = Color(0xFF444444),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = DarkTextSecondary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface),
    ) {
        content()
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DarkBorder),
    )
}

@Composable
private fun SettingsNavRow(
    icon: String,
    iconBg: Color,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = DarkTextPrimary)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 1.dp))
            }
        }
        if (value != null) {
            Text(value, fontSize = 13.sp, color = DarkTextSecondary)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text("›", fontSize = 20.sp, color = Color(0xFF444444))
    }
}

@Composable
private fun SettingsToggleRow(
    icon: String,
    iconBg: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = DarkTextPrimary, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GreenAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF333333),
            ),
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(onBack = {})
}
