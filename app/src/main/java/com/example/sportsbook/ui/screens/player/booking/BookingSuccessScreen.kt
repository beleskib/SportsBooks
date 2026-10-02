package com.example.sportsbook.ui.screens.player.booking

import android.widget.Toast
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun BookingSuccessScreen(
    bookingId: Long,
    onViewDetails: () -> Unit,
    onInviteFriends: () -> Unit,
    onBackToHome: () -> Unit,
    viewModel: BookingSuccessViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val booking = uiState.booking
    val level = uiState.level

    val venueName = booking?.venue?.name ?: booking?.coach?.name ?: "Venue"
    val sportName = (booking?.venue?.sportType ?: booking?.coach?.sportType)
        ?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""
    val slot = booking?.timeSlot
    val displayDate = slot?.slotDate?.toDisplayDate() ?: "—"
    val displayTime = slot?.displayTime ?: "—"
    val duration = if (slot != null) {
        try {
            val start = slot.startTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
            val end = slot.endTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
            val mins = end - start
            if (mins >= 60) "${mins / 60}h${if (mins % 60 > 0) " ${mins % 60}m" else ""}" else "${mins}m"
        } catch (_: Exception) { "1h" }
    } else "—"
    val totalPrice = booking?.totalPrice?.toInt() ?: 0

    val xpRemaining = level?.let { it.xpRangeForLevel - it.xpForCurrentLevel } ?: 0
    val nextLevel = (level?.currentLevel ?: 1) + 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Success icon ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(GreenDark, GreenAccent))
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", fontSize = 56.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Booking Confirmed!",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DarkTextPrimary,
        )

        Text(
            text = "Your booking at $venueName has been confirmed. See you there!",
            fontSize = 14.sp,
            color = DarkTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )

        // ── Booking summary card ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .padding(20.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(venueName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                if (sportName.isNotBlank()) {
                    Text(sportName, fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow("Date", displayDate)
            SummaryRow("Time", displayTime)
            SummaryRow("Duration", duration)

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontSize = 13.sp, color = DarkTextSecondary)
                Text(
                    text = if (totalPrice > 0) "$totalPrice MKD" else "—",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenAccent,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBorder)
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Booking ID: #BK-${bookingId}",
                    fontSize = 12.sp,
                    color = DarkTextSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── XP card ─────────────────────────────────────────────────────
        if (level != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1B3A1E), DarkSurface))
                    )
                    .border(1.dp, GreenDark, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GreenAccent)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text("Level ${level.currentLevel}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("${level.totalXp} XP Total", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                    Text("$xpRemaining XP to Level $nextLevel", fontSize = 11.sp, color = Color(0xFF81C784))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── Action buttons ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GreenAccent)
                .clickable(onClick = onViewDetails)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("View Booking Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .clickable(onClick = onInviteFriends)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Invite Friends to Play", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val actionContext = LocalContext.current
            val shareActions = listOf("💬" to "Share", "📅" to "Add to Calendar", "📌" to "Get Directions")
            shareActions.forEach { (icon, label) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .clickable {
                            Toast.makeText(actionContext, "$label coming soon", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 10.dp),
                ) {
                    Text(icon, fontSize = 20.sp)
                    Text(label, fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(14.dp))
                .clickable(onClick = onBackToHome)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Back to Home", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextSecondary)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 13.sp, color = DarkTextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun BookingSuccessScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(GreenDark, GreenAccent))),
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", fontSize = 56.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Booking Confirmed!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DarkTextPrimary)
        Text("Your booking has been confirmed.", fontSize = 14.sp, color = DarkTextSecondary, textAlign = TextAlign.Center)
    }
}
