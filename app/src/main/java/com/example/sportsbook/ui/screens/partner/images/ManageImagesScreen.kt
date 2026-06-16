package com.example.sportsbook.ui.screens.partner.images

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent

@Composable
fun ManageImagesScreen(
    entityType: String,
    entityId: Long,
    onBack: () -> Unit,
    viewModel: ManageImagesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(entityType, entityId) {
        viewModel.initialize(entityType, entityId)
    }

    LaunchedEffect(uiState.actionError) {
        uiState.actionError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearActionError()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Manage Images", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                    if (uiState.entityName.isNotBlank()) {
                        Text(uiState.entityName, fontSize = 13.sp, color = DarkTextSecondary)
                    }
                }
                // + Add button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(GreenAccent)
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            // Upload progress
            if (uiState.isUploading) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(DarkSurface)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(GreenAccent))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Uploading image...", fontSize = 12.sp, color = DarkTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenAccent)
                    }
                }
                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error!!,
                        onRetry = viewModel::loadImages,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                uiState.images.isEmpty() && !uiState.isUploading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
                            Text("🖼️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No images yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            Text("Tap + Add to upload your first photo", fontSize = 14.sp, color = DarkTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    ) {
                        items(uiState.images, key = { it.id }) { image ->
                            ImageCard(
                                image = image,
                                onSetPrimary = { viewModel.setPrimaryImage(image.id) },
                                onDelete = { viewModel.deleteImage(image.id) },
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ImageCard(
    image: DisplayImage,
    onSetPrimary: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface),
    ) {
        AsyncImage(
            model = image.imageUrl,
            contentDescription = "Listing image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(150.dp),
        )

        if (image.isPrimary) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(GreenAccent)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
            ) {
                Text("Primary", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!image.isPrimary) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, GreenAccent.copy(alpha = 0.5f), CircleShape)
                        .clickable(onClick = onSetPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Star, "Set as primary", modifier = Modifier.size(16.dp), tint = GreenAccent)
                }
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f), CircleShape)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Delete, "Delete image", modifier = Modifier.size(16.dp), tint = Color(0xFFEF5350))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun ManageImagesScreenPreview() {
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Manage Images", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                    Text("City Tennis Center", fontSize = 13.sp, color = DarkTextSecondary)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(GreenAccent).padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text("+ Add", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
            // Empty state preview
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
                    Text("🖼️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No images yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                    Text("Tap + Add to upload your first photo", fontSize = 14.sp, color = DarkTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
}
