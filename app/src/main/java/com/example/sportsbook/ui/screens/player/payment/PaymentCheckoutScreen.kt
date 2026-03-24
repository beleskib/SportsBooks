package com.example.sportsbook.ui.screens.player.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.theme.GoldDark
import com.example.sportsbook.ui.theme.GoldLight
import com.example.sportsbook.ui.theme.USOpenGold
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlin.math.roundToInt

private val XpGreen = Color(0xFF2E7D32)
private val XpGreenLight = Color(0xFF4CAF50)

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
                title = { Text("Checkout") },
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
        if (uiState.isLoadingBooking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading booking details...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Booking Details Card ─────────────────────────────
                BookingDetailsCard(
                    venueName = uiState.venueName,
                    coachName = uiState.coachName,
                    slotDate = uiState.slotDate,
                    slotTime = uiState.slotTime,
                    sportType = uiState.sportType,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // ── XP Spend Section ─────────────────────────────────
                if (uiState.amount > 0 && uiState.availableXp > 0) {
                    XpSpendSection(
                        availableXp = uiState.availableXp,
                        currentLevel = uiState.currentLevel,
                        totalPrice = uiState.amount,
                        xpToRedeem = uiState.xpToRedeem,
                        xpDiscount = uiState.xpDiscount,
                        xpRedeemed = uiState.xpRedeemed,
                        isRedeemingXp = uiState.isRedeemingXp,
                        onXpSliderChange = viewModel::onXpSliderChange,
                        onQuickXp = viewModel::setQuickXp,
                        onApplyXpDiscount = viewModel::redeemXp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // ── Price Breakdown ──────────────────────────────────
                PriceBreakdownCard(
                    originalPrice = uiState.amount,
                    xpDiscount = if (uiState.xpRedeemed) uiState.xpDiscount else 0.0,
                    xpRedeemed = uiState.xpRedeemed,
                    xpAmount = uiState.xpToRedeem,
                    finalAmount = finalAmount,
                    isFreeWithXp = isFreeWithXp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Pay Button ───────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = XpGreen
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Complete Booking — Free with XP!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        uiState.clientSecret == null -> {
                            Button(
                                onClick = { viewModel.initiatePayment() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                val displayAmount = if (uiState.amount > 0) finalAmount else uiState.amount
                                Text(
                                    text = if (displayAmount > 0) "Pay ${"%.0f".format(displayAmount)} ден" else "Proceed to Payment",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// Booking Details Card
// ═════════════════════════════════════════════════════════════════
@Composable
private fun BookingDetailsCard(
    venueName: String?,
    coachName: String?,
    slotDate: String?,
    slotTime: String?,
    sportType: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Entity name
            val entityName = venueName ?: coachName ?: "Booking"
            val entityIcon = if (venueName != null) Icons.Default.LocationOn else Icons.Default.Person
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = entityIcon,
                    contentDescription = null,
                    tint = USOpenGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date & Time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                slotDate?.let { date ->
                    DetailChip(label = "Date", value = date)
                }
                slotTime?.let { time ->
                    DetailChip(label = "Time", value = time)
                }
                sportType?.let { sport ->
                    DetailChip(
                        label = "Sport",
                        value = sport.lowercase().replaceFirstChar { it.uppercase() }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// XP Spend Section — the main feature
// ═════════════════════════════════════════════════════════════════
@Composable
private fun XpSpendSection(
    availableXp: Int,
    currentLevel: Int,
    totalPrice: Double,
    xpToRedeem: Int,
    xpDiscount: Double,
    xpRedeemed: Boolean,
    isRedeemingXp: Boolean,
    onXpSliderChange: (Int) -> Unit,
    onQuickXp: (Int) -> Unit,
    onApplyXpDiscount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxRedeemable = minOf(availableXp, (totalPrice * 100).roundToInt())
    val maxDiscountMkd = maxRedeemable / 100.0
    val canCoverFull = maxRedeemable >= (totalPrice * 100).roundToInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GoldDark.copy(alpha = 0.15f),
                            USOpenGold.copy(alpha = 0.08f),
                            GoldLight.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(USOpenGold.copy(alpha = 0.5f), GoldLight.copy(alpha = 0.3f))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // ── Header row: XP icon + title + balance ────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // XP badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(USOpenGold, GoldDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "XP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Use XP as Discount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Level $currentLevel  •  100 XP = 1 ден",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // XP balance badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(USOpenGold.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$availableXp XP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = USOpenGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Applied state ────────────────────────────────
                AnimatedVisibility(
                    visible = xpRedeemed,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(XpGreen.copy(alpha = 0.12f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = XpGreenLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Discount Applied!",
                                fontWeight = FontWeight.Bold,
                                color = XpGreenLight,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "$xpToRedeem XP redeemed for ${"%.0f".format(xpDiscount)} ден off",
                                style = MaterialTheme.typography.bodySmall,
                                color = XpGreenLight.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // ── Slider + quick picks (pre-apply) ─────────────
                AnimatedVisibility(
                    visible = !xpRedeemed,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        if (maxRedeemable > 0) {
                            // Quick pick buttons
                            Text(
                                text = "Quick select",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val quickPicks = buildList {
                                    if (maxRedeemable >= 100) add(100 to "1 ден")
                                    if (maxRedeemable >= 500) add(500 to "5 ден")
                                    if (maxRedeemable >= 1000) add(1000 to "10 ден")
                                    if (canCoverFull && maxRedeemable > 0) {
                                        add(maxRedeemable to "Max")
                                    } else if (maxRedeemable > 0 && !contains(maxRedeemable to "Max")) {
                                        // Add the max option if not already a quick pick
                                        val maxLabel = "${"%.0f".format(maxDiscountMkd)} ден"
                                        if (none { it.first == maxRedeemable }) {
                                            add(maxRedeemable to maxLabel)
                                        }
                                    }
                                }

                                quickPicks.forEach { (xp, label) ->
                                    val isSelected = xpToRedeem == xp
                                    QuickPickChip(
                                        xp = xp,
                                        label = label,
                                        isSelected = isSelected,
                                        onClick = { onQuickXp(xp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Slider
                            val sliderProgress by animateFloatAsState(
                                targetValue = xpToRedeem.toFloat(),
                                animationSpec = spring(stiffness = Spring.StiffnessLow),
                                label = "xp_slider"
                            )

                            Slider(
                                value = sliderProgress,
                                onValueChange = { onXpSliderChange(it.roundToInt()) },
                                valueRange = 0f..maxRedeemable.toFloat(),
                                steps = ((maxRedeemable / 100) - 1).coerceAtLeast(0),
                                colors = SliderDefaults.colors(
                                    thumbColor = USOpenGold,
                                    activeTrackColor = USOpenGold,
                                    inactiveTrackColor = USOpenGold.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Value label
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "0 XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (xpToRedeem > 0) {
                                        "$xpToRedeem XP = ${"%.0f".format(xpToRedeem / 100.0)} ден discount"
                                    } else {
                                        "Slide to select amount"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (xpToRedeem > 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (xpToRedeem > 0) USOpenGold else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$maxRedeemable XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Apply button
                            Button(
                                onClick = onApplyXpDiscount,
                                enabled = xpToRedeem > 0 && !isRedeemingXp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = USOpenGold,
                                    contentColor = Color.Black,
                                    disabledContainerColor = USOpenGold.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isRedeemingXp) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Applying...", fontWeight = FontWeight.Bold)
                                } else if (xpToRedeem > 0) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Apply $xpToRedeem XP (save ${"%.0f".format(xpToRedeem / 100.0)} ден)",
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text("Select XP to Redeem", fontWeight = FontWeight.Bold)
                                }
                            }

                            // Cover full hint
                            if (canCoverFull) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "You have enough XP to cover this booking entirely!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = XpGreenLight,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Text(
                                text = "You need at least 100 XP to apply a discount.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPickChip(
    xp: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) USOpenGold.copy(alpha = 0.25f) else Color.Transparent
    val borderColor = if (isSelected) USOpenGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val textColor = if (isSelected) USOpenGold else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1
            )
            Text(
                text = "$xp XP",
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// Price Breakdown Card
// ═════════════════════════════════════════════════════════════════
@Composable
private fun PriceBreakdownCard(
    originalPrice: Double,
    xpDiscount: Double,
    xpRedeemed: Boolean,
    xpAmount: Int,
    finalAmount: Double,
    isFreeWithXp: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Price Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Original price
            PriceRow(
                label = "Booking Price",
                value = "${"%.0f".format(originalPrice)} ден",
                decoration = if (xpRedeemed && xpDiscount > 0) TextDecoration.LineThrough else null,
                valueColor = if (xpRedeemed && xpDiscount > 0)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurface
            )

            // XP Discount line
            AnimatedVisibility(
                visible = xpRedeemed && xpDiscount > 0,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    PriceRow(
                        label = "XP Discount ($xpAmount XP)",
                        value = "-${"%.0f".format(xpDiscount)} ден",
                        valueColor = XpGreenLight,
                        labelColor = XpGreenLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Final total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isFreeWithXp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = USOpenGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FREE",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = XpGreenLight
                        )
                    }
                } else {
                    Text(
                        text = "${"%.0f".format(finalAmount)} ден",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    decoration: TextDecoration? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textDecoration = decoration
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// Preview
// ═════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun PaymentCheckoutScreenPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Checkout") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            ) {
                BookingDetailsCard(
                    venueName = "Arena Sports Center",
                    coachName = null,
                    slotDate = "2026-03-25",
                    slotTime = "10:00 - 11:00",
                    sportType = "BASKETBALL",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                XpSpendSection(
                    availableXp = 2450,
                    currentLevel = 5,
                    totalPrice = 1500.0,
                    xpToRedeem = 500,
                    xpDiscount = 5.0,
                    xpRedeemed = false,
                    isRedeemingXp = false,
                    onXpSliderChange = {},
                    onQuickXp = {},
                    onApplyXpDiscount = {},
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                PriceBreakdownCard(
                    originalPrice = 1500.0,
                    xpDiscount = 5.0,
                    xpRedeemed = true,
                    xpAmount = 500,
                    finalAmount = 1495.0,
                    isFreeWithXp = false,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Pay 1495 ден", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
