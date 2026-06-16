package com.example.sportsbook.ui.screens.player.match.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsbook.domain.enums.MatchStatus

@Composable
fun MatchStatusBadge(
    status: MatchStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        MatchStatus.OPEN -> Color(0xFF4CAF50) to Color.White
        MatchStatus.FULL -> Color(0xFFFF9800) to Color.White
        MatchStatus.IN_PROGRESS -> Color(0xFF2196F3) to Color.White
        MatchStatus.COMPLETED -> Color(0xFF9E9E9E) to Color.White
        MatchStatus.CANCELLED -> Color(0xFFF44336) to Color.White
        MatchStatus.DRAFT -> Color(0xFF607D8B) to Color.White
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchStatusBadgePreview() {
    MatchStatusBadge(status = MatchStatus.OPEN)
}
