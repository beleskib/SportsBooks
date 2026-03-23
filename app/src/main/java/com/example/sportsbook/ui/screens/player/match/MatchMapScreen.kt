package com.example.sportsbook.ui.screens.player.match

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.common.LoadingIndicator
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchMapScreen(
    onMatchClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: MatchMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val defaultPos = uiState.userLocation?.let { LatLng(it.latitude, it.longitude) }
        ?: uiState.matches.firstOrNull()?.let {
            if (it.latitude != null && it.longitude != null) LatLng(it.latitude, it.longitude) else null
        }
        ?: LatLng(41.9981, 21.4254)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPos, 12f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                cameraPositionState = cameraPositionState
            ) {
                uiState.matches.forEach { match ->
                    if (match.latitude != null && match.longitude != null) {
                        Marker(
                            state = MarkerState(position = LatLng(match.latitude, match.longitude)),
                            title = match.title,
                            snippet = "${match.sportType.displayName} • ${match.spotsLeft} spots left",
                            onInfoWindowClick = { onMatchClick(match.id) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchMapScreenPreview() {
    MaterialTheme {
        MatchMapScreen(onMatchClick = {}, onBack = {})
    }
}
