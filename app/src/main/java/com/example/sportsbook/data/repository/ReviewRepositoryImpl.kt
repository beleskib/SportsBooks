package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.CreateReviewRequestDto
import com.example.sportsbook.domain.model.Review
import com.example.sportsbook.domain.repository.ReviewRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ReviewRepository {

    override suspend fun getReviewsForVenue(venueId: Long): Result<List<Review>> = runCatching {
        apiService.getVenueReviews(venueId).data.map { it.toDomain() }
    }

    override suspend fun getReviewsForCoach(coachId: Long): Result<List<Review>> = runCatching {
        apiService.getCoachReviews(coachId).data.map { it.toDomain() }
    }

    override suspend fun createReview(review: Review): Result<Review> = runCatching {
        apiService.createReview(
            CreateReviewRequestDto(
                venueId = review.venueId,
                coachId = review.coachId,
                bookingId = review.bookingId,
                rating = review.rating,
                comment = review.comment
            )
        ).data.toDomain()
    }

    override suspend fun getMyReviews(): Result<List<Review>> = runCatching {
        apiService.getMyReviews().data.map { it.toDomain() }
    }
}
