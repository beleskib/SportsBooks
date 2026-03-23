package com.example.sportsbook.ui.screens.player.match.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsbook.domain.enums.MatchStatus
import com.example.sportsbook.domain.enums.MatchType
import com.example.sportsbook.domain.enums.MatchVisibility
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.ui.common.toDisplayDate

@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: title + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                MatchStatusBadge(status = match.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sport + type chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(match.sportType.displayName, style = MaterialTheme.typography.labelSmall) }
                )
                if (match.minSkillLevel != null && match.maxSkillLevel != null) {
                    SkillRangeBadge(
                        minLevel = match.minSkillLevel,
                        maxLevel = match.maxSkillLevel
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info rows
            MatchInfoRow(icon = Icons.Default.CalendarToday, text = match.matchDate.toDisplayDate())
            MatchInfoRow(icon = Icons.Default.Schedule, text = match.displayTime)
            MatchInfoRow(icon = Icons.Default.LocationOn, text = match.displayLocation)

            Spacer(modifier = Modifier.height(8.dp))

            // Player count progress
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${match.currentPlayers}/${match.maxPlayers} players",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { match.currentPlayers.toFloat() / match.maxPlayers.toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape),
                )
                if (match.spotsLeft > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${match.spotsLeft} spots left",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Cost
            if (!match.isFree) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%.2f", match.costPerPlayer)} per player",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Free",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MatchInfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchCardPreview() {
    MatchCard(
        match = Match(
            id = 1,
            title = "Sunday Basketball Pickup",
            sportType = SportType.BASKETBALL,
            matchType = MatchType.STANDALONE,
            status = MatchStatus.OPEN,
            visibility = MatchVisibility.PUBLIC,
            matchDate = "2026-03-15",
            startTime = "10:00",
            endTime = "12:00",
            minPlayers = 6,
            maxPlayers = 10,
            currentPlayers = 7,
            minSkillLevel = 2,
            maxSkillLevel = 4,
            locationName = "Central Park Courts",
            isFree = true
        ),
        onClick = {}
    )
}
