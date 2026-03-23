package com.example.sportsbook.data.repository

import android.net.Uri
import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.AddImageRequestDto
import com.example.sportsbook.domain.model.CoachImage
import com.example.sportsbook.domain.model.VenueImage
import com.example.sportsbook.domain.repository.ImageRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val apiService: ApiService
) : ImageRepository {

    override suspend fun uploadAndGetUrl(uri: Uri, storagePath: String): Result<String> =
        runCatching {
            val storageRef = firebaseStorage.reference.child(storagePath)
            storageRef.putFile(uri).await()
            storageRef.downloadUrl.await().toString()
        }

    // Venue images

    override suspend fun addVenueImage(
        venueId: Long,
        imageUrl: String,
        isPrimary: Boolean,
        displayOrder: Int
    ): Result<VenueImage> = runCatching {
        val request = AddImageRequestDto(imageUrl, isPrimary, displayOrder)
        apiService.addVenueImage(venueId, request).data.toDomain()
    }

    override suspend fun deleteVenueImage(venueId: Long, imageId: Long): Result<Unit> =
        runCatching {
            apiService.deleteVenueImage(venueId, imageId)
            Unit
        }

    override suspend fun setVenuePrimaryImage(venueId: Long, imageId: Long): Result<Unit> =
        runCatching {
            apiService.setVenuePrimaryImage(venueId, imageId)
            Unit
        }

    // Coach images

    override suspend fun addCoachImage(
        coachId: Long,
        imageUrl: String,
        isPrimary: Boolean,
        displayOrder: Int
    ): Result<CoachImage> = runCatching {
        val request = AddImageRequestDto(imageUrl, isPrimary, displayOrder)
        apiService.addCoachImage(coachId, request).data.toDomain()
    }

    override suspend fun deleteCoachImage(coachId: Long, imageId: Long): Result<Unit> =
        runCatching {
            apiService.deleteCoachImage(coachId, imageId)
            Unit
        }

    override suspend fun setCoachPrimaryImage(coachId: Long, imageId: Long): Result<Unit> =
        runCatching {
            apiService.setCoachPrimaryImage(coachId, imageId)
            Unit
        }
}
