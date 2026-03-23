package com.example.sportsbook.domain.repository

import android.net.Uri
import com.example.sportsbook.domain.model.CoachImage
import com.example.sportsbook.domain.model.VenueImage

interface ImageRepository {

    /**
     * Upload image file to Firebase Storage and return the download URL.
     */
    suspend fun uploadAndGetUrl(uri: Uri, storagePath: String): Result<String>

    // Venue image management via backend API
    suspend fun addVenueImage(
        venueId: Long,
        imageUrl: String,
        isPrimary: Boolean,
        displayOrder: Int
    ): Result<VenueImage>

    suspend fun deleteVenueImage(venueId: Long, imageId: Long): Result<Unit>

    suspend fun setVenuePrimaryImage(venueId: Long, imageId: Long): Result<Unit>

    // Coach image management via backend API
    suspend fun addCoachImage(
        coachId: Long,
        imageUrl: String,
        isPrimary: Boolean,
        displayOrder: Int
    ): Result<CoachImage>

    suspend fun deleteCoachImage(coachId: Long, imageId: Long): Result<Unit>

    suspend fun setCoachPrimaryImage(coachId: Long, imageId: Long): Result<Unit>
}
