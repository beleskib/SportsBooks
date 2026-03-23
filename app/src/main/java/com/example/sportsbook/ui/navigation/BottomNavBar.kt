package com.example.sportsbook.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsbook.ui.theme.CoolGray
import com.example.sportsbook.ui.theme.Navy600
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Route
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, Route.PlayerHome),
    BottomNavItem("Bookings", Icons.Default.CalendarMonth, Route.MyBookings),
    BottomNavItem("Payments", Icons.Default.Payment, Route.PaymentHistory),
    BottomNavItem("Profile", Icons.Default.Person, Route.PlayerProfile)
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (Route) -> Unit
) {
    NavigationBar(
        containerColor = Navy900,
        contentColor = WarmWhite,
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
                        tint = if (isSelected) USOpenGold else CoolGray
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (isSelected) WarmWhite else CoolGray,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Navy600
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
