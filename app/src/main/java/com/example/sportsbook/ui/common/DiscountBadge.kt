package com.example.sportsbook.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsbook.ui.theme.SportsBookTheme

@Composable
fun DiscountBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFE53935))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Preview
@Composable
private fun DiscountBadgePreview() {
    SportsBookTheme { DiscountBadge(text = "20% OFF") }
}
