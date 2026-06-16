package com.example.sportsbook.ui.screens.player.booking

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkNavBar
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun BookingConfirmationScreen(
    timeSlotId: Long,
    onProceedToPayment: (Long, String) -> Unit,
    onBack: () -> Unit,
    onSplitWithFriends: ((timeSlotId: Long, venueId: Long) -> Unit)? = null,
    viewModel: BookingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(timeSlotId) {
        if (uiState.selectedSlot == null) {
            viewModel.loadSlotById(timeSlotId)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.bookingSuccess) {
        if (uiState.bookingSuccess) {
            snackbarHostState.showSnackbar("Booking request sent! The venue partner will review your request.")
            uiState.createdBookingId?.let { bookingId ->
                onProceedToPayment(bookingId, "")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp) // space for bottom bar
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Header ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkTextPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Confirm Booking", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Booking card ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface),
            ) {
                // Hero gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1B5E20), GreenAccent, Color(0xFF81C784))
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    Text(
                        text = uiState.selectedSlot?.let { "Venue Booking" } ?: "Arena Sport Center",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                // Detail rows
                val slot = uiState.selectedSlot
                BookingDetailRow("📅", "Date", slot?.slotDate?.toDisplayDate() ?: "—")
                BookingDetailRow("🕐", "Time", slot?.displayTime ?: "—")
                BookingDetailRow("🏀", "Court", "Court A - Indoor (Basketball)")
                BookingDetailRow("📍", "Location", "Bul. Partizanski Odredi 17, Skopje")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Split with friends ───────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Split with Friends", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                    Text("Optional", fontSize = 13.sp, color = GreenAccent)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // "You" avatar
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(GreenAccent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("B", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    // Add slots
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkBorder)
                                .border(2.dp, Color(0xFF555555), CircleShape)
                                .clickable {
                                    val venueId = uiState.selectedSlot?.venueId
                                    if (onSplitWithFriends != null && venueId != null) {
                                        onSplitWithFriends(timeSlotId, venueId)
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("+", fontSize = 18.sp, color = DarkTextSecondary)
                        }
                    }
                }
                Text("Add friends to split the cost equally", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Promo code ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🎟️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Add promo code", fontSize = 14.sp, color = DarkTextSecondary, modifier = Modifier.weight(1f))
                Text("Apply", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GreenAccent)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Price breakdown ──────────────────────────────────────────
            Text("Price Breakdown", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(12.dp))

            val basePrice = uiState.selectedSlot?.priceOverride?.toInt() ?: 1000
            val discount = (basePrice * 0.25).toInt()
            val serviceFee = 50
            val total = basePrice - discount + serviceFee

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(16.dp),
            ) {
                PriceRow("Court A - 1 hour", "$basePrice MKD", Color.Transparent)
                PriceRow("25% Weekend Discount", "-$discount MKD", GreenAccent)
                PriceRow("Service fee", "$serviceFee MKD", Color.Transparent)
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                    Text("$total MKD", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Payment method ───────────────────────────────────────────
            Text("Payment Method", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1A237E)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("VISA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Credit Card", fontSize = 12.sp, color = DarkTextSecondary)
                    Text("**** **** **** 4532", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                }
                Text("Change", fontSize = 13.sp, color = GreenAccent, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Cancellation policy ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1B3A1E))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("✅", fontSize = 16.sp)
                Text(
                    text = "Free cancellation up to 2 hours before start time. After that, 50% of the booking fee will be charged.",
                    fontSize = 12.sp,
                    color = Color(0xFF81C784),
                    lineHeight = 18.sp,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom bar
        ConfirmBottomBar(
            slot = uiState.selectedSlot,
            isLoading = uiState.isBookingLoading,
            onConfirm = { viewModel.confirmBooking() },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp),
        )
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun BookingDetailRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkBorder),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp),
        ) {
            Text(label.uppercase(), fontSize = 11.sp, color = DarkTextSecondary, letterSpacing = 0.5.sp)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary, modifier = Modifier.padding(top = 1.dp))
        }
    }
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(DarkBorder))
}

@Composable
private fun PriceRow(label: String, value: String, textColor: Color) {
    val labelColor = if (textColor != Color.Transparent) textColor else DarkTextPrimary
    val valueColor = if (textColor != Color.Transparent) textColor else DarkTextPrimary
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 14.sp, color = labelColor)
        Text(value, fontSize = 14.sp, color = valueColor)
    }
}

@Composable
private fun ConfirmBottomBar(
    slot: TimeSlot?,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val basePrice = slot?.priceOverride?.toInt() ?: 1000
    val total = (basePrice * 0.75).toInt() + 50

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkNavBar)
            .border(width = 1.dp, color = DarkBorder, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GreenAccent)
                .clickable(enabled = !isLoading, onClick = onConfirm)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Confirm & Pay $total MKD", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Text(
            "By confirming, you agree to the booking terms",
            fontSize = 11.sp,
            color = DarkTextSecondary,
            modifier = Modifier.padding(top = 6.dp).align(Alignment.CenterHorizontally),
        )
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun BookingConfirmationScreenPreview() {
    val sampleSlot = TimeSlot(id = 1L, venueId = 1L, slotDate = "2026-05-25", startTime = "15:00", endTime = "16:00", priceOverride = 800.0)
    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(bottom = 100.dp).verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Confirm Booking", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(16.dp)).background(DarkSurface),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                            .background(Brush.linearGradient(listOf(Color(0xFF1B5E20), GreenAccent, Color(0xFF81C784))))
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart,
                    ) {
                        Text("Arena Sport Center", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    BookingDetailRow("📅", "Date", "Sunday, May 25, 2026")
                    BookingDetailRow("🕐", "Time", "15:00 - 16:00 (1 hour)")
                    BookingDetailRow("🏀", "Court", "Court A - Indoor (Basketball)")
                    BookingDetailRow("📍", "Location", "Bul. Partizanski Odredi 17, Skopje")
                }
            }
            ConfirmBottomBar(slot = sampleSlot, isLoading = false, onConfirm = {}, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
