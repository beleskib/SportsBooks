package com.example.sportsbook.ui.screens.player.favorites

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.FavoriteEntityType
import com.example.sportsbook.domain.model.Favorite
import com.example.sportsbook.ui.common.LoadingIndicator

private val TABS = listOf(
    Triple("Venues", FavoriteEntityType.VENUE, Icons.Default.Place),
    Triple("Coaches", FavoriteEntityType.COACH, Icons.Default.EmojiPeople),
    Triple("Matches", FavoriteEntityType.MATCH, Icons.Default.SportsSoccer)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                TABS.forEachIndexed { index, (title, _, icon) ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title) },
                        icon = { Icon(icon, contentDescription = title, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            if (uiState.isLoading) {
                LoadingIndicator()
            } else {
                val (_, entityType, _) = TABS[uiState.selectedTab]
                val filtered = uiState.favorites.filter { it.entityType == entityType }

                if (filtered.isEmpty()) {
                    FavoritesEmptyState(
                        entityTypeName = TABS[uiState.selectedTab].first
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(filtered, key = { it.id }) { favorite ->
                            FavoriteItem(
                                favorite = favorite,
                                onRemove = {
                                    viewModel.removeFavorite(
                                        entityType = favorite.entityType.value,
                                        entityId = favorite.entityId
                                    )
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesEmptyState(
    entityTypeName: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No favorite $entityTypeName yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Browse and tap the heart icon to save your favorites",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun FavoriteItem(
    favorite: Favorite,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entityIcon = when (favorite.entityType) {
        FavoriteEntityType.VENUE -> Icons.Default.Place
        FavoriteEntityType.COACH -> Icons.Default.EmojiPeople
        FavoriteEntityType.MATCH -> Icons.Default.SportsSoccer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = entityIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${favorite.entityType.value.replaceFirstChar { it.uppercase() }} #${favorite.entityId}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                if (!favorite.createdAt.isNullOrBlank()) {
                    Text(
                        text = "Saved on ${favorite.createdAt.take(10)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove from favorites",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    MaterialTheme {
        FavoritesScreen(onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteItemPreview() {
    MaterialTheme {
        FavoriteItem(
            favorite = Favorite(
                id = 1,
                userId = 10,
                entityType = FavoriteEntityType.VENUE,
                entityId = 42,
                createdAt = "2026-03-10T10:00:00Z"
            ),
            onRemove = {}
        )
    }
}
