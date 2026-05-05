package com.example.sportsbook.ui.screens.player.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.model.Community
import com.example.sportsbook.ui.theme.BorderGray
import com.example.sportsbook.ui.theme.CardWhite
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.LightBg
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.TextPrimary
import com.example.sportsbook.ui.theme.TextSecondary
import com.example.sportsbook.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityListScreen(
    onCommunityClick: (Long) -> Unit,
    onCreateCommunity: () -> Unit,
    onBack: () -> Unit,
    viewModel: CommunityListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = LightBg,
        topBar = {
            TopAppBar(
                title = { Text("Communities", color = GoldAccent) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavBarBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateCommunity,
                containerColor = GoldAccent
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Community", tint = NavBarBg)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val tabs = listOf("My Communities", "Discover")
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = CardWhite,
                contentColor = GoldAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                        color = GoldAccent
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab.ordinal == index,
                        onClick = { viewModel.selectTab(CommunityTab.entries[index]) },
                        text = {
                            Text(
                                text = title,
                                color = if (uiState.selectedTab.ordinal == index) GoldAccent else TextSecondary
                            )
                        }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldAccent)
                    }
                }

                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                uiState.displayedCommunities.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Groups,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (uiState.selectedTab == CommunityTab.MY)
                                    "You haven't joined any communities yet"
                                else
                                    "No public communities found",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            if (uiState.selectedTab == CommunityTab.MY) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Create one or discover public communities",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextTertiary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.displayedCommunities, key = { it.id }) { community ->
                            CommunityCard(
                                community = community,
                                onClick = { onCommunityClick(community.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityCard(
    community: Community,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Community image / placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightBg),
                contentAlignment = Alignment.Center
            ) {
                if (!community.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = community.imageUrl,
                        contentDescription = community.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!community.sportType.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    SportBadge(sportType = community.sportType)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${community.memberCount} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (community.isPublic) {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = "Public",
                            tint = GoldAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Public",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SportBadge(sportType: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(GoldAccent.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = sportType.replace("_", " ")
                .lowercase()
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = GoldAccent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun CommunityListScreenPreview() {
    Scaffold(
        containerColor = LightBg,
        topBar = {
            TopAppBar(
                title = { Text("Communities", color = GoldAccent) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavBarBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = 0,
                containerColor = CardWhite
            ) {
                Tab(selected = true, onClick = {}, text = { Text("My Communities") })
                Tab(selected = false, onClick = {}, text = { Text("Discover") })
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    listOf(
                        Community(id = 1, name = "Basketball Crew", sportType = "basketball", memberCount = 12, isPublic = true),
                        Community(id = 2, name = "Tennis Club", sportType = "tennis", memberCount = 8)
                    )
                ) { community ->
                    CommunityCard(community = community, onClick = {})
                }
            }
        }
    }
}
