package com.example.sportsbook.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
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
import com.example.sportsbook.ui.theme.CardWhite
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.TextPrimary
import com.example.sportsbook.ui.theme.TextSecondary
import com.example.sportsbook.ui.theme.TextTertiary

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Route
)

// Profile pinned LAST per UX convention — users expect account/profile
// to live at the far right of the tab bar (matches Instagram, X, Strava, etc.).
val playerNavItems = listOf(
    BottomNavItem("Discover", Icons.Default.Home, Route.PlayerHome),
    BottomNavItem("Feed", Icons.Default.DynamicFeed, Route.NewsFeed),
    BottomNavItem("Bookings", Icons.Default.CalendarMonth, Route.MyBookings),
    // v2-practical-ux
    BottomNavItem("Play", Icons.Default.SportsSoccer, Route.V2PlayHome),
    BottomNavItem("Profile", Icons.Default.Person, Route.PlayerProfile)
)

val partnerNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Default.Dashboard, Route.PartnerDashboard),
    BottomNavItem("Reservations", Icons.Default.Inbox, Route.PendingReservations),
    BottomNavItem("Analytics", Icons.AutoMirrored.Filled.TrendingUp, Route.PartnerAnalytics),
    // v2-practical-ux
    BottomNavItem("Calendar", Icons.Default.CalendarViewWeek, Route.V2WeeklyCalendar)
)

// Keep backward compatibility
val bottomNavItems = playerNavItems

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (Route) -> Unit
) {
    NavigationBar(
        containerColor = CardWhite,
        contentColor = TextPrimary,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route::class.qualifiedName
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) TextPrimary else TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (isSelected) TextPrimary else TextTertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = GoldAccent.copy(alpha = 0.15f)
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
        containerColor = CardWhite,
        contentColor = TextPrimary,
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
                        tint = if (isSelected) TextPrimary else TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (isSelected) TextPrimary else TextTertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = GoldAccent.copy(alpha = 0.15f)
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
