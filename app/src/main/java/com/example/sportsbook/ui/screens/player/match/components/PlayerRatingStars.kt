package com.example.sportsbook.ui.screens.player.match.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsbook.ui.theme.DarkTextPrimary

@Composable
fun PlayerRatingStars(
    rating: Double,
    maxStars: Int = 5,
    starSize: Dp = 20.dp,
    color: Color = Color(0xFFFFC107),
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..maxStars) {
            val icon = when {
                rating >= i -> Icons.Default.Star
                rating >= i - 0.5 -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Default.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(starSize),
                tint = color,
            )
        }
    }
}

@Composable
fun InteractiveRatingStars(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    label: String,
    maxStars: Int = 5,
    starSize: Dp = 32.dp,
    color: Color = Color(0xFFFFC107),
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = DarkTextPrimary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (i in 1..maxStars) {
                Box(
                    modifier = Modifier
                        .size(starSize)
                        .clickable { onRatingChanged(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "$i stars",
                        modifier = Modifier.size(starSize),
                        tint = color,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PlayerRatingStarsPreview() {
    PlayerRatingStars(rating = 3.5)
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun InteractiveRatingStarsPreview() {
    InteractiveRatingStars(
        rating = 4,
        onRatingChanged = {},
        label = "Skill",
    )
}
