package com.example.sportsbook.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ImageCarousel(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    contentDescription: String = "Image"
) {
    if (imageUrls.isEmpty()) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Images",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    Box(modifier = modifier) {
        HorizontalPager(state = pagerState) { page ->
            AsyncImage(
                model = imageUrls[page],
                contentDescription = "$contentDescription ${page + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (imageUrls.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(imageUrls.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageCarouselEmptyPreview() {
    MaterialTheme {
        ImageCarousel(
            imageUrls = emptyList(),
            modifier = Modifier.size(300.dp, 200.dp)
        )
    }
}
