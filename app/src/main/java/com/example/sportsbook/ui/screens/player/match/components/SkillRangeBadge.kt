package com.example.sportsbook.ui.screens.player.match.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SkillRangeBadge(
    minLevel: Int,
    maxLevel: Int,
    modifier: Modifier = Modifier
) {
    val label = if (minLevel == maxLevel) {
        skillLevelLabel(minLevel)
    } else {
        "${skillLevelLabel(minLevel)} – ${skillLevelLabel(maxLevel)}"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Canonical match-filter skill levels. Matches src/shared/types/skillLevel.ts.
 * DB stores INTEGER; legacy value 5 is collapsed to "Competitive" so historical
 * lobbies/matches with max=5 still render a sensible label.
 */
private fun skillLevelLabel(level: Int): String = when (level) {
    1 -> "Beginner"
    2 -> "Intermediate"
    3 -> "Advanced"
    4, 5 -> "Competitive"
    else -> "Lvl $level"
}

@Preview(showBackground = true)
@Composable
private fun SkillRangeBadgePreview() {
    SkillRangeBadge(minLevel = 2, maxLevel = 4)
}
