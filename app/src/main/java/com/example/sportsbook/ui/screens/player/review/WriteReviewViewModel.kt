package com.example.sportsbook.ui.screens.player.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.repository.ReviewRepository
import com.example.sportsbook.domain.model.Review
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WriteReviewUiState(
    val bookingId: Long = 0,
    val rating: Int = 0,
    val comment: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class WriteReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val bookingRepository: BookingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: Long = checkNotNull(savedStateHandle["bookingId"])

    private val _uiState = MutableStateFlow(WriteReviewUiState(bookingId = bookingId))
    val uiState: StateFlow<WriteReviewUiState> = _uiState.asStateFlow()

    fun onRatingChange(rating: Int) {
        _uiState.update { it.copy(rating = rating) }
    }

    fun onCommentChange(comment: String) {
        _uiState.update { it.copy(comment = comment) }
    }

    fun submitReview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            bookingRepository.getBookingById(bookingId)
                .onSuccess { booking ->
                    val review = Review(
                        id = 0,
                        playerId = 0,
                        venueId = booking.venueId,
                        coachId = booking.coachId,
                        bookingId = bookingId,
                        rating = _uiState.value.rating,
                        comment = _uiState.value.comment.takeIf { it.isNotBlank() }
                    )
                    reviewRepository.createReview(review)
                        .onSuccess {
                            _uiState.update { it.copy(isSuccess = true, isLoading = false) }
                        }
                        .onFailure { e ->
                            _uiState.update { it.copy(error = e.message, isLoading = false) }
                        }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}
