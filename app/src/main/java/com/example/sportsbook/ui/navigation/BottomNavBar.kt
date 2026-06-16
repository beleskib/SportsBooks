package com.example.sportsbook.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkNavBar
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.DarkTextTertiary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.SportsBookTheme

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Route
)

// ══════════════════════════════════════════════════════════════
// v3 Player Bottom Nav: Home | Explore | Play | Social | More
// ══════════════════════════════════════════════════════════════
val playerNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, Route.PlayerHome),
    BottomNavItem("Explore", Icons.Default.Explore, Route.Explore),
    BottomNavItem("Play", Icons.Default.SportsSoccer, Route.MatchList),
    BottomNavItem("Social", Icons.Default.People, Route.SocialHub),
    BottomNavItem("More", Icons.Default.MoreHoriz, Route.MoreMenu),
)

// ══════════════════════════════════════════════════════════════
// v3 Partner Bottom Nav: Dashboard | Bookings | Slots | Revenue | Settings
// ══════════════════════════════════════════════════════════════
val partnerNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Default.Dashboard, Route.PartnerDashboard),
    BottomNavItem("Bookings", Icons.Default.CalendarMonth, Route.PendingReservations),
    BottomNavItem("Slots", Icons.Default.Schedule, Route.TimeSlotManagement),
    BottomNavItem("Revenue", Icons.Default.Payments, Route.PartnerAnalytics),
    BottomNavItem("Settings", Icons.Default.Settings, Route.Settings),
)

// Keep backward compatibility
val bottomNavItems = playerNavItems

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (Route) -> Unit
) {
    NavigationBar(
        containerColor = DarkNavBar,
        contentColor = DarkTextSecondary,
        tonalElevation = 0.dp
    ) {
        playerNavItems.forEach { item ->
            val isSelected = currentRoute == item.route::class.qualifiedName
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) GreenAccent else DarkTextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (isSelected) GreenAccent else DarkTextTertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = GreenAccent.copy(alpha = 0.12f)
                )
            )
        }
    }
}

@Composable
fun PartnerBottomNavBar(
    currentRoute: String?,
    onNavigate: (Route) -> Unit
) {
    NavigationBar(
        containerColor = DarkNavBar,
        contentColor = DarkTextSecondary,
        tonalElevation = 0.dp
    ) {
        partnerNavItems.forEach { item ->
            val isSelected = currentRoute == item.route::class.qualifiedName
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) GreenAccent else DarkTextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (isSelected) GreenAccent else DarkTextTertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = GreenAccent.copy(alpha = 0.12f)
                )
            )
        }
    }
}

@Preview
@Composable
private fun BottomNavBarPreview() {
    SportsBookTheme {
        BottomNavBar(
            currentRoute = Route.PlayerHome::class.qualifiedName,
            onNavigate = {}
        )
    }
}

@Preview
@Composable
private fun PartnerBottomNavBarPreview() {
    SportsBookTheme {
        PartnerBottomNavBar(
            currentRoute = Route.PartnerDashboard::class.qualifiedName,
            onNavigate = {}
        )
    }
}
