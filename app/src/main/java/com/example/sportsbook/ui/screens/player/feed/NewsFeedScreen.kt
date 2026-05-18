package com.example.sportsbook.ui.screens.player.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.sportsbook.data.remote.dto.AddCommentRequestDto
import com.example.sportsbook.data.remote.dto.CreateFeedPostRequestDto
import com.example.sportsbook.data.remote.dto.FeedCommentDto
import com.example.sportsbook.data.remote.dto.FeedPostDto
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.ui.theme.BorderGray
import com.example.sportsbook.ui.theme.CardWhite
import com.example.sportsbook.ui.theme.DividerGray
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.LightBg
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.TextPrimary
import com.example.sportsbook.ui.theme.TextSecondary
import com.example.sportsbook.ui.theme.TextTertiary
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.sportsbook.data.remote.api.ApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val posts: List<FeedPostDto> = emptyList(),
    val isLoading: Boolean = false,
    val isPosting: Boolean = false,
    val error: String? = null,
    val currentUserName: String? = null,
    val currentUserPhotoUrl: String? = null,
    val selectedPostId: Long? = null,
    val comments: List<FeedCommentDto> = emptyList(),
    val isLoadingComments: Boolean = false,
    val isPostingComment: Boolean = false,
    val showComments: Boolean = false
)

@HiltViewModel
class NewsFeedViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null

    init {
        loadCurrentUser()
        loadFeed()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.update {
                it.copy(
                    currentUserName = user?.displayName,
                    currentUserPhotoUrl = user?.photoUrl
                )
            }
        }
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getFeed()
                _uiState.update { it.copy(posts = response.data, isLoading = false) }
            } catch (e: Exception) {
                android.util.Log.e("NewsFeed", "loadFeed failed", e)
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun createPost(content: String, postType: String = "text", metadata: Map<String, String>? = null) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPosting = true) }
            try {
                val request = CreateFeedPostRequestDto(
                    postType = postType,
                    content = content,
                    metadata = metadata
                )
                apiService.createPost(request)
                // Reload feed inline (don't call loadFeed() which launches a separate coroutine)
                val response = apiService.getFeed()
                _uiState.update { it.copy(posts = response.data, isPosting = false) }
            } catch (e: Exception) {
                android.util.Log.e("NewsFeed", "createPost failed", e)
                _uiState.update { it.copy(isPosting = false, error = e.message) }
            }
        }
    }

    fun toggleLike(postId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.toggleLike(postId)
                val updatedPost = response.data
                _uiState.update { state ->
                    state.copy(
                        posts = state.posts.map {
                            if (it.id == postId) updatedPost else it
                        }
                    )
                }
            } catch (_: Exception) { }
        }
    }

    fun openComments(postId: Long) {
        _uiState.update { it.copy(selectedPostId = postId, showComments = true) }
        loadComments(postId)
    }

    fun closeComments() {
        _uiState.update { it.copy(showComments = false, selectedPostId = null, comments = emptyList()) }
    }

    private fun loadComments(postId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingComments = true) }
            try {
                val response = apiService.getPostComments(postId)
                _uiState.update { it.copy(comments = response.data, isLoadingComments = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingComments = false) }
            }
        }
    }

    fun postComment(content: String) {
        val postId = _uiState.value.selectedPostId ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPostingComment = true) }
            try {
                apiService.addComment(postId, AddCommentRequestDto(content = content))
                loadComments(postId)
                _uiState.update { state ->
                    state.copy(
                        isPostingComment = false,
                        posts = state.posts.map {
                            if (it.id == postId) it.copy(commentsCount = it.commentsCount + 1) else it
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isPostingComment = false) }
            }
        }
    }

    fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(30_000)
                try {
                    val response = apiService.getFeed()
                    _uiState.update { it.copy(posts = response.data) }
                } catch (_: Exception) { }
            }
        }
    }

    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    onUserClick: (Long) -> Unit = {},
    viewModel: NewsFeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var composerText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        viewModel.startAutoRefresh()
        onDispose { viewModel.stopAutoRefresh() }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = LightBg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Feed",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavBarBg)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading && uiState.posts.isEmpty(),
            onRefresh = { viewModel.loadFeed() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Inline post composer
                item {
                    FeedComposer(
                        text = composerText,
                        onTextChange = { composerText = it },
                        userPhotoUrl = uiState.currentUserPhotoUrl,
                        isPosting = uiState.isPosting,
                        onPost = {
                            viewModel.createPost(composerText)
                            composerText = ""
                        },
                        onSuggestionClick = { suggestion ->
                            composerText = suggestion
                        }
                    )
                }

                if (uiState.posts.isEmpty() && !uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SportsScore,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = TextTertiary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No posts yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Follow players and partners to see their updates",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.posts, key = { it.id }) { post ->
                        FeedPostCard(
                            post = post,
                            onLike = { viewModel.toggleLike(post.id) },
                            onComment = { viewModel.openComments(post.id) },
                            onUserClick = { onUserClick(post.userId) }
                        )
                    }
                }
            }
        }

        if (uiState.showComments) {
            CommentsBottomSheet(
                comments = uiState.comments,
                isLoading = uiState.isLoadingComments,
                isPosting = uiState.isPostingComment,
                onDismiss = { viewModel.closeComments() },
                onPostComment = { viewModel.postComment(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsBottomSheet(
    comments: List<FeedCommentDto>,
    isLoading: Boolean,
    isPosting: Boolean,
    onDismiss: () -> Unit,
    onPostComment: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Comments",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GoldAccent)
                        }
                    }
                } else if (comments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No comments yet. Be the first!",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(comments, key = { it.id }) { comment ->
                        CommentItem(comment = comment)
                    }
                }
            }

            HorizontalDivider(color = DividerGray)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Write a comment...", color = TextTertiary) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LightBg,
                        unfocusedContainerColor = LightBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = GoldAccent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        onPostComment(commentText)
                        commentText = ""
                    },
                    enabled = commentText.isNotBlank() && !isPosting
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = GoldAccent,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (commentText.isNotBlank()) GoldAccent else TextTertiary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CommentItem(comment: FeedCommentDto) {
    Row(modifier = Modifier.fillMaxWidth()) {
        if (comment.userPhotoUrl != null) {
            AsyncImage(
                model = comment.userPhotoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BorderGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = GoldAccent
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.userName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatTimeAgo(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun FeedComposer(
    text: String,
    onTextChange: (String) -> Unit,
    userPhotoUrl: String?,
    isPosting: Boolean,
    onPost: () -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Profile picture
            if (userPhotoUrl != null) {
                AsyncImage(
                    model = userPhotoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BorderGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = GoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text field
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        "What's on your mind?",
                        color = TextTertiary
                    )
                },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = LightBg,
                    unfocusedContainerColor = LightBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = GoldAccent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 3,
                trailingIcon = {
                    if (text.isNotBlank()) {
                        IconButton(
                            onClick = onPost,
                            enabled = !isPosting
                        ) {
                            if (isPosting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = GoldAccent,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Post",
                                    tint = GoldAccent
                                )
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Suggestion chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionChip(
                label = "Share a milestone",
                icon = Icons.Default.EmojiEvents,
                color = GoldAccent,
                onClick = { onSuggestionClick("Just hit a new milestone! ") }
            )
            SuggestionChip(
                label = "Share your XP",
                icon = Icons.Default.Star,
                color = Color(0xFF4CAF50),
                onClick = { onSuggestionClick("Leveling up! Check out my XP ") }
            )
            SuggestionChip(
                label = "Match update",
                icon = Icons.Default.SportsScore,
                color = Color(0xFFFF9800),
                onClick = { onSuggestionClick("Great match today! ") }
            )
            SuggestionChip(
                label = "Achievement unlocked",
                icon = Icons.Default.WorkspacePremium,
                color = Color(0xFF2196F3),
                onClick = { onSuggestionClick("Just unlocked a new achievement! ") }
            )
        }
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary
            )
        },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = LightBg
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = color.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun FeedPostCard(
    post: FeedPostDto,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onUserClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onUserClick)
            ) {
                if (post.authorPhotoUrl != null) {
                    AsyncImage(
                        model = post.authorPhotoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BorderGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = GoldAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PostTypeBadge(postType = post.postType)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatTimeAgo(post.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            }

            // Content
            if (!post.content.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }

            // Image
            if (post.imageUrl != null) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Match share metadata card
            if (post.postType == "match_share" && post.metadata["matchTitle"] != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.SportsScore,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = post.metadata["matchTitle"] ?: "",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                        post.metadata["sport"]?.let { sport ->
                            Text(
                                text = sport,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF9800)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            post.metadata["date"]?.let { date ->
                                Text(text = date, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            post.metadata["time"]?.let { time ->
                                Text(text = time, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        post.metadata["location"]?.let { location ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = TextTertiary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = location,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        post.metadata["players"]?.let { players ->
                            Text(
                                text = "Players: $players",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Achievement/milestone metadata
            if (post.postType != "match_share") {
                val metaTitle = post.metadata["achievementName"]
                    ?: post.metadata["venueName"]
                    ?: post.metadata["matchResult"]
                if (metaTitle != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldAccent.copy(alpha = 0.08f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = GoldAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = metaTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = GoldAccent
                        )
                    }
                }
            }

            // Actions: Like & Comment count
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerGray)
            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByMe) Color.Red else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "${post.likesCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    modifier = Modifier.clickable(onClick = onComment),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comments",
                        modifier = Modifier.size(18.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.commentsCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun PostTypeBadge(postType: String) {
    val (label, color) = when (postType) {
        "achievement" -> "Achievement" to GoldAccent
        "milestone" -> "Milestone" to Color(0xFF4CAF50)
        "booking_completed" -> "Booking" to Color(0xFF2196F3)
        "match_result" -> "Match" to Color(0xFFFF9800)
        "match_share" -> "Match" to Color(0xFFFF9800)
        "photo" -> "Photo" to Color(0xFFE91E63)
        else -> "Post" to TextSecondary
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = color
    )
}

private fun formatTimeAgo(dateString: String): String {
    return try {
        val now = System.currentTimeMillis()
        val dateMillis = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
            .parse(dateString)?.time ?: return dateString
        val diff = now - dateMillis
        val minutes = diff / 60000
        val hours = minutes / 60
        val days = hours / 24
        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> "${days / 7}w ago"
        }
    } catch (_: Exception) {
        dateString
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun NewsFeedScreenPreview() {
    val samplePosts = listOf(
        FeedPostDto(
            id = 1, userId = 1, authorName = "Alex Johnson",
            postType = "achievement", content = "Just unlocked my first achievement!",
            metadata = mapOf("achievementName" to "First Booking"),
            likesCount = 12, commentsCount = 3, isLikedByMe = true,
            createdAt = "2026-03-24T10:00:00"
        ),
        FeedPostDto(
            id = 2, userId = 2, authorName = "City Sports Hall",
            postType = "text", content = "New basketball courts now open! Book your slots today.",
            likesCount = 45, commentsCount = 8,
            createdAt = "2026-03-24T08:30:00"
        ),
        FeedPostDto(
            id = 3, userId = 3, authorName = "Coach Mike",
            postType = "milestone", content = "100 training sessions completed!",
            metadata = mapOf("achievementName" to "100 Sessions Milestone"),
            likesCount = 67, commentsCount = 15,
            createdAt = "2026-03-23T18:00:00"
        ),
        FeedPostDto(
            id = 4, userId = 4, authorName = "Jordan Lee",
            postType = "match_share", content = "Come join us for Basketball at City Sports Center!",
            metadata = mapOf(
                "matchTitle" to "Sunday Pickup Basketball",
                "sport" to "Basketball",
                "date" to "2026-03-30",
                "time" to "18:00 - 19:30",
                "location" to "City Sports Center",
                "players" to "4/12"
            ),
            likesCount = 8, commentsCount = 2,
            createdAt = "2026-03-24T12:00:00"
        )
    )
    var composerText by remember { mutableStateOf("") }
    SportsBookTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBg),
            contentPadding = PaddingValues(vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                FeedComposer(
                    text = composerText,
                    onTextChange = { composerText = it },
                    userPhotoUrl = null,
                    isPosting = false,
                    onPost = {},
                    onSuggestionClick = { composerText = it }
                )
            }
            items(samplePosts) { post ->
                FeedPostCard(post = post, onLike = {}, onComment = {}, onUserClick = {})
            }
        }
    }
}
