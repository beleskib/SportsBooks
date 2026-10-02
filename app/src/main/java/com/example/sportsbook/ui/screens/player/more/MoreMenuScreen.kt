package com.example.sportsbook.ui.screens.player.more

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun MoreMenuScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToBookings: () -> Unit = {},
    onNavigateToPayments: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToXpLevel: () -> Unit = {},
    onNavigateToCommunities: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: MoreMenuViewModel = hiltViewModel(),
) {
    val menuState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = menuState.user
    val stats = menuState.stats
    val level = menuState.level
    val userName = user?.displayName ?: "Player"
    val userInitial = userName.firstOrNull()?.uppercase() ?: "?"
    var darkModeOn by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val currentLevel = level?.currentLevel ?: 1
    val totalXp = level?.totalXp ?: 0
    val xpToNext = level?.xpToNextLevel ?: 100
    val xpProgress = if (xpToNext > 0) (totalXp.toFloat() / xpToNext.toFloat()).coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Profile header ────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar
                Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(GreenAccent, Color(0xFF2196F3))))
                            .border(3.dp, GreenAccent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(userInitial, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700))
                            .border(2.dp, DarkBg, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$currentLevel", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(userName, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkTextPrimary)
                    Text(
                        user?.email ?: "",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.1f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(xpProgress)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Brush.horizontalGradient(listOf(GreenAccent, Color(0xFFFFD700)))),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$totalXp / $xpToNext XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable(onClick = onNavigateToProfile),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✏️", fontSize = 16.sp)
                }
            }
        }

        // ── Quick stats ───────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    Triple("${stats?.totalMatches ?: 0}", "Matches", GreenAccent),
                    Triple("${stats?.totalBookings ?: 0}", "Bookings", Color(0xFF2196F3)),
                    Triple("${stats?.totalReviews ?: 0}", "Reviews", Color(0xFFFFD700)),
                    Triple("${stats?.totalFriends ?: 0}", "Friends", Color(0xFFCE93D8)),
                ).forEach { (value, label, color) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
                        Text(
                            text = label.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
        }

        // Divider
        item { Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 20.dp).background(Color.White.copy(alpha = 0.06f))) }

        // ── Account section ───────────────────────────────────────────
        item { MoreSectionLabel("Account") }
        item {
            MoreMenuRow(icon = "👤", iconBgKey = "blue", title = "Profile", subtitle = "Edit your info, photo & sports", onClick = onNavigateToProfile)
            MoreMenuRow(icon = "🏆", iconBgKey = "gold", title = "Achievements", subtitle = "${menuState.achievementsEarned} of ${menuState.achievementsTotal} unlocked", badge = if (menuState.achievementsEarned > 0) "${menuState.achievementsEarned}" else null, badgeGreen = true, onClick = onNavigateToAchievements)
            MoreMenuRow(icon = "📊", iconBgKey = "green", title = "Stats & Activity", subtitle = "Match history, XP breakdown", onClick = onNavigateToStats)
            MoreMenuRow(icon = "⚡", iconBgKey = "teal", title = "XP & Levels", subtitle = "Your progress, rewards & rank", onClick = onNavigateToXpLevel)
            MoreMenuRow(icon = "⭐", iconBgKey = "orange", title = "My Reviews", subtitle = "Reviews you've given & received", onClick = { Toast.makeText(context, "My Reviews coming soon", Toast.LENGTH_SHORT).show() })
        }

        // Divider
        item { Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 20.dp).background(Color.White.copy(alpha = 0.06f))) }

        // ── Activity section ──────────────────────────────────────────
        item { MoreSectionLabel("Activity") }
        item {
            MoreMenuRow(icon = "📅", iconBgKey = "purple", title = "My Bookings", subtitle = "Upcoming & past reservations", badge = if ((stats?.totalBookings ?: 0) > 0) "${stats?.totalBookings}" else null, onClick = onNavigateToBookings)
            MoreMenuRow(icon = "⚡", iconBgKey = "teal", title = "My Matches", subtitle = "Active & completed matches", onClick = { Toast.makeText(context, "My Matches coming soon", Toast.LENGTH_SHORT).show() })
            MoreMenuRow(icon = "👥", iconBgKey = "blue", title = "Communities", subtitle = "Join or create a sports group", onClick = onNavigateToCommunities)
            MoreMenuRow(icon = "💳", iconBgKey = "pink", title = "Payments", subtitle = "Transaction history", onClick = onNavigateToPayments)
            MoreMenuRow(icon = "🏦", iconBgKey = "teal", title = "Payment Methods", subtitle = "Manage saved cards", onClick = onNavigateToPaymentMethods)
        }

        // Divider
        item { Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 20.dp).background(Color.White.copy(alpha = 0.06f))) }

        // ── Preferences section ───────────────────────────────────────
        item { MoreSectionLabel("Preferences") }
        item {
            MoreMenuRow(icon = "🔔", iconBgKey = "grey", title = "Notifications", subtitle = "Push, email & in-app alerts", onClick = onNavigateToNotifications)
            MoreMenuRowToggle(icon = "🎨", iconBgKey = "grey", title = "Appearance", subtitle = "Dark mode, theme colors", checked = darkModeOn, onCheckedChange = { darkModeOn = it })
            MoreMenuRow(icon = "🌐", iconBgKey = "grey", title = "Language", subtitle = "English", onClick = { Toast.makeText(context, "Language settings coming soon", Toast.LENGTH_SHORT).show() })
            MoreMenuRow(icon = "⚙️", iconBgKey = "grey", title = "Settings", subtitle = "Account, privacy, security", onClick = onNavigateToSettings)
        }

        // Divider
        item { Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 20.dp).background(Color.White.copy(alpha = 0.06f))) }

        // ── Support section ───────────────────────────────────────────
        item { MoreSectionLabel("Support") }
        item {
            MoreMenuRow(icon = "💬", iconBgKey = "green", title = "Help & FAQ", onClick = { Toast.makeText(context, "Help & FAQ coming soon", Toast.LENGTH_SHORT).show() })
            MoreMenuRow(icon = "📧", iconBgKey = "blue", title = "Contact Us", onClick = { Toast.makeText(context, "Contact us coming soon", Toast.LENGTH_SHORT).show() })
            MoreMenuRow(icon = "🚪", iconBgKey = "red", title = "Log Out", titleColor = Color(0xFFEF5350), onClick = onSignOut, showArrow = false)
        }

        // ── Version ───────────────────────────────────────────────────
        item {
            Text(
                text = "SportsBook v2.1.0 (build 47)",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun MoreSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = 0.35f),
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp),
    )
}

@Composable
private fun MoreMenuRow(
    icon: String,
    iconBgKey: String,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    badgeGreen: Boolean = false,
    titleColor: Color = DarkTextPrimary,
    showArrow: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor(iconBgKey)),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(top = 1.dp))
            }
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (badgeGreen) GreenAccent else Color(0xFFE91E63))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (showArrow) {
            Text("›", fontSize = 18.sp, color = Color.White.copy(alpha = 0.2f))
        }
    }
}

@Composable
private fun MoreMenuRowToggle(
    icon: String,
    iconBgKey: String,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor(iconBgKey)),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(top = 1.dp))
            }
        }
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

private fun iconBgColor(key: String): Color = when (key) {
    "green" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
    "blue" -> Color(0xFF2196F3).copy(alpha = 0.15f)
    "orange" -> Color(0xFFFF9800).copy(alpha = 0.15f)
    "purple" -> Color(0xFF9C27B0).copy(alpha = 0.15f)
    "red" -> Color(0xFFF44336).copy(alpha = 0.15f)
    "gold" -> Color(0xFFFFD700).copy(alpha = 0.15f)
    "teal" -> Color(0xFF00BCD4).copy(alpha = 0.15f)
    "pink" -> Color(0xFFE91E63).copy(alpha = 0.15f)
    else -> Color(0xFF9E9E9E).copy(alpha = 0.15f)
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun MoreMenuScreenPreview() {
    MoreMenuScreen()
}
