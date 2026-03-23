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

private fun skillLevelLabel(level: Int): String = when (level) {
    1 -> "Newbie"
    2 -> "Beginner"
    3 -> "Intermediate"
    4 -> "Semi Pro"
    5 -> "Pro"
    else -> "Lvl $level"
}

@Preview(showBackground = true)
@Composable
private fun SkillRangeBadgePreview() {
    SkillRangeBadge(minLevel = 2, maxLevel = 4)
}
