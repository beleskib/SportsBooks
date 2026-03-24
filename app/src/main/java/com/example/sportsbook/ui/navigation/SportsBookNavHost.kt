package com.example.sportsbook.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.sportsbook.MainActivity
import com.example.sportsbook.NotificationDeepLink
import kotlinx.coroutines.flow.StateFlow
import com.example.sportsbook.ui.screens.auth.LoginScreen
import com.example.sportsbook.ui.screens.auth.RegisterScreen
import com.example.sportsbook.ui.screens.onboarding.PartnerTypeSelectionScreen
import com.example.sportsbook.ui.screens.onboarding.RoleSelectionScreen
import com.example.sportsbook.ui.screens.partner.dashboard.PartnerDashboardScreen
import com.example.sportsbook.ui.screens.partner.images.ManageImagesScreen
import com.example.sportsbook.ui.screens.partner.setup.PartnerSetupScreen
import com.example.sportsbook.ui.screens.player.booking.BookingCalendarScreen
import com.example.sportsbook.ui.screens.player.booking.BookingChatScreen
import com.example.sportsbook.ui.screens.player.booking.BookingConfirmationScreen
import com.example.sportsbook.ui.screens.player.coach.CoachDetailScreen
import com.example.sportsbook.ui.screens.player.coach.CoachListScreen
import com.example.sportsbook.ui.screens.player.feed.NewsFeedScreen
import com.example.sportsbook.ui.screens.player.home.AllSportsScreen
import com.example.sportsbook.ui.screens.player.home.PlayerHomeScreen
import com.example.sportsbook.ui.screens.player.notifications.NotificationsScreen
import com.example.sportsbook.ui.screens.player.onboarding.PlayerOnboardingScreen
import com.example.sportsbook.ui.screens.player.profile.PlayerProfileScreen
import com.example.sportsbook.ui.screens.player.settings.SettingsScreen
import com.example.sportsbook.ui.screens.player.mybookings.BookingDetailScreen
import com.example.sportsbook.ui.screens.player.mybookings.MyBookingsScreen
import com.example.sportsbook.ui.screens.player.review.WriteReviewScreen
import com.example.sportsbook.ui.screens.player.sport.SportDetailScreen
import com.example.sportsbook.ui.screens.player.venue.VenueDetailScreen
import com.example.sportsbook.ui.screens.player.payment.PaymentCheckoutScreen
import com.example.sportsbook.ui.screens.player.payment.PaymentDetailScreen
import com.example.sportsbook.ui.screens.player.payment.PaymentHistoryScreen
import com.example.sportsbook.ui.screens.player.venue.VenueListScreen
import com.example.sportsbook.ui.screens.player.match.MatchListScreen
import com.example.sportsbook.ui.screens.player.match.MatchDetailScreen
import com.example.sportsbook.ui.screens.player.match.CreateMatchScreen
import com.example.sportsbook.ui.screens.player.match.MatchChatScreen
import com.example.sportsbook.ui.screens.player.match.RatePlayersScreen
import com.example.sportsbook.ui.screens.player.match.MatchMapScreen
import com.example.sportsbook.ui.screens.player.match.AvailablePlayersScreen
import com.example.sportsbook.ui.screens.player.venue.VenueMapScreen
import com.example.sportsbook.ui.screens.player.profile.PublicPlayerProfileScreen
import com.example.sportsbook.ui.screens.player.search.SearchScreen
import com.example.sportsbook.ui.screens.player.favorites.FavoritesScreen
import com.example.sportsbook.ui.screens.player.friends.FriendsListScreen
import com.example.sportsbook.ui.screens.player.friends.FriendRequestsScreen
import com.example.sportsbook.ui.screens.player.friends.AddFriendScreen
import com.example.sportsbook.ui.screens.player.party.PartyCreateScreen
import com.example.sportsbook.ui.screens.player.party.PartyDetailScreen
import com.example.sportsbook.ui.screens.player.party.PartyInviteMembersScreen
import com.example.sportsbook.ui.screens.partner.edit.EditCoachScreen
import com.example.sportsbook.ui.screens.partner.edit.EditVenueScreen
import com.example.sportsbook.ui.screens.partner.reservations.PendingReservationsScreen
import com.example.sportsbook.ui.screens.partner.stripe.StripeConnectScreen
import com.example.sportsbook.ui.screens.partner.timeslots.TimeSlotManagementScreen
import com.example.sportsbook.ui.screens.player.gamification.PlayerXpLevelScreen
import com.example.sportsbook.ui.screens.player.gamification.PlayerAchievementsScreen
import com.example.sportsbook.ui.screens.player.gamification.PlayerStatsScreen
import com.example.sportsbook.ui.screens.partner.analytics.PartnerAnalyticsScreen
import com.example.sportsbook.ui.screens.splash.SplashScreen

@Composable
fun SportsBookNavHost(
    pendingDeepLink: StateFlow<NotificationDeepLink?>? = null,
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current

    // Handle notification deep links
    val deepLink = pendingDeepLink?.collectAsStateWithLifecycle()
    LaunchedEffect(deepLink?.value) {
        val link = deepLink?.value ?: return@LaunchedEffect
        // Only navigate if we're past the splash/login screens
        val current = navController.currentDestination?.route ?: return@LaunchedEffect
        val isOnMainScreen = current.contains("PlayerHome") ||
            current.contains("PartnerDashboard") ||
            current.contains("MyBookings") ||
            current.contains("NewsFeed") ||
            current.contains("PendingReservations")
        if (!isOnMainScreen) return@LaunchedEffect

        when {
            link.type.startsWith("booking_") && link.bookingId != null -> {
                navController.navigate(Route.BookingDetail(link.bookingId))
            }
            link.type == "booking_request" && link.bookingId != null -> {
                navController.navigate(Route.PendingReservations)
            }
            link.type.startsWith("match_") && link.matchId != null -> {
                navController.navigate(Route.MatchDetail(link.matchId))
            }
        }

        // Consume the deep link so it doesn't re-navigate
        (context as? MainActivity)?.consumeDeepLink()
    }

    val currentRoute = currentBackStackEntry?.destination?.route

    val showPlayerBottomBar = remember(currentBackStackEntry) {
        when (currentRoute) {
            Route.PlayerHome::class.qualifiedName,
            Route.NewsFeed::class.qualifiedName,
            Route.MyBookings::class.qualifiedName,
            Route.PlayerProfile::class.qualifiedName -> true
            else -> false
        }
    }

    val showPartnerBottomBar = remember(currentBackStackEntry) {
        when (currentRoute) {
            Route.PartnerDashboard::class.qualifiedName,
            Route.PendingReservations::class.qualifiedName,
            Route.PartnerAnalytics::class.qualifiedName -> true
            else -> false
        }
    }

    Scaffold(
        containerColor = com.example.sportsbook.ui.theme.Navy900,
        bottomBar = {
            when {
                showPlayerBottomBar -> BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Route.PlayerHome) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
                showPartnerBottomBar -> PartnerBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Route.PartnerDashboard) { inclusive = false }
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
                    onNavigateToPlayerOnboarding = {
                        navController.navigate(Route.PlayerOnboarding) {
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
                        // Route through Splash so it checks role/partner status
                        // and skips role selection for existing partners/players
                        navController.navigate(Route.Splash) {
                            popUpTo(Route.Login) { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.Register> {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Route.Splash) {
                            popUpTo(Route.Login) { inclusive = true }
                        }
                    }
                )
            }

            // Onboarding
            composable<Route.RoleSelection> {
                RoleSelectionScreen(
                    onPlayerSelected = {
                        navController.navigate(Route.PlayerOnboarding) {
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

            composable<Route.PlayerOnboarding> {
                PlayerOnboardingScreen(
                    onComplete = {
                        navController.navigate(Route.PlayerHome) {
                            popUpTo(Route.PlayerOnboarding) { inclusive = true }
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
                    },
                    onNavigateToProfile = {
                        navController.navigate(Route.PlayerProfile) {
                            popUpTo(Route.PlayerHome) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Route.Notifications)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Route.Settings)
                    },
                    onSignOut = {
                        navController.navigate(Route.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onFindMatch = {
                        navController.navigate(Route.MatchList)
                    },
                    onNavigateToSearch = {
                        navController.navigate(Route.Search)
                    },
                    onNavigateToFavorites = {
                        navController.navigate(Route.Favorites)
                    },
                    onNavigateToFriends = {
                        navController.navigate(Route.FriendsList)
                    },
                    onBrowseAllSports = {
                        navController.navigate(Route.AllSports)
                    }
                )
            }

            composable<Route.NewsFeed> {
                NewsFeedScreen(
                    onUserClick = { userId ->
                        navController.navigate(Route.PlayerPublicProfile(userId))
                    }
                )
            }

            composable<Route.AllSports> {
                AllSportsScreen(
                    onBack = { navController.popBackStack() },
                    onSportClick = { sportType ->
                        navController.navigate(Route.SportDetail(sportType))
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
                    onBack = { navController.popBackStack() },
                    onShowMap = { navController.navigate(Route.VenueMap) }
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
                    onProceedToPayment = { bookingId, _ ->
                        navController.navigate(Route.MyBookings) {
                            popUpTo(Route.PlayerHome) { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.PaymentCheckout> {
                PaymentCheckoutScreen(
                    onPaymentSuccess = {
                        navController.navigate(Route.MyBookings) {
                            popUpTo(Route.PlayerHome) { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.PaymentHistory> {
                PaymentHistoryScreen(
                    onPaymentClick = { paymentId ->
                        navController.navigate(Route.PaymentDetail(paymentId))
                    }
                )
            }

            composable<Route.PaymentDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.PaymentDetail>()
                PaymentDetailScreen(
                    paymentId = route.paymentId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.MyBookings> {
                MyBookingsScreen(
                    onBookingClick = { bookingId ->
                        navController.navigate(Route.BookingDetail(bookingId))
                    },
                    onPayNow = { bookingId ->
                        navController.navigate(Route.PaymentCheckout(bookingId))
                    }
                )
            }

            composable<Route.PlayerProfile> {
                PlayerProfileScreen(
                    onSignOut = {
                        navController.navigate(Route.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        navController.navigate(Route.PartnerDashboard) {
                            launchSingleTop = true
                        }
                    },
                    onManageTimeSlots = {
                        navController.navigate(Route.TimeSlotManagement)
                    },
                    onNavigateToXpLevel = {
                        navController.navigate(Route.PlayerXpLevel)
                    },
                    onNavigateToAchievements = {
                        navController.navigate(Route.PlayerAchievements)
                    },
                    onNavigateToStats = {
                        navController.navigate(Route.PlayerStatsScreen)
                    },
                    onNavigateToPayments = {
                        navController.navigate(Route.PaymentHistory)
                    },
                    onNavigateToFriends = {
                        navController.navigate(Route.FriendsList)
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
                    onOpenChat = { bookingId ->
                        navController.navigate(Route.BookingChat(bookingId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.BookingChat> {
                BookingChatScreen(
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

            composable<Route.Notifications> {
                NotificationsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.Settings> {
                SettingsScreen(
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
                            launchSingleTop = true
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
                            launchSingleTop = true
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.PartnerDashboard> {
                val context = LocalContext.current
                PartnerDashboardScreen(
                    onSignOut = {
                        navController.navigate(Route.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onManageImages = { entityType, entityId ->
                        navController.navigate(Route.ManageImages(entityType, entityId))
                    },
                    onOpenWebDashboard = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://10.0.2.2:5173"))
                        context.startActivity(intent)
                    },
                    onBrowseAsPlayer = {
                        navController.navigate(Route.PlayerHome) {
                            launchSingleTop = true
                        }
                    },
                    onPaymentSetup = {
                        navController.navigate(Route.StripeConnect)
                    },
                    onManageTimeSlots = {
                        navController.navigate(Route.TimeSlotManagement)
                    },
                    onEditVenue = { venueId ->
                        navController.navigate(Route.EditVenue(venueId))
                    },
                    onEditCoach = { coachId ->
                        navController.navigate(Route.EditCoach(coachId))
                    },
                    onAddVenue = {
                        navController.navigate(Route.VenueSetup)
                    },
                    onAddCoachProfile = {
                        navController.navigate(Route.CoachSetup)
                    },
                    onViewPendingReservations = {
                        navController.navigate(Route.PendingReservations)
                    },
                    onViewAnalytics = {
                        navController.navigate(Route.PartnerAnalytics)
                    }
                )
            }

            composable<Route.PendingReservations> {
                PendingReservationsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.TimeSlotManagement> {
                TimeSlotManagementScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.ManageImages> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.ManageImages>()
                ManageImagesScreen(
                    entityType = route.entityType,
                    entityId = route.entityId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.StripeConnect> {
                StripeConnectScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<Route.EditVenue> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.EditVenue>()
                EditVenueScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable<Route.EditCoach> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.EditCoach>()
                EditCoachScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            // Matchmaking flow
            composable<Route.MatchList> {
                MatchListScreen(
                    onMatchClick = { matchId ->
                        navController.navigate(Route.MatchDetail(matchId))
                    },
                    onCreateMatch = {
                        navController.navigate(Route.CreateMatch)
                    },
                    onCreateParty = {
                        navController.navigate(Route.CreateParty)
                    },
                    onShowMap = {
                        navController.navigate(Route.MatchMap)
                    },
                    onBrowseAvailablePlayers = {
                        navController.navigate(Route.AvailablePlayers())
                    }
                )
            }

            composable<Route.MatchDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.MatchDetail>()
                MatchDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOpenChat = { matchId ->
                        navController.navigate(Route.MatchChat(matchId))
                    },
                    onRatePlayers = { matchId ->
                        navController.navigate(Route.RatePlayers(matchId))
                    },
                    onJoinWithParty = { matchId ->
                        navController.navigate(Route.MatchDetail(matchId))
                    },
                    onBrowseAvailablePlayers = { matchId, sportType, minSkill, maxSkill ->
                        navController.navigate(Route.AvailablePlayers(
                            matchId = matchId,
                            sportType = sportType,
                            minSkillLevel = minSkill,
                            maxSkillLevel = maxSkill
                        ))
                    }
                )
            }

            composable<Route.CreateMatch> {
                CreateMatchScreen(
                    onMatchCreated = { matchId ->
                        navController.navigate(Route.MatchDetail(matchId)) {
                            popUpTo(Route.CreateMatch) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.MatchChat> {
                MatchChatScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.RatePlayers> {
                RatePlayersScreen(
                    onBack = { navController.popBackStack() },
                    onSubmitted = { navController.popBackStack() }
                )
            }

            composable<Route.AvailablePlayers> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.AvailablePlayers>()
                AvailablePlayersScreen(
                    matchId = route.matchId,
                    matchSportType = route.sportType,
                    matchMinSkill = route.minSkillLevel,
                    matchMaxSkill = route.maxSkillLevel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Maps
            composable<Route.VenueMap> {
                VenueMapScreen(
                    onVenueClick = { venueId ->
                        navController.navigate(Route.VenueDetail(venueId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.MatchMap> {
                MatchMapScreen(
                    onMatchClick = { matchId ->
                        navController.navigate(Route.MatchDetail(matchId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Public Profile
            composable<Route.PlayerPublicProfile> {
                PublicPlayerProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Search
            composable<Route.Search> {
                SearchScreen(
                    onVenueClick = { venueId ->
                        navController.navigate(Route.VenueDetail(venueId))
                    },
                    onCoachClick = { coachId ->
                        navController.navigate(Route.CoachDetail(coachId))
                    },
                    onMatchClick = { matchId ->
                        navController.navigate(Route.MatchDetail(matchId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Favorites
            composable<Route.Favorites> {
                FavoritesScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Party
            composable<Route.CreateParty> {
                PartyCreateScreen(
                    onPartyCreated = { partyId ->
                        navController.navigate(Route.PartyDetail(partyId)) {
                            popUpTo(Route.CreateParty) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.PartyDetail> {
                PartyDetailScreen(
                    onInviteFriends = { partyId ->
                        navController.navigate(Route.PartyInviteMembers(partyId))
                    },
                    onFindMatch = {
                        navController.navigate(Route.MatchList)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.PartyInviteMembers> {
                PartyInviteMembersScreen(
                    onInvitesSent = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            // Friends
            composable<Route.FriendsList> {
                FriendsListScreen(
                    onBack = { navController.popBackStack() },
                    onAddFriend = { navController.navigate(Route.AddFriend) },
                    onViewRequests = { navController.navigate(Route.FriendRequests) }
                )
            }

            composable<Route.FriendRequests> {
                FriendRequestsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.AddFriend> {
                AddFriendScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Gamification
            composable<Route.PlayerXpLevel> {
                PlayerXpLevelScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.PlayerAchievements> {
                PlayerAchievementsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.PlayerStatsScreen> {
                PlayerStatsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Route.PartnerAnalytics> {
                PartnerAnalyticsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
