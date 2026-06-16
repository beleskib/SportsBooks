package com.example.sportsbook.ui.screens.player.payment

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.SavedCard
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import com.example.sportsbook.ui.theme.OrangeAccent

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun PaymentMethodsScreen(
    onBack: () -> Unit = {},
    viewModel: PaymentMethodsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }

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
            Text("Payment Methods", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // ── Add Card Button ──────────────────────────────────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, GreenAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .background(GreenAccent.copy(alpha = 0.08f))
                                .clickable(enabled = !uiState.isAddingCard) { viewModel.initiateAddCard() }
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (uiState.isAddingCard) {
                                CircularProgressIndicator(color = GreenAccent, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("➕", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Add New Card", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
                                }
                            }
                        }
                    }

                    // ── Info text ─────────────────────────────────────────
                    item {
                        Text(
                            text = "Cards are securely stored by Stripe. We never see your full card number.",
                            fontSize = 12.sp,
                            color = DarkTextSecondary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // ── Setup Intent Info (when available) ───────────────
                    if (uiState.setupIntent != null) {
                        item {
                            SetupIntentCard(
                                onDismiss = { viewModel.clearSetupIntent() },
                                onCardAdded = {
                                    viewModel.clearSetupIntent()
                                    viewModel.loadCards()
                                }
                            )
                        }
                    }

                    // ── Error ─────────────────────────────────────────────
                    if (uiState.error != null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEF5350).copy(alpha = 0.15f))
                                    .padding(12.dp),
                            ) {
                                Text("❌  ${uiState.error}", fontSize = 13.sp, color = Color(0xFFEF5350))
                            }
                        }
                    }

                    // ── Success Message ───────────────────────────────────
                    if (uiState.successMessage != null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GreenAccent.copy(alpha = 0.15f))
                                    .padding(12.dp),
                            ) {
                                Text("✅  ${uiState.successMessage}", fontSize = 13.sp, color = GreenAccent)
                            }
                        }
                    }

                    // ── Section label ─────────────────────────────────────
                    if (uiState.cards.isNotEmpty()) {
                        item {
                            Text(
                                text = "SAVED CARDS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextSecondary,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
                            )
                        }
                    }

                    // ── Card list ─────────────────────────────────────────
                    items(uiState.cards, key = { it.id }) { card ->
                        SavedCardRow(
                            card = card,
                            showDeleteConfirm = showDeleteConfirm == card.id,
                            onSetDefault = { viewModel.setDefaultCard(card.id) },
                            onDeleteRequest = { showDeleteConfirm = card.id },
                            onDeleteConfirm = {
                                viewModel.deleteCard(card.id)
                                showDeleteConfirm = null
                            },
                            onDeleteCancel = { showDeleteConfirm = null },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // ── Empty state ───────────────────────────────────────
                    if (uiState.cards.isEmpty() && !uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💳", fontSize = 52.sp)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text("No saved cards", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                                    Text(
                                        "Add a card to speed up your bookings",
                                        fontSize = 13.sp,
                                        color = DarkTextSecondary,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun SavedCardRow(
    card: SavedCard,
    showDeleteConfirm: Boolean,
    onSetDefault: () -> Unit,
    onDeleteRequest: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
) {
    val brandEmoji = when (card.brand.lowercase()) {
        "visa" -> "💳"
        "mastercard" -> "🟠"
        "amex" -> "🔵"
        "discover" -> "🟡"
        else -> "💳"
    }
    val brandName = card.brand.replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(
                width = if (card.isDefault) 1.5.dp else 1.dp,
                color = if (card.isDefault) GreenAccent.copy(alpha = 0.5f) else DarkBorder,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Card icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF252525)),
                contentAlignment = Alignment.Center,
            ) {
                Text(brandEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Card details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(brandName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                    if (card.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GreenAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text("DEFAULT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GreenAccent, letterSpacing = 0.5.sp)
                        }
                    }
                }
                Text(
                    text = "•••• •••• •••• ${card.last4}",
                    fontSize = 14.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                    letterSpacing = 1.sp,
                )
                Text(
                    text = "Expires ${"%02d".format(card.expMonth)}/${card.expYear}",
                    fontSize = 12.sp,
                    color = DarkTextSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        // Action buttons
        if (!showDeleteConfirm) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!card.isDefault) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GreenDark.copy(alpha = 0.3f))
                            .clickable(onClick = onSetDefault)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Set as Default", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GreenAccent)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF5350).copy(alpha = 0.1f))
                        .clickable(onClick = onDeleteRequest)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Remove", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF5350))
                }
            }
        } else {
            // Delete confirmation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF5350).copy(alpha = 0.08f))
                    .padding(12.dp),
            ) {
                Text("Remove this card?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF5350))
                Text("This action cannot be undone", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            .clickable(onClick = onDeleteCancel)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Cancel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEF5350))
                            .clickable(onClick = onDeleteConfirm)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Remove", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupIntentCard(
    onDismiss: () -> Unit,
    onCardAdded: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, OrangeAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🔐", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Card Setup Ready", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                Text(
                    "Stripe payment sheet will open to securely collect your card details.",
                    fontSize = 12.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBg)
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Cancel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextSecondary)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onCardAdded)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Done", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PaymentMethodsScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Payment Methods", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }

        SavedCardRow(
            card = SavedCard(id = "pm_1", brand = "visa", last4 = "4242", expMonth = 12, expYear = 2027, isDefault = true),
            showDeleteConfirm = false,
            onSetDefault = {},
            onDeleteRequest = {},
            onDeleteConfirm = {},
            onDeleteCancel = {},
        )
        Spacer(modifier = Modifier.height(8.dp))
        SavedCardRow(
            card = SavedCard(id = "pm_2", brand = "mastercard", last4 = "8888", expMonth = 3, expYear = 2028, isDefault = false),
            showDeleteConfirm = false,
            onSetDefault = {},
            onDeleteRequest = {},
            onDeleteConfirm = {},
            onDeleteCancel = {},
        )
    }
}
