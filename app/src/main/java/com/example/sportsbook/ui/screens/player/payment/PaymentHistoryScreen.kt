package com.example.sportsbook.ui.screens.player.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.sportsbook.domain.enums.PaymentStatus
import com.example.sportsbook.domain.model.Payment
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.OrangeAccent

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    onBack: () -> Unit = {},
    onPaymentClick: (Long) -> Unit = {},
    viewModel: PaymentHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf("All", "Completed", "Pending", "Refunded")

    // Summary totals derived from payments
    val completedPayments = uiState.payments.filter { it.status == PaymentStatus.COMPLETED }
    val pendingPayments = uiState.payments.filter { it.status == PaymentStatus.PENDING }
    val refundedPayments = uiState.payments.filter { it.status == PaymentStatus.REFUNDED }
    val thisMonthTotal = completedPayments.sumOf { it.amount }.toInt()
    val totalSpent = uiState.payments.filter {
        it.status == PaymentStatus.COMPLETED || it.status == PaymentStatus.PENDING
    }.sumOf { it.amount }.toInt()
    val refundedTotal = refundedPayments.sumOf { it.amount }.toInt()

    // Filtered list
    val displayedPayments = when (selectedFilter) {
        1 -> completedPayments
        2 -> pendingPayments
        3 -> refundedPayments
        else -> uiState.payments
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Payment History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // ── Summary cards ─────────────────────────────────────
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                SummaryCard(
                                    value = if (uiState.payments.isEmpty()) "0" else "$thisMonthTotal MKD",
                                    label = "This month",
                                    valueColor = GreenAccent,
                                    modifier = Modifier.weight(1f),
                                )
                                SummaryCard(
                                    value = if (uiState.payments.isEmpty()) "0" else "$totalSpent MKD",
                                    label = "Total spent",
                                    valueColor = DarkTextPrimary,
                                    modifier = Modifier.weight(1f),
                                )
                                SummaryCard(
                                    value = if (uiState.payments.isEmpty()) "0" else "$refundedTotal MKD",
                                    label = "Refunded",
                                    valueColor = OrangeAccent,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        // ── Filter chips ──────────────────────────────────────
                        item {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                filters.forEachIndexed { index, label ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (index == selectedFilter) GreenAccent else DarkSurface)
                                            .clickable { selectedFilter = index }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (index == selectedFilter) Color.White else DarkTextSecondary,
                                        )
                                    }
                                }
                            }
                        }

                        if (displayedPayments.isEmpty()) {
                            // ── Empty state ───────────────────────────────────
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("💸", fontSize = 48.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("No payments yet", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                                        Text("Your payment history will appear here", fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }
                            }
                        } else {
                            // ── Grouped by month ──────────────────────────────
                            val grouped = groupPaymentsByMonth(displayedPayments)
                            grouped.forEach { (monthLabel, monthPayments) ->
                                item(key = "header_$monthLabel") {
                                    Text(
                                        text = monthLabel.uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkTextSecondary,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    )
                                }
                                items(monthPayments, key = { it.id }) { payment ->
                                    PaymentRow(payment = payment, onClick = { onPaymentClick(payment.id) })
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            color = DarkTextSecondary,
            modifier = Modifier.padding(top = 4.dp),
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
private fun PaymentRow(
    payment: Payment,
    onClick: () -> Unit,
) {
    val sportEmoji = when {
        payment.venueName?.contains("tennis", ignoreCase = true) == true -> "🎾"
        payment.venueName?.contains("basket", ignoreCase = true) == true -> "🏀"
        payment.venueName?.contains("football", ignoreCase = true) == true -> "⚽"
        payment.venueName?.contains("swim", ignoreCase = true) == true -> "🏊"
        payment.venueName?.contains("volleyball", ignoreCase = true) == true -> "🏐"
        payment.coachName != null -> "🏋️"
        else -> "💳"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Sport icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF252525)),
            contentAlignment = Alignment.Center,
        ) {
            Text(sportEmoji, fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = payment.venueName ?: payment.coachName ?: "Booking #${payment.bookingId}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary,
            )
            val dateText = buildString {
                payment.slotDate?.let { append(it.toDisplayDate()) }
                if (payment.startTime != null && payment.endTime != null) {
                    append(" • ${payment.startTime} - ${payment.endTime}")
                }
            }
            if (dateText.isNotBlank()) {
                Text(dateText, fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Amount + status
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${"%.0f".format(payment.amount)} MKD",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary,
            )
            val (statusText, statusColor) = when (payment.status) {
                PaymentStatus.COMPLETED -> "Paid ✓" to GreenAccent
                PaymentStatus.PENDING -> "Pending" to OrangeAccent
                PaymentStatus.FAILED -> "Failed" to Color(0xFFEF5350)
                PaymentStatus.REFUNDED -> "Refunded" to OrangeAccent
            }
            Text(statusText, fontSize = 11.sp, color = statusColor, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

private fun groupPaymentsByMonth(payments: List<Payment>): Map<String, List<Payment>> {
    return payments
        .groupBy { payment ->
            payment.slotDate?.let { date ->
                try {
                    val parts = date.split("-")
                    if (parts.size >= 2) {
                        val year = parts[0]
                        val month = when (parts[1]) {
                            "01" -> "January"; "02" -> "February"; "03" -> "March"
                            "04" -> "April"; "05" -> "May"; "06" -> "June"
                            "07" -> "July"; "08" -> "August"; "09" -> "September"
                            "10" -> "October"; "11" -> "November"; "12" -> "December"
                            else -> parts[1]
                        }
                        "$month $year"
                    } else "Recent"
                } catch (e: Exception) { "Recent" }
            } ?: "Recent"
        }
        .toSortedMap(compareByDescending { it })
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PaymentHistoryScreenPreview() {
    val samplePayments = listOf(
        Payment(id = 1L, bookingId = 10L, amount = 1500.00, currency = "MKD", status = PaymentStatus.COMPLETED, venueName = "City Tennis Center", slotDate = "2026-06-05", startTime = "10:00", endTime = "11:00"),
        Payment(id = 2L, bookingId = 11L, amount = 2400.00, currency = "MKD", status = PaymentStatus.COMPLETED, venueName = "City Football Arena", slotDate = "2026-06-03", startTime = "14:00", endTime = "16:00"),
        Payment(id = 3L, bookingId = 12L, amount = 800.00, currency = "MKD", status = PaymentStatus.PENDING, venueName = "Downtown Basketball", slotDate = "2026-05-28", startTime = "16:00", endTime = "17:00"),
        Payment(id = 4L, bookingId = 13L, amount = 500.00, currency = "MKD", status = PaymentStatus.REFUNDED, venueName = "City Tennis Center", slotDate = "2026-05-25", startTime = "10:00", endTime = "11:00"),
    )
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Payment History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryCard("4,200 MKD", "This month", GreenAccent, Modifier.weight(1f))
                SummaryCard("9,800 MKD", "Total spent", DarkTextPrimary, Modifier.weight(1f))
                SummaryCard("500 MKD", "Refunded", OrangeAccent, Modifier.weight(1f))
            }
            samplePayments.forEach { payment ->
                PaymentRow(payment = payment, onClick = {})
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
}
