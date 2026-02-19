package com.example.sportsbook.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsbook.ui.theme.SportsBookTheme

@Composable
fun PriceTag(
    price: Double,
    discountedPrice: Double? = null,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (discountedPrice != null && discountedPrice < price) {
            Text(
                text = "$${String.format("%.2f", price)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$${String.format("%.2f", discountedPrice)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = "$${String.format("%.2f", price)}/hr",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview
@Composable
private fun PriceTagPreview() {
    SportsBookTheme { PriceTag(price = 50.0, discountedPrice = 40.0) }
}
