package com.example.sportsbook.ui.screens.player.payment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.common.toDisplayDate
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.rememberPaymentSheet

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

                    uiState.timeSlot?.let { slot ->
                        SummaryRow("Date", slot.slotDate.toDisplayDate())
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow("Time", slot.displayTime)
                        Spacer(modifier = Modifier.height(8.dp))
                        slot.priceOverride?.let { price ->
                            SummaryRow("Price", "$${"%.2f".format(price)}")
                        }
                    }

                    if (uiState.amount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Total: $${"%.2f".format(uiState.amount)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                    uiState.clientSecret == null -> {
                        Button(
                            onClick = { viewModel.initiatePayment() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val priceText = uiState.timeSlot?.priceOverride?.let {
                                " $${"%.2f".format(it)}"
                            } ?: ""
                            Text("Pay$priceText")
                        }
                    }
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

@Preview(showBackground = true)
@Composable
private fun PaymentCheckoutScreenPreview() {
    MaterialTheme {
        PaymentCheckoutScreen(
            onPaymentSuccess = {},
            onBack = {}
        )
    }
}
