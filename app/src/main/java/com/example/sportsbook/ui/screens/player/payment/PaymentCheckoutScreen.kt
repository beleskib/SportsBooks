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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.SolidColor
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
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GoldLight
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import kotlin.math.roundToInt

private val XpGreen = Color(0xFF2E7D32)
private val XpGreenLight = Color(0xFF4CAF50)
private val SplitBlue = Color(0xFF1565C0)
private val SplitBlueLight = Color(0xFF42A5F5)

@Composable
fun PaymentCheckoutScreen(
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: PaymentCheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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

    val finalAmount = (uiState.amount - uiState.xpDiscount).coerceAtLeast(0.0)
    val isFreeWithXp = uiState.amount > 0 && finalAmount == 0.0 && uiState.xpRedeemed
    val displayPayAmount = if (uiState.splitEnabled && uiState.selectedFriends.isNotEmpty()) {
        uiState.yourShare
    } else {
        finalAmount
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.sportsbook.ui.theme.DarkBg),
    ) {
        if (uiState.isLoadingBooking) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = GreenAccent)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Loading booking details...", color = com.example.sportsbook.ui.theme.DarkTextSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp),
            ) {
                // ── Custom header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(com.example.sportsbook.ui.theme.DarkSurface)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = com.example.sportsbook.ui.theme.DarkTextPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Payment", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = com.example.sportsbook.ui.theme.DarkTextPrimary)
                }

                // ── Step indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(true, true, true).forEachIndexed { i, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i < 2) GreenDark else GreenAccent),
                        )
                    }
                }

                // ── Booking Details Card ─────────────────────────────
                BookingDetailsCard(
                    venueName = uiState.venueName,
                    coachName = uiState.coachName,
                    slotDate = uiState.slotDate,
                    slotTime = uiState.slotTime,
                    sportType = uiState.sportType,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
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
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
                    )
                }

                // ── Split with Friends Section ──────────────────────
                if (!isFreeWithXp) {
                    SplitWithFriendsSection(
                        splitEnabled = uiState.splitEnabled,
                        friends = uiState.filteredFriends,
                        selectedFriends = uiState.selectedFriends,
                        searchQuery = uiState.friendSearchQuery,
                        isLoading = uiState.isLoadingFriends,
                        totalAmount = finalAmount,
                        splitPartySize = uiState.splitPartySize,
                        yourShare = uiState.yourShare,
                        onToggleSplit = viewModel::toggleSplit,
                        onSearchQueryChange = viewModel::onFriendSearchQueryChange,
                        onToggleFriend = viewModel::toggleFriendSelection,
                        isFriendSelected = viewModel::isFriendSelected,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
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
                    splitEnabled = uiState.splitEnabled && uiState.selectedFriends.isNotEmpty(),
                    splitPartySize = uiState.splitPartySize,
                    yourShare = uiState.yourShare,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
                )

                // ── Secure note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🔒 Secured payment • Firebase encrypted", fontSize = 12.sp, color = Color(0xFF555555))
                }
            }
        }

        // ── Bottom pay button
        if (!uiState.isLoadingBooking) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, com.example.sportsbook.ui.theme.DarkBg, com.example.sportsbook.ui.theme.DarkBg)),
                    )
                    .padding(16.dp),
            ) {
                when {
                    uiState.isCreatingIntent || uiState.isProcessingPayment || uiState.isCreatingSplit -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator(color = GreenAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when {
                                    uiState.isCreatingSplit -> "Setting up split..."
                                    uiState.isCreatingIntent -> "Preparing payment..."
                                    else -> "Processing..."
                                },
                                color = com.example.sportsbook.ui.theme.DarkTextSecondary,
                                fontSize = 14.sp,
                            )
                        }
                    }
                    isFreeWithXp -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(XpGreen, XpGreenLight)))
                                .clickable { viewModel.initiatePayment() }
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("⭐ Complete Booking — Free with XP!", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (uiState.splitEnabled && uiState.selectedFriends.isNotEmpty())
                                        Brush.linearGradient(listOf(SplitBlue, SplitBlueLight))
                                    else
                                        Brush.linearGradient(listOf(GreenAccent, GreenDark)),
                                )
                                .clickable { viewModel.initiatePayment() }
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            val amt = if (uiState.amount > 0) displayPayAmount else uiState.amount
                            val label = when {
                                uiState.splitEnabled && uiState.selectedFriends.isNotEmpty() ->
                                    "Pay Your Share — ${"%.0f".format(displayPayAmount)} ден"
                                amt > 0 -> "🔒 Pay ${"%.0f".format(amt)} ден"
                                else -> "🔒 Proceed to Payment"
                            }
                            Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp),
        )
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        val entityName = venueName ?: coachName ?: "Booking"
        val entityIcon = if (venueName != null) Icons.Default.LocationOn else Icons.Default.Person
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = entityIcon, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = entityName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            slotDate?.let { date -> DetailChip(label = "Date", value = date) }
            slotTime?.let { time -> DetailChip(label = "Time", value = time) }
            sportType?.let { sport -> DetailChip(label = "Sport", value = sport.lowercase().replaceFirstChar { it.uppercase() }) }
        }
    }
}

@Composable
private fun DetailChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = DarkTextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
    }
}

// ═════════════════════════════════════════════════════════════════
// Split with Friends Section
// ═════════════════════════════════════════════════════════════════
@Composable
private fun SplitWithFriendsSection(
    splitEnabled: Boolean,
    friends: List<Friendship>,
    selectedFriends: List<Friendship>,
    searchQuery: String,
    isLoading: Boolean,
    totalAmount: Double,
    splitPartySize: Int,
    yourShare: Double,
    onToggleSplit: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleFriend: (Friendship) -> Unit,
    isFriendSelected: (Friendship) -> Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(SplitBlue.copy(alpha = 0.10f), SplitBlueLight.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))))
            .border(1.dp, Brush.linearGradient(listOf(SplitBlue.copy(alpha = 0.4f), SplitBlueLight.copy(alpha = 0.2f))), RoundedCornerShape(16.dp)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Header with toggle ──────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(SplitBlue.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SplitBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Split with Friends", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        Text("Split the cost equally", fontSize = 11.sp, color = DarkTextSecondary)
                    }
                }
                Switch(checked = splitEnabled, onCheckedChange = onToggleSplit,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SplitBlue))
            }

            // ── Expanded content when enabled ───────────────────
            AnimatedVisibility(visible = splitEnabled, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedFriends.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            selectedFriends.forEach { friend ->
                                SelectedFriendChip(name = friend.friendName ?: "Friend", onRemove = { onToggleFriend(friend) })
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Search field
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkBg)
                            .border(1.dp, SplitBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = DarkTextSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery, onValueChange = onSearchQueryChange, singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = DarkTextPrimary, fontSize = 14.sp),
                            cursorBrush = SolidColor(GreenAccent), modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                Box { if (searchQuery.isEmpty()) Text("Search friends...", fontSize = 14.sp, color = Color(0xFF555555)); inner() }
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = SplitBlueLight)
                        }
                    } else if (friends.isEmpty()) {
                        Text(text = if (searchQuery.isNotBlank()) "No friends found" else "No friends yet",
                            fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            friends.take(5).forEach { friend ->
                                FriendRow(friend = friend, isSelected = isFriendSelected(friend), onToggle = { onToggleFriend(friend) })
                            }
                            if (friends.size > 5) {
                                Text("Search to find more friends...", fontSize = 11.sp, color = DarkTextSecondary, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                            }
                        }
                    }

                    if (selectedFriends.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SplitBlue.copy(alpha = 0.2f)))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SplitBlue.copy(alpha = 0.08f)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text("Splitting $splitPartySize ways", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SplitBlue)
                                Text("${"%.0f".format(totalAmount)} ден ÷ $splitPartySize people", fontSize = 11.sp, color = SplitBlue.copy(alpha = 0.7f))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${"%.0f".format(yourShare)} ден", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SplitBlue)
                                Text("each", fontSize = 11.sp, color = SplitBlue.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedFriendChip(
    name: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SplitBlue.copy(alpha = 0.15f))
            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SplitBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove),
                tint = SplitBlue.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun FriendRow(
    friend: Friendship,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val bgColor = if (isSelected) SplitBlue.copy(alpha = 0.10f) else Color.Transparent
    val borderColor = if (isSelected) SplitBlue.copy(alpha = 0.4f) else DarkBorder.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isSelected) SplitBlue.copy(alpha = 0.2f) else DarkSurface),
            contentAlignment = Alignment.Center,
        ) {
            val initial = friend.friendName?.firstOrNull()?.uppercase() ?: "?"
            Text(initial, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) SplitBlue else DarkTextSecondary)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = friend.friendName ?: "Unknown",
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) SplitBlue else DarkTextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isSelected) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Selected", tint = SplitBlue, modifier = Modifier.size(20.dp))
        }
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(GreenDark.copy(alpha = 0.15f), GreenAccent.copy(alpha = 0.08f), GoldLight.copy(alpha = 0.05f))))
            .border(1.dp, Brush.linearGradient(listOf(GreenAccent.copy(alpha = 0.5f), GoldLight.copy(alpha = 0.3f))), RoundedCornerShape(16.dp)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Header row: XP icon + title + balance ────────
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Brush.radialGradient(colors = listOf(GreenAccent, GreenDark))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("XP", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Use XP as Discount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        Text("Level $currentLevel  •  100 XP = 1 ден", fontSize = 11.sp, color = DarkTextSecondary)
                    }
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(GreenAccent.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("$availableXp XP", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GreenAccent)
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
                                fontSize = 12.sp,
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
                            Text("Quick select", fontSize = 11.sp, color = DarkTextSecondary, modifier = Modifier.padding(bottom = 8.dp))

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
                                    thumbColor = GreenAccent,
                                    activeTrackColor = GreenAccent,
                                    inactiveTrackColor = GreenAccent.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("0 XP", fontSize = 11.sp, color = DarkTextSecondary)
                                Text(
                                    text = if (xpToRedeem > 0) "$xpToRedeem XP = ${"%.0f".format(xpToRedeem / 100.0)} ден discount" else "Slide to select amount",
                                    fontSize = 11.sp,
                                    fontWeight = if (xpToRedeem > 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (xpToRedeem > 0) GreenAccent else DarkTextSecondary,
                                )
                                Text("$maxRedeemable XP", fontSize = 11.sp, color = DarkTextSecondary)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val canApply = xpToRedeem > 0 && !isRedeemingXp
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (canApply) GreenAccent else GreenAccent.copy(alpha = 0.3f))
                                    .then(if (canApply) Modifier.clickable(onClick = onApplyXpDiscount) else Modifier),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isRedeemingXp) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Applying...", fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                } else if (xpToRedeem > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Apply $xpToRedeem XP (save ${"%.0f".format(xpToRedeem / 100.0)} ден)", fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                } else {
                                    Text("Select XP to Redeem", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }

                            if (canCoverFull) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("You have enough XP to cover this booking entirely!", fontSize = 11.sp, color = XpGreenLight, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
                            }
                        } else {
                            Text("You need at least 100 XP to apply a discount.", fontSize = 13.sp, color = DarkTextSecondary)
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
    val bgColor = if (isSelected) GreenAccent.copy(alpha = 0.25f) else Color.Transparent
    val borderColor = if (isSelected) GreenAccent else DarkBorder
    val textColor = if (isSelected) GreenAccent else DarkTextPrimary

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
    splitEnabled: Boolean = false,
    splitPartySize: Int = 1,
    yourShare: Double = 0.0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text("Price Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

            PriceRow(
                label = "Booking Price",
                value = "${"%.0f".format(originalPrice)} ден",
                decoration = if (xpRedeemed && xpDiscount > 0) TextDecoration.LineThrough else null,
                valueColor = if (xpRedeemed && xpDiscount > 0) DarkTextSecondary else DarkTextPrimary,
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

            // Split line
            AnimatedVisibility(
                visible = splitEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    PriceRow(
                        label = "Split $splitPartySize ways",
                        value = "÷ $splitPartySize",
                        valueColor = SplitBlue,
                        labelColor = SplitBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (splitEnabled) "Your Share" else "Total", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                if (isFreeWithXp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FREE", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = XpGreenLight)
                    }
                } else {
                    val displayAmount = if (splitEnabled) yourShare else finalAmount
                    Text(
                        text = "${"%.0f".format(displayAmount)} ден",
                        fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                        color = if (splitEnabled) SplitBlue else GreenAccent,
                    )
                }
            }
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    valueColor: Color = DarkTextPrimary,
    labelColor: Color = DarkTextSecondary,
    decoration: TextDecoration? = null,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = labelColor)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = valueColor, textDecoration = decoration)
    }
}

// ═════════════════════════════════════════════════════════════════
// Preview
// ═════════════════════════════════════════════════════════════════
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PaymentCheckoutScreenPreview() {
    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 100.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Payment", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }
            BookingDetailsCard(
                venueName = "Arena Sports Center", coachName = null,
                slotDate = "2026-03-25", slotTime = "10:00 - 11:00", sportType = "BASKETBALL",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            XpSpendSection(
                availableXp = 2450, currentLevel = 5, totalPrice = 1500.0,
                xpToRedeem = 500, xpDiscount = 5.0, xpRedeemed = false, isRedeemingXp = false,
                onXpSliderChange = {}, onQuickXp = {}, onApplyXpDiscount = {},
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            SplitWithFriendsSection(
                splitEnabled = true,
                friends = listOf(Friendship(id = 1, friendId = 10, friendName = "Marko"), Friendship(id = 2, friendId = 11, friendName = "Stefan")),
                selectedFriends = listOf(Friendship(id = 1, friendId = 10, friendName = "Marko")),
                searchQuery = "", isLoading = false, totalAmount = 1000.0,
                splitPartySize = 2, yourShare = 500.0,
                onToggleSplit = {}, onSearchQueryChange = {}, onToggleFriend = {},
                isFriendSelected = { it.friendId == 10L },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            PriceBreakdownCard(
                originalPrice = 1000.0, xpDiscount = 0.0, xpRedeemed = false, xpAmount = 0,
                finalAmount = 1000.0, isFreeWithXp = false, splitEnabled = true, splitPartySize = 2, yourShare = 500.0,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, DarkBg, DarkBg))).padding(16.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(SplitBlue, SplitBlueLight))),
                contentAlignment = Alignment.Center,
            ) {
                Text("Pay Your Share — 500 ден", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
