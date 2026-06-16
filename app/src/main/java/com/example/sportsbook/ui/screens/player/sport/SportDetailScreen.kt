package com.example.sportsbook.ui.screens.player.sport

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.sportsbook.ui.common.DiscountBadge
import com.example.sportsbook.ui.common.EmptyStateView
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.PriceTag
import com.example.sportsbook.ui.common.RatingBar
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark

@Composable
fun SportDetailScreen(
    sportType: String,
    onVenueClick: (Long) -> Unit,
    onCoachClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: SportDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Venues", "Coaches")

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
            Text(
                text = uiState.sportDisplayName.ifBlank { sportType },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary,
                modifier = Modifier.weight(1f),
            )
        }

        // ── Tabs ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) GreenAccent else DarkSurface)
                        .border(1.dp, if (isSelected) GreenAccent else DarkBorder, RoundedCornerShape(50))
                        .clickable { selectedTabIndex = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else DarkTextSecondary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorView(message = uiState.error!!, onRetry = viewModel::loadData)
            selectedTabIndex == 0 -> {
                if (uiState.venues.isEmpty()) {
                    EmptyStateView(title = "No venues found")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                        items(uiState.venues) { venue ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(DarkSurface)
                                        .clickable { onVenueClick(venue.id) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GreenDark.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("🏟️", fontSize = 28.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(venue.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                                        Text(venue.address, fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RatingBar(rating = venue.avgRating)
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text("(${venue.totalReviews})", fontSize = 12.sp, color = DarkTextSecondary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        PriceTag(price = venue.pricePerHour, discountedPrice = venue.discountedPrice)
                                    }
                                }
                                if (venue.activeDiscount != null) {
                                    DiscountBadge(
                                        text = venue.activeDiscount.displayValue,
                                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 6.dp),
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
            else -> {
                if (uiState.coaches.isEmpty()) {
                    EmptyStateView(title = "No coaches found")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                        items(uiState.coaches) { coach ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(DarkSurface)
                                        .clickable { onCoachClick(coach.id) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GreenDark.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("🧑‍🏫", fontSize = 28.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(coach.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                                        if (!coach.specialization.isNullOrBlank()) {
                                            Text(coach.specialization, fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                                        }
                                        Text("${coach.experienceYears}y experience", fontSize = 13.sp, color = DarkTextSecondary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RatingBar(rating = coach.avgRating)
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text("(${coach.totalReviews})", fontSize = 12.sp, color = DarkTextSecondary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        PriceTag(price = coach.pricePerHour, discountedPrice = coach.discountedPrice)
                                    }
                                }
                                if (coach.activeDiscount != null) {
                                    DiscountBadge(
                                        text = coach.activeDiscount.displayValue,
                                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 6.dp),
                                    )
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

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SportDetailScreenPreview() {
    val sampleVenues = listOf(
        com.example.sportsbook.domain.model.Venue(id = 1L, name = "City Basketball Court", address = "123 Main St", pricePerHour = 30.0, avgRating = 4.5, totalReviews = 22),
        com.example.sportsbook.domain.model.Venue(id = 2L, name = "Downtown Sports Hall", address = "456 Park Ave", pricePerHour = 40.0, avgRating = 4.2, totalReviews = 11),
    )
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Basketball", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            listOf("Venues" to true, "Coaches" to false).forEach { (title, isSelected) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) GreenAccent else DarkSurface)
                        .border(1.dp, if (isSelected) GreenAccent else DarkBorder, RoundedCornerShape(50))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) Color.White else DarkTextSecondary)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(sampleVenues) { venue ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(GreenDark.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🏟️", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(venue.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                        Text(venue.address, fontSize = 13.sp, color = DarkTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        PriceTag(price = venue.pricePerHour, discountedPrice = venue.discountedPrice)
                    }
                }
            }
        }
    }
}
