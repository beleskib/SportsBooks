package com.example.sportsbook.ui.screens.player.friends

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
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark

@Composable
fun FriendRequestsScreen(
    onBack: () -> Unit,
    viewModel: FriendRequestsViewModel = hiltViewModel(),
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
            Column(modifier = Modifier.weight(1f)) {
                Text("Friend Requests", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                if (uiState.requests.isNotEmpty()) {
                    Text("${uiState.requests.size} pending", fontSize = 13.sp, color = GreenAccent)
                }
            }
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }
            uiState.error != null -> ErrorView(message = uiState.error!!, onRetry = viewModel::loadRequests)
            uiState.requests.isEmpty() -> RequestsEmptyState()
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(uiState.requests, key = { it.id }) { request ->
                        FriendRequestCard(
                            request = request,
                            onAccept = { viewModel.respondToRequest(request.id, accept = true) },
                            onDecline = { viewModel.respondToRequest(request.id, accept = false) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun RequestsEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Text("📬", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No pending requests", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            Text("Friend requests you receive will appear here", fontSize = 14.sp, color = DarkTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun FriendRequestCard(
    request: Friendship,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(GreenDark),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = request.friendName?.firstOrNull()?.uppercase() ?: "?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(request.friendName ?: "Unknown Player", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            if (!request.createdAt.isNullOrBlank()) {
                Text("Requested ${request.createdAt.take(10)}", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
            }
        }

        // Accept
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GreenAccent.copy(alpha = 0.15f))
                .border(1.dp, GreenAccent.copy(alpha = 0.5f), CircleShape)
                .clickable(onClick = onAccept),
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", fontSize = 16.sp, color = GreenAccent, fontWeight = FontWeight.Bold)
        }

        // Decline
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFB71C1C).copy(alpha = 0.12f))
                .border(1.dp, Color(0xFFEF5350).copy(alpha = 0.4f), CircleShape)
                .clickable(onClick = onDecline),
            contentAlignment = Alignment.Center,
        ) {
            Text("✕", fontSize = 14.sp, color = Color(0xFFEF5350), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun FriendRequestsScreenPreview() {
    val sampleRequests = listOf(
        Friendship(id = 5, friendId = 99, friendName = "Jordan Smith", friendPhotoUrl = null, status = "pending", createdAt = "2026-03-10T12:00:00Z"),
        Friendship(id = 6, friendId = 100, friendName = "Taylor Brooks", friendPhotoUrl = null, status = "pending", createdAt = "2026-03-12T09:30:00Z"),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text("Friend Requests", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                    Text("2 pending", fontSize = 13.sp, color = GreenAccent)
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(sampleRequests) { request ->
                    FriendRequestCard(request = request, onAccept = {}, onDecline = {})
                }
            }
        }
}
