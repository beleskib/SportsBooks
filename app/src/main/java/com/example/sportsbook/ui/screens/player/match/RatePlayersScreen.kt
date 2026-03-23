package com.example.sportsbook.ui.screens.player.match

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sportsbook.ui.screens.player.match.components.InteractiveRatingStars
import com.example.sportsbook.ui.screens.player.match.components.ParticipantAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatePlayersScreen(
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: RatePlayersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.submitted) {
        if (uiState.submitted) onSubmitted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rate Players") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.ratings.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No players to rate")
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Rate your teammates",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(uiState.ratings) { rating ->
                            RatingCard(
                                rating = rating,
                                onSkillChange = { viewModel.updateRating(rating.userId, "skill", it) },
                                onSportsmanshipChange = { viewModel.updateRating(rating.userId, "sportsmanship", it) },
                                onPunctualityChange = { viewModel.updateRating(rating.userId, "punctuality", it) },
                                onCommentChange = { viewModel.updateComment(rating.userId, it) }
                            )
                        }
                    }

                    // Error
                    if (uiState.error != null) {
                        Text(
                            uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Submit
                    Button(
                        onClick = viewModel::submitRatings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        enabled = uiState.allRatingsValid && !uiState.isSubmitting
                    ) {
                        Text(if (uiState.isSubmitting) "Submitting..." else "Submit Ratings")
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingCard(
    rating: PlayerRatingInput,
    onSkillChange: (Int) -> Unit,
    onSportsmanshipChange: (Int) -> Unit,
    onPunctualityChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ParticipantAvatar(
                name = rating.userName,
                photoUrl = rating.userPhotoUrl,
                size = 56.dp
            )

            Spacer(modifier = Modifier.height(4.dp))

            InteractiveRatingStars(
                rating = rating.skillRating,
                onRatingChanged = onSkillChange,
                label = "Skill"
            )

            InteractiveRatingStars(
                rating = rating.sportsmanshipRating,
                onRatingChanged = onSportsmanshipChange,
                label = "Sportsmanship"
            )

            InteractiveRatingStars(
                rating = rating.punctualityRating,
                onRatingChanged = onPunctualityChange,
                label = "Punctuality"
            )

            OutlinedTextField(
                value = rating.comment,
                onValueChange = onCommentChange,
                label = { Text("Comment (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RatePlayersScreenPreview() {
    RatePlayersScreen(onBack = {}, onSubmitted = {})
}
