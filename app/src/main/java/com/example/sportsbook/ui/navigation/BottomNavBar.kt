package com.example.sportsbook.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.sportsbook.ui.theme.SportsBookTheme

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Route
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, Route.PlayerHome),
    BottomNavItem("Bookings", Icons.Default.CalendarMonth, Route.MyBookings)
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (Route) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route::class.qualifiedName
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
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
