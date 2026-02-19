package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.CoachCertification
import com.example.sportsbook.domain.model.CoachImage
import kotlinx.serialization.Serializable

@Serializable
data class CoachDto(
    val id: Long,
    val userId: Long,
    val name: String,
    val bio: String? = null,
    val sportType: SportType,
    val specialization: String? = null,
    val experienceYears: Int = 0,
    val pricePerHour: Double,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val avgRating: Double = 0.0,
    val totalReviews: Int = 0,
    val isActive: Boolean = true,
    val images: List<CoachImageDto> = emptyList(),
    val certifications: List<CoachCertificationDto> = emptyList(),
    val activeDiscount: DiscountDto? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Coach = Coach(
        id = id, userId = userId, name = name, bio = bio,
        sportType = sportType, specialization = specialization,
        experienceYears = experienceYears, pricePerHour = pricePerHour,
        address = address, city = city, country = country,
        latitude = latitude, longitude = longitude,
        phoneNumber = phoneNumber, email = email,
        avgRating = avgRating, totalReviews = totalReviews,
        isActive = isActive,
        images = images.map { it.toDomain() },
        certifications = certifications.map { it.toDomain() },
        activeDiscount = activeDiscount?.toDomain(),
        createdAt = createdAt, updatedAt = updatedAt
    )
}

@Serializable
data class CoachImageDto(
    val id: Long,
    val coachId: Long,
    val imageUrl: String,
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0
) {
    fun toDomain(): CoachImage = CoachImage(
        id = id, coachId = coachId, imageUrl = imageUrl,
        isPrimary = isPrimary, displayOrder = displayOrder
    )
}

@Serializable
data class CoachCertificationDto(
    val id: Long,
    val coachId: Long,
    val name: String,
    val issuingBody: String? = null,
    val yearObtained: Int? = null,
    val certificateUrl: String? = null
) {
    fun toDomain(): CoachCertification = CoachCertification(
        id = id, coachId = coachId, name = name,
        issuingBody = issuingBody, yearObtained = yearObtained,
        certificateUrl = certificateUrl
    )
}
