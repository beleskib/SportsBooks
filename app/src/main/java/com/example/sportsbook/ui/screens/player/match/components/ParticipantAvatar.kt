package com.example.sportsbook.ui.screens.player.match.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.ParticipantRole

@Composable
fun ParticipantAvatar(
    name: String?,
    photoUrl: String?,
    role: ParticipantRole = ParticipantRole.PLAYER,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = name,
                        modifier = Modifier.size(size * 0.6f),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            if (role == ParticipantRole.HOST) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Host",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name ?: "Player",
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontWeight = if (role == ParticipantRole.HOST) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantAvatarPreview() {
    ParticipantAvatar(
        name = "John Doe",
        photoUrl = null,
        role = ParticipantRole.HOST
    )
}
