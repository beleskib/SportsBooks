package com.example.sportsbook.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.sportsbook.ui.screens.auth.LoginScreen
import com.example.sportsbook.ui.screens.auth.RegisterScreen
import com.example.sportsbook.ui.screens.onboarding.PartnerTypeSelectionScreen
import com.example.sportsbook.ui.screens.onboarding.RoleSelectionScreen
import com.example.sportsbook.ui.screens.partner.dashboard.PartnerDashboardScreen
import com.example.sportsbook.ui.screens.partner.setup.PartnerSetupScreen
import com.example.sportsbook.ui.screens.player.booking.BookingCalendarScreen
import com.example.sportsbook.ui.screens.player.booking.BookingConfirmationScreen
import com.example.sportsbook.ui.screens.player.coach.CoachDetailScreen
import com.example.sportsbook.ui.screens.player.coach.CoachListScreen
import com.example.sportsbook.ui.screens.player.home.PlayerHomeScreen
import com.example.sportsbook.ui.screens.player.mybookings.BookingDetailScreen
import com.example.sportsbook.ui.screens.player.mybookings.MyBookingsScreen
import com.example.sportsbook.ui.screens.player.review.WriteReviewScreen
import com.example.sportsbook.ui.screens.player.sport.SportDetailScreen
import com.example.sportsbook.ui.screens.player.venue.VenueDetailScreen
import com.example.sportsbook.ui.screens.player.venue.VenueListScreen
import com.example.sportsbook.ui.screens.splash.SplashScreen

@Composable
fun SportsBookNavHost() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    val showBottomBar = remember(currentBackStackEntry) {
        when (currentBackStackEntry?.destination?.route) {
            Route.PlayerHome::class.qualifiedName,
            Route.MyBookings::class.qualifiedName -> true
            else -> false
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentBackStackEntry?.destination?.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Route.PlayerHome) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Splash,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth flow
            composable<Route.Splash> {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(Route.Login) {
                            popUpTo(Route.Splash) { inclusive = true }
                        }
                    },
                    onNavigateToRoleSelection = {
                        navController.navigate(Route.RoleSelection) {
                            popUpTo(Route.Splash) { inclusive = true }
                        }
                    },
                    onNavigateToPlayerHome = {
                        navController.navigate(Route.PlayerHome) {
                            popUpTo(Route.Splash) { inclusive = true }
                        }
                    },
                    onNavigateToPartnerDashboard = {
                        navController.navigate(Route.PartnerDashboard) {
                            popUpTo(Route.Splash) { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.Login> {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Route.Register) },
                    onLoginSuccess = {
                        navController.navigate(Route.RoleSelection) {
                            popUpTo(Route.Login) { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.Register> {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Route.RoleSelection) {
                            popUpTo(Route.Login) { inclusive = true }
                        }
                    }
                )
            }

            // Onboarding
            composable<Route.RoleSelection> {
                RoleSelectionScreen(
                    onPlayerSelected = {
                        navController.navigate(Route.PlayerHome) {
                            popUpTo(Route.RoleSelection) { inclusive = true }
                        }
                    },
                    onPartnerSelected = {
                        navController.navigate(Route.PartnerTypeSelection)
                    }
                )
            }

            composable<Route.PartnerTypeSelection> {
                PartnerTypeSelectionScreen(
                    onVenueOwnerSelected = {
                        navController.navigate(Route.VenueSetup) {
                            popUpTo(Route.RoleSelection) { inclusive = true }
                        }
                    },
                    onCoachSelected = {
                        navController.navigate(Route.CoachSetup) {
                            popUpTo(Route.RoleSelection) { inclusive = true }
                        }
                    }
                )
            }

            // Player flow
            composable<Route.PlayerHome> {
                PlayerHomeScreen(
                    onSportClick = { sportType ->
                        navController.navigate(Route.SportDetail(sportType))
                    },
                    onVenueClick = { venueId ->
                        navController.navigate(Route.VenueDetail(venueId))
                    },
                    onCoachClick = { coachId ->
                        navController.navigate(Route.CoachDetail(coachId))
                    }
                )
            }

            composable<Route.SportDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.SportDetail>()
                SportDetailScreen(
                    sportType = route.sportType,
                    onVenueClick = { venueId -> navController.navigate(Route.VenueDetail(venueId)) },
                    onCoachClick = { coachId -> navController.navigate(Route.CoachDetail(coachId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.VenueList> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.VenueList>()
                VenueListScreen(
                    sportType = route.sportType,
                    onVenueClick = { venueId -> navController.navigate(Route.VenueDetail(venueId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.VenueDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.VenueDetail>()
                VenueDetailScreen(
                    venueId = route.venueId,
                    onBookClick = { navController.navigate(Route.BookingCalendar(venueId = route.venueId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.CoachList> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.CoachList>()
                CoachListScreen(
                    sportType = route.sportType,
                    onCoachClick = { coachId -> navController.navigate(Route.CoachDetail(coachId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.CoachDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.CoachDetail>()
                CoachDetailScreen(
                    coachId = route.coachId,
                    onBookClick = { navController.navigate(Route.BookingCalendar(coachId = route.coachId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.BookingCalendar> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.BookingCalendar>()
                BookingCalendarScreen(
                    venueId = route.venueId,
                    coachId = route.coachId,
                    onSlotSelected = { timeSlotId ->
                        navController.navigate(Route.BookingConfirmation(timeSlotId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.BookingConfirmation> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.BookingConfirmation>()
                BookingConfirmationScreen(
                    timeSlotId = route.timeSlotId,
                    onBookingConfirmed = {
                        navController.navigate(Route.MyBookings) {
                            popUpTo(Route.PlayerHome) { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.MyBookings> {
                MyBookingsScreen(
                    onBookingClick = { bookingId ->
                        navController.navigate(Route.BookingDetail(bookingId))
                    }
                )
            }

            composable<Route.BookingDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.BookingDetail>()
                BookingDetailScreen(
                    bookingId = route.bookingId,
                    onWriteReview = { bookingId ->
                        navController.navigate(Route.WriteReview(bookingId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.WriteReview> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.WriteReview>()
                WriteReviewScreen(
                    bookingId = route.bookingId,
                    onReviewSubmitted = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            // Partner flow
            composable<Route.VenueSetup> {
                PartnerSetupScreen(
                    isCoach = false,
                    onSetupComplete = {
                        navController.navigate(Route.PartnerDashboard) {
                            popUpTo(Route.VenueSetup) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.CoachSetup> {
                PartnerSetupScreen(
                    isCoach = true,
                    onSetupComplete = {
                        navController.navigate(Route.PartnerDashboard) {
                            popUpTo(Route.CoachSetup) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.PartnerDashboard> {
                PartnerDashboardScreen(
                    onSignOut = {
                        navController.navigate(Route.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
