package com.example.sportsbook.ui.screens.player.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.theme.USOpenGold
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCheckoutScreen(
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: PaymentCheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val paymentSheet = rememberPaymentSheet { result ->
        viewModel.onPaymentSheetResult(result)
    }

    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            onPaymentSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }

    // When clientSecret is ready, launch PaymentSheet
    LaunchedEffect(uiState.clientSecret) {
        uiState.clientSecret?.let { secret ->
            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret = secret,
                configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "SportsBook",
                )
            )
        }
    }

    val finalAmount = (uiState.amount - uiState.xpDiscount).coerceAtLeast(0.0)
    val isFreeWithXp = uiState.amount > 0 && finalAmount == 0.0 && uiState.xpRedeemed

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Booking Summary Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Booking Summary",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    uiState.bookingId?.let { id ->
                        SummaryRow("Booking ID", "#$id")
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (uiState.amount > 0) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        if (uiState.xpRedeemed && uiState.xpDiscount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Original Price:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${"%.0f".format(uiState.amount)} ден",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "XP Discount (${uiState.xpToRedeem} XP):",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "-${"%.0f".format(uiState.xpDiscount)} ден",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text(
                            text = if (isFreeWithXp) "Total: Free with XP!" else "Total: ${"%.0f".format(finalAmount)} ден",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isFreeWithXp) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // XP Discount Section — only shown when amount is loaded
            if (uiState.amount > 0 && uiState.availableXp > 0) {
                XpDiscountSection(
                    availableXp = uiState.availableXp,
                    totalPrice = uiState.amount,
                    xpToRedeem = uiState.xpToRedeem,
                    xpDiscount = uiState.xpDiscount,
                    xpRedeemed = uiState.xpRedeemed,
                    isRedeemingXp = uiState.isRedeemingXp,
                    onXpSliderChange = viewModel::onXpSliderChange,
                    onApplyXpDiscount = viewModel::redeemXp,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pay Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isCreatingIntent || uiState.isProcessingPayment -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.isCreatingIntent) "Preparing payment..." else "Processing...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    isFreeWithXp -> {
                        Button(
                            onClick = { viewModel.initiatePayment() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Complete Booking (Free with XP!)")
                        }
                    }
                    uiState.clientSecret == null -> {
                        Button(
                            onClick = { viewModel.initiatePayment() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val displayAmount = if (uiState.amount > 0) finalAmount else uiState.amount
                            val priceText = if (displayAmount > 0) " ${"%.0f".format(displayAmount)} ден" else ""
                            Text("Pay$priceText")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun XpDiscountSection(
    availableXp: Int,
    totalPrice: Double,
    xpToRedeem: Int,
    xpDiscount: Double,
    xpRedeemed: Boolean,
    isRedeemingXp: Boolean,
    onXpSliderChange: (Int) -> Unit,
    onApplyXpDiscount: () -> Unit,
) {
    // Maximum XP redeemable is capped so it cannot exceed the total price
    val maxRedeemable = minOf(availableXp, (totalPrice * 100).roundToInt())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "XP Discount",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "You have $availableXp XP  •  100 XP = 1 MKD",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = xpRedeemed,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.padding(start = 6.dp))
                    Text(
                        text = "XP Discount Applied: -${"%.0f".format(xpDiscount)} ден",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            if (!xpRedeemed) {
                if (maxRedeemable > 0) {
                    Slider(
                        value = xpToRedeem.toFloat(),
                        onValueChange = { onXpSliderChange(it.roundToInt()) },
                        valueRange = 0f..maxRedeemable.toFloat(),
                        steps = (maxRedeemable / 10).coerceAtLeast(0),
                        enabled = !xpRedeemed,
                        colors = SliderDefaults.colors(
                            thumbColor = USOpenGold,
                            activeTrackColor = USOpenGold,
                            inactiveTrackColor = USOpenGold.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = if (xpToRedeem > 0) {
                            "Redeem $xpToRedeem XP for ${"%.0f".format(xpToRedeem / 100.0)} ден discount"
                        } else {
                            "Move the slider to select XP to redeem"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onApplyXpDiscount,
                        enabled = xpToRedeem > 0 && !isRedeemingXp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (isRedeemingXp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Apply XP Discount")
                        }
                    }
                } else {
                    Text(
                        text = "Not enough XP to apply a discount on this booking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun PaymentCheckoutScreenPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Payment") },
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
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Booking Summary",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SummaryRow("Date", "Mar 25, 2026")
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow("Time", "10:00 - 11:00")
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow("Price", "1500 ден")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Total: 1500 ден",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                XpDiscountSection(
                    availableXp = 450,
                    totalPrice = 25.0,
                    xpToRedeem = 200,
                    xpDiscount = 2.0,
                    xpRedeemed = false,
                    isRedeemingXp = false,
                    onXpSliderChange = {},
                    onApplyXpDiscount = {},
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pay 1380 ден")
                }
            }
        }
    }
}
