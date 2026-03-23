package com.example.sportsbook.ui.screens.partner.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.VenueRepository
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartnerDashboardViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val venueRepository: VenueRepository,
    private val coachRepository: CoachRepository
) : ViewModel() {

    data class State(
        val totalBookings: Int = 0,
        val pendingBookings: Int = 0,
        val venues: List<Venue> = emptyList(),
        val coachProfile: Coach? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(State())
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load bookings
            bookingRepository.getPartnerBookings()
                .onSuccess { bookings ->
                    val total = bookings.size
                    val pending = bookings.count { it.status == BookingStatus.PENDING }
                    _uiState.update {
                        it.copy(totalBookings = total, pendingBookings = pending)
                    }
                }

            // Load partner's venues
            venueRepository.getMyVenues()
                .onSuccess { venues ->
                    _uiState.update { it.copy(venues = venues) }
                }

            // Load partner's coach profile
            coachRepository.getMyCoachProfile()
                .onSuccess { coach ->
                    _uiState.update { it.copy(coachProfile = coach) }
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerDashboardScreen(
    onSignOut: () -> Unit,
    onManageImages: (entityType: String, entityId: Long) -> Unit = { _, _ -> },
    onOpenWebDashboard: () -> Unit = {},
    onBrowseAsPlayer: () -> Unit = {},
    onPaymentSetup: () -> Unit = {},
    onManageTimeSlots: () -> Unit = {},
    viewModel: PartnerDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partner Dashboard") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    LoadingIndicator(modifier = Modifier.fillMaxWidth())
                }
                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error!!,
                        onRetry = viewModel::loadDashboard,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Total Bookings",
                            count = uiState.totalBookings,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Pending",
                            count = uiState.pendingBookings,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Venue image management
                    uiState.venues.forEach { venue ->
                        ListingCard(
                            title = venue.name,
                            subtitle = "Venue",
                            imageCount = venue.images.size,
                            onManageImages = { onManageImages("venue", venue.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Coach image management
                    uiState.coachProfile?.let { coach ->
                        ListingCard(
                            title = coach.name,
                            subtitle = "Coach",
                            imageCount = coach.images.size,
                            onManageImages = { onManageImages("coach", coach.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (uiState.venues.isEmpty() && uiState.coachProfile == null) {
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "No listings found. Create a venue or coach profile to get started.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Manage Time Slots
            Button(
                onClick = onManageTimeSlots,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage Time Slots")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Payment setup
            Button(
                onClick = onPaymentSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Payment Setup (Stripe Connect)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Open web dashboard
            FilledTonalButton(
                onClick = onOpenWebDashboard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage from Partner Dashboard")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Browse as player
            FilledTonalButton(
                onClick = onBrowseAsPlayer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.SportsTennis,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browse & Book as Player")
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Sign Out")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ListingCard(
    title: String,
    subtitle: String,
    imageCount: Int,
    onManageImages: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$subtitle · $imageCount image${if (imageCount != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onManageImages,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage Images")
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PartnerDashboardScreenPreview() {
    MaterialTheme {
        PartnerDashboardScreen(onSignOut = {})
    }
}
