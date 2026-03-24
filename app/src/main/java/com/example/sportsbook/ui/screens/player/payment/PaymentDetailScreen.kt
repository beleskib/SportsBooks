package com.example.sportsbook.ui.screens.player.payment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.PaymentStatus
import com.example.sportsbook.domain.model.Payment
import com.example.sportsbook.ui.common.toDisplayDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailScreen(
    paymentId: Long,
    onBack: () -> Unit,
    viewModel: PaymentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = viewModel::loadPayment) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.payment != null -> {
                val payment = uiState.payment!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount + Status header card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${"%.0f".format(payment.amount)} ден",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = payment.currency.uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            PaymentDetailStatusBadge(status = payment.status)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Booking details card
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Booking Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Venue or Coach
                            val serviceName = payment.venueName ?: payment.coachName
                            if (serviceName != null) {
                                val icon = if (payment.venueName != null) {
                                    Icons.Default.LocationOn
                                } else {
                                    Icons.Default.Person
                                }
                                val label = if (payment.venueName != null) "Venue" else "Coach"
                                DetailRowWithIcon(
                                    icon = icon,
                                    label = label,
                                    value = serviceName
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Date
                            payment.slotDate?.let { date ->
                                DetailRowWithIcon(
                                    icon = Icons.Default.CalendarToday,
                                    label = "Date",
                                    value = date.toDisplayDate()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Time
                            if (payment.startTime != null && payment.endTime != null) {
                                DetailRowWithIcon(
                                    icon = Icons.Default.Schedule,
                                    label = "Time",
                                    value = "${payment.startTime} - ${payment.endTime}"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Booking ID
                            DetailRowWithIcon(
                                icon = Icons.Default.CreditCard,
                                label = "Booking ID",
                                value = "#${payment.bookingId}"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payment info card
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Payment Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            PaymentInfoRow(label = "Payment ID", value = "#${payment.id}")

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            payment.paymentMethod?.let { method ->
                                PaymentInfoRow(label = "Method", value = method)
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }

                            payment.externalPaymentId?.let { externalId ->
                                PaymentInfoRow(
                                    label = "Stripe ID",
                                    value = externalId.take(24) + if (externalId.length > 24) "…" else ""
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }

                            payment.paidAt?.let { paidAt ->
                                PaymentInfoRow(label = "Paid At", value = paidAt)
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }

                            payment.createdAt?.let { createdAt ->
                                PaymentInfoRow(label = "Created", value = createdAt)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
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
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
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
        createdAt = "2026-03-25T09:00:00Z"
    )
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Payment Details") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${"%.0f".format(samplePayment.amount)} ден",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = samplePayment.currency.uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PaymentDetailStatusBadge(status = samplePayment.status)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Booking Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRowWithIcon(
                            icon = Icons.Default.LocationOn,
                            label = "Venue",
                            value = samplePayment.venueName ?: ""
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRowWithIcon(
                            icon = Icons.Default.CalendarToday,
                            label = "Date",
                            value = samplePayment.slotDate?.let { it } ?: ""
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRowWithIcon(
                            icon = Icons.Default.Schedule,
                            label = "Time",
                            value = "${samplePayment.startTime} - ${samplePayment.endTime}"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRowWithIcon(
                            icon = Icons.Default.CreditCard,
                            label = "Booking ID",
                            value = "#${samplePayment.bookingId}"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
