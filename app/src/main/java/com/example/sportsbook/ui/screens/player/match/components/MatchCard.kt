package com.example.sportsbook.ui.screens.player.match.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import com.example.sportsbook.domain.enums.MatchStatus
import com.example.sportsbook.domain.enums.MatchType
import com.example.sportsbook.domain.enums.MatchVisibility
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.ui.common.toPrettyDate
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.OrangeAccent

@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        // Header: title + status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = match.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            MatchStatusBadge(status = match.status)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sport + skill chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(GreenAccent.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(match.sportType.displayName, fontSize = 10.sp, color = GreenAccent, fontWeight = FontWeight.SemiBold)
            }
            if (match.minSkillLevel != null && match.maxSkillLevel != null) {
                SkillRangeBadge(minLevel = match.minSkillLevel, maxLevel = match.maxSkillLevel)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Info rows
        MatchInfoRow(icon = Icons.Default.CalendarToday, text = match.matchDate.toPrettyDate())
        MatchInfoRow(icon = Icons.Default.Schedule, text = match.displayTime)
        MatchInfoRow(icon = Icons.Default.LocationOn, text = match.displayLocation)

        Spacer(modifier = Modifier.height(8.dp))

        // Player count progress bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = GreenAccent,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${match.currentPlayers}/${match.maxPlayers} players",
                fontSize = 12.sp,
                color = DarkTextSecondary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Custom progress bar
            val progress = (match.currentPlayers.toFloat() / match.maxPlayers.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(DarkBorder),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(GreenAccent),
                )
            }
            if (match.spotsLeft > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${match.spotsLeft} left",
                    fontSize = 10.sp,
                    color = GreenAccent,
                )
            }
        }

        // Cost
        Spacer(modifier = Modifier.height(4.dp))
        if (!match.isFree) {
            Text(
                text = "${"%.2f".format(match.costPerPlayer)} MKD/player",
                fontSize = 12.sp,
                color = OrangeAccent,
            )
        } else {
            Text(
                text = "Free",
                fontSize = 12.sp,
                color = GreenAccent,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun MatchInfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = DarkTextSecondary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = DarkTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
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
            isFree = true,
        ),
        onClick = {},
    )
}
