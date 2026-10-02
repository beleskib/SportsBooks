package com.example.sportsbook.ui.screens.player.review

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun WriteReviewScreen(
    bookingId: Long,
    onReviewSubmitted: () -> Unit,
    onBack: () -> Unit,
    viewModel: WriteReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Criteria ratings (local state — not in ViewModel)
    var criteriaCourtQuality by remember { mutableIntStateOf(5) }
    var criteriaCleanliness by remember { mutableIntStateOf(4) }
    var criteriaStaff by remember { mutableIntStateOf(3) }
    var criteriaValue by remember { mutableIntStateOf(4) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onReviewSubmitted()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp),
        ) {
            // ── Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
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
                Text("Write Review", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                Box(modifier = Modifier.size(40.dp)) // spacer to center title
            }

            // ── Venue preview card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF252525)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🎾", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("Venue", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                    Text("Booking #$bookingId", fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                }
            }

            // ── Overall rating
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "How was your experience?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= uiState.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "$i stars",
                            tint = if (i <= uiState.rating) Color(0xFFFFD700) else Color(0xFF555555),
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { viewModel.onRatingChange(i) },
                        )
                    }
                }
                if (uiState.rating > 0) {
                    Text(
                        text = ratingLabel(uiState.rating),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenAccent,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            // ── Criteria ratings
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
            ) {
                Text("Rate specific areas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(bottom = 12.dp))
                listOf(
                    Triple("Court Quality", criteriaCourtQuality) { v: Int -> criteriaCourtQuality = v },
                    Triple("Cleanliness", criteriaCleanliness) { v: Int -> criteriaCleanliness = v },
                    Triple("Staff", criteriaStaff) { v: Int -> criteriaStaff = v },
                    Triple("Value for Money", criteriaValue) { v: Int -> criteriaValue = v },
                ).forEachIndexed { _, (label, value, onChange) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(label, fontSize = 14.sp, color = DarkTextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= value) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (i <= value) Color(0xFFFFD700) else Color(0xFF555555),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { onChange(i) },
                                )
                            }
                        }
                    }
                }
            }

            // ── Review text input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp),
            ) {
                Text("Your Review", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(bottom = 10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .padding(16.dp),
                ) {
                    BasicTextField(
                        value = uiState.comment,
                        onValueChange = { viewModel.onCommentChange(it) },
                        textStyle = TextStyle(
                            color = DarkTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                        ),
                        cursorBrush = SolidColor(GreenAccent),
                        modifier = Modifier.fillMaxSize(),
                        decorationBox = { inner ->
                            if (uiState.comment.isEmpty()) {
                                Text("Share your experience with others...", fontSize = 14.sp, color = Color(0xFF555555))
                            }
                            inner()
                        },
                    )
                }
                Text(
                    "${uiState.comment.length} / 500",
                    fontSize = 12.sp,
                    color = Color(0xFF555555),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    textAlign = TextAlign.End,
                )
            }

            // ── Photos section (placeholder)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp),
            ) {
                Text("Add Photos (optional)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(bottom = 10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val photoContext = LocalContext.current
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .clickable {
                                Toast.makeText(photoContext, "Photo upload coming soon", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("+", fontSize = 24.sp, color = Color(0xFF555555))
                    }
                }
            }
        }

        // ── Bottom submit bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, DarkBg, DarkBg)),
                )
                .padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (!uiState.isLoading && uiState.rating > 0)
                            Brush.linearGradient(listOf(GreenAccent, GreenDark))
                        else
                            Brush.linearGradient(listOf(DarkSurface, DarkSurface)),
                    )
                    .clickable(enabled = uiState.rating > 0 && !uiState.isLoading) { viewModel.submitReview() }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (uiState.isLoading) "Submitting..." else "Submit Review",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp),
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun ratingLabel(rating: Int): String = when (rating) {
    1 -> "Poor"
    2 -> "Fair"
    3 -> "Good"
    4 -> "Great!"
    5 -> "Excellent!"
    else -> ""
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun WriteReviewScreenPreview() {
    WriteReviewScreen(
            bookingId = 1L,
            onReviewSubmitted = {},
            onBack = {},
        )
}
