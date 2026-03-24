package com.example.sportsbook.ui.screens.player.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Sport
import com.example.sportsbook.ui.theme.Navy600
import com.example.sportsbook.ui.theme.Navy700
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite
import com.example.sportsbook.ui.theme.SportBasketball
import com.example.sportsbook.ui.theme.SportFootball
import com.example.sportsbook.ui.theme.SportTennis
import com.example.sportsbook.ui.theme.SportPaddle
import com.example.sportsbook.ui.theme.SportVolleyball
import com.example.sportsbook.ui.theme.SportSwimming
import com.example.sportsbook.ui.theme.SportBoxing
import com.example.sportsbook.ui.theme.SportMMA
import com.example.sportsbook.ui.theme.SportYoga
import com.example.sportsbook.ui.theme.SportPilates
import com.example.sportsbook.ui.theme.SportCrossfit
import com.example.sportsbook.ui.theme.SportRunning
import com.example.sportsbook.ui.theme.SportCycling
import com.example.sportsbook.ui.theme.SportGolf
import com.example.sportsbook.ui.theme.SportBadminton
import com.example.sportsbook.ui.theme.SportTableTennis
import com.example.sportsbook.ui.theme.SportHandball
import com.example.sportsbook.ui.theme.SportBaseball
import com.example.sportsbook.ui.theme.SportCricket

private data class SportVisualData(val emoji: String, val color: Color, val tagline: String)

private val sportVisualsMap = mapOf(
    SportType.BASKETBALL to SportVisualData("\uD83C\uDFC0", SportBasketball, "Courts & coaching"),
    SportType.FOOTBALL to SportVisualData("\u26BD", SportFootball, "Pitches & training"),
    SportType.TENNIS to SportVisualData("\uD83C\uDFBE", SportTennis, "Courts & lessons"),
    SportType.PADDLE to SportVisualData("\uD83C\uDFD3", SportPaddle, "Book sessions"),
    SportType.VOLLEYBALL to SportVisualData("\uD83C\uDFD0", SportVolleyball, "Courts & teams"),
    SportType.SWIMMING to SportVisualData("\uD83C\uDFCA", SportSwimming, "Pools & coaching"),
    SportType.BOXING to SportVisualData("\uD83E\uDD4A", SportBoxing, "Train with pros"),
    SportType.MMA to SportVisualData("\uD83E\uDD4B", SportMMA, "Combat training"),
    SportType.YOGA to SportVisualData("\uD83E\uDDD8", SportYoga, "Studios & classes"),
    SportType.PILATES to SportVisualData("\uD83E\uDD38", SportPilates, "Book sessions"),
    SportType.CROSSFIT to SportVisualData("\uD83C\uDFCB\uFE0F", SportCrossfit, "Find boxes"),
    SportType.RUNNING to SportVisualData("\uD83C\uDFC3", SportRunning, "Groups & coaches"),
    SportType.CYCLING to SportVisualData("\uD83D\uDEB4", SportCycling, "Routes & clubs"),
    SportType.GOLF to SportVisualData("\u26F3", SportGolf, "Book tee times"),
    SportType.BADMINTON to SportVisualData("\uD83C\uDFF8", SportBadminton, "Reserve courts"),
    SportType.TABLE_TENNIS to SportVisualData("\uD83C\uDFD3", SportTableTennis, "Book tables"),
    SportType.HANDBALL to SportVisualData("\uD83E\uDD3E", SportHandball, "Courts & teams"),
    SportType.BASEBALL to SportVisualData("\u26BE", SportBaseball, "Fields & coaching"),
    SportType.CRICKET to SportVisualData("\uD83C\uDFCF", SportCricket, "Pitches & nets"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllSportsScreen(
    onBack: () -> Unit,
    onSportClick: (String) -> Unit,
    viewModel: PlayerHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = { Text("All Sports", color = WarmWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WarmWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900)
            )
        }
    ) { paddingValues ->
        AllSportsContent(
            allSports = uiState.sports,
            onSportClick = onSportClick,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun AllSportsContent(
    allSports: List<Sport>,
    onSportClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = allSports.chunked(2)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Navy900),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(rows.size) { index ->
            val pair = rows[index]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { sport ->
                    AllSportsCategoryCard(
                        sport = sport,
                        onClick = { onSportClick(sport.sportType.name.lowercase()) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AllSportsCategoryCard(
    sport: Sport,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visual = sportVisualsMap[sport.sportType]
        ?: SportVisualData("\uD83C\uDFC0", SportBasketball, "Book now")

    Card(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            visual.color.copy(alpha = 0.35f),
                            Navy700
                        )
                    )
                )
        ) {
            Text(
                text = visual.emoji,
                fontSize = 52.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp, top = 2.dp),
                color = Color.White.copy(alpha = 0.12f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = visual.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sport.sportType.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WarmWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = visual.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = visual.color.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun AllSportsScreenPreview() {
    val sampleSports = listOf(
        Sport(id = 1, sportType = SportType.BASKETBALL, name = "Basketball"),
        Sport(id = 2, sportType = SportType.FOOTBALL, name = "Football"),
        Sport(id = 3, sportType = SportType.TENNIS, name = "Tennis"),
        Sport(id = 4, sportType = SportType.PADDLE, name = "Paddle"),
        Sport(id = 5, sportType = SportType.VOLLEYBALL, name = "Volleyball"),
        Sport(id = 6, sportType = SportType.YOGA, name = "Yoga"),
        Sport(id = 7, sportType = SportType.BOXING, name = "Boxing"),
        Sport(id = 8, sportType = SportType.SWIMMING, name = "Swimming"),
    )
    SportsBookTheme {
        AllSportsContent(
            allSports = sampleSports,
            onSportClick = {}
        )
    }
}
