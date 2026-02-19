package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.Review

interface ReviewRepository {
    suspend fun getReviewsForVenue(venueId: Long): Result<List<Review>>
    suspend fun getReviewsForCoach(coachId: Long): Result<List<Review>>
    suspend fun createReview(review: Review): Result<Review>
    suspend fun getMyReviews(): Result<List<Review>>
}
