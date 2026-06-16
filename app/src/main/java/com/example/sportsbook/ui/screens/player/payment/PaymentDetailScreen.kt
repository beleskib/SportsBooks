package com.example.sportsbook.ui.screens.player.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun PaymentDetailScreen(
    paymentId: Long,
    onBack: () -> Unit,
    viewModel: PaymentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ────────────────────────────────────────────────────────
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
            Text("Payment Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }

            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            fontSize = 14.sp,
                            color = Color(0xFFEF5350),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .clickable(onClick = viewModel::loadPayment)
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Text("Retry", fontSize = 14.sp, color = GreenAccent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            uiState.payment != null -> {
                val payment = uiState.payment!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // ── Amount + Status header card ───────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(GreenAccent.copy(alpha = 0.08f))
                            .border(1.dp, GreenAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${"%.0f".format(payment.amount)} ден",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = payment.currency.uppercase(),
                            fontSize = 13.sp,
                            color = DarkTextSecondary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PaymentDetailStatusBadge(status = payment.status)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Booking details card ──────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                            .padding(16.dp),
                    ) {
                        Text("Booking Details", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))

                        val serviceName = payment.venueName ?: payment.coachName
                        if (serviceName != null) {
                            val icon = if (payment.venueName != null) Icons.Default.LocationOn else Icons.Default.Person
                            val label = if (payment.venueName != null) "Venue" else "Coach"
                            DetailRowWithIcon(icon = icon, label = label, value = serviceName)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        payment.slotDate?.let { date ->
                            DetailRowWithIcon(icon = Icons.Default.CalendarToday, label = "Date", value = date.toDisplayDate())
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (payment.startTime != null && payment.endTime != null) {
                            DetailRowWithIcon(icon = Icons.Default.Schedule, label = "Time", value = "${payment.startTime} - ${payment.endTime}")
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        DetailRowWithIcon(icon = Icons.Default.CreditCard, label = "Booking ID", value = "#${payment.bookingId}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Payment info card ─────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                            .padding(16.dp),
                    ) {
                        Text("Payment Information", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))

                        PaymentInfoRow(label = "Payment ID", value = "#${payment.id}")
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder).padding(vertical = 8.dp))

                        payment.paymentMethod?.let { method ->
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentInfoRow(label = "Method", value = method)
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder).padding(vertical = 8.dp))
                        }

                        payment.externalPaymentId?.let { externalId ->
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentInfoRow(
                                label = "Transaction ID",
                                value = externalId.take(24) + if (externalId.length > 24) "…" else "",
                            )
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder).padding(vertical = 8.dp))
                        }

                        payment.paidAt?.let { paidAt ->
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentInfoRow(label = "Paid At", value = paidAt)
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder).padding(vertical = 8.dp))
                        }

                        payment.createdAt?.let { createdAt ->
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentInfoRow(label = "Created", value = createdAt)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRowWithIcon(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DarkTextSecondary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = DarkTextSecondary)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkTextPrimary)
        }
    }
}

@Composable
private fun PaymentInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.width(100.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkTextPrimary)
    }
}

@Composable
private fun PaymentDetailStatusBadge(status: PaymentStatus) {
    val (text, color) = when (status) {
        PaymentStatus.PENDING -> "Pending" to Color(0xFFFFA000)
        PaymentStatus.COMPLETED -> "Completed" to Color(0xFF4CAF50)
        PaymentStatus.FAILED -> "Failed" to Color(0xFFF44336)
        PaymentStatus.REFUNDED -> "Refunded" to Color(0xFF2196F3)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PaymentDetailScreenPreview() {
    val samplePayment = Payment(
        id = 1L,
        bookingId = 42L,
        payerId = 10L,
        amount = 1500.00,
        currency = "MKD",
        status = PaymentStatus.COMPLETED,
        paymentMethod = "card",
        externalPaymentId = "pi_3NkAbcDefGhIjKl",
        paidAt = "2026-03-25T10:15:00Z",
        venueName = "City Tennis Center",
        slotDate = "2026-03-25",
        startTime = "10:00",
        endTime = "11:00",
        createdAt = "2026-03-25T09:00:00Z",
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
            Text("Payment Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(GreenAccent.copy(alpha = 0.08f))
                    .border(1.dp, GreenAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("${"%.0f".format(samplePayment.amount)} ден", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(samplePayment.currency.uppercase(), fontSize = 13.sp, color = DarkTextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                PaymentDetailStatusBadge(status = samplePayment.status)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface).border(1.dp, DarkBorder, RoundedCornerShape(14.dp)).padding(16.dp),
            ) {
                Text("Booking Details", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                DetailRowWithIcon(icon = Icons.Default.LocationOn, label = "Venue", value = samplePayment.venueName ?: "")
                Spacer(modifier = Modifier.height(12.dp))
                DetailRowWithIcon(icon = Icons.Default.CalendarToday, label = "Date", value = samplePayment.slotDate ?: "")
                Spacer(modifier = Modifier.height(12.dp))
                DetailRowWithIcon(icon = Icons.Default.Schedule, label = "Time", value = "${samplePayment.startTime} - ${samplePayment.endTime}")
                Spacer(modifier = Modifier.height(12.dp))
                DetailRowWithIcon(icon = Icons.Default.CreditCard, label = "Booking ID", value = "#${samplePayment.bookingId}")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
