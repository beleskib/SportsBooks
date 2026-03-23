package com.example.sportsbook.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FavoriteButton(
    isFavorited: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorited) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorited) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun FavoriteButtonPreview() {
    MaterialTheme {
        FavoriteButton(isFavorited = false, onToggle = {})
    }
}

@Preview
@Composable
private fun FavoriteButtonFavoritedPreview() {
    MaterialTheme {
        FavoriteButton(isFavorited = true, onToggle = {})
    }
}
