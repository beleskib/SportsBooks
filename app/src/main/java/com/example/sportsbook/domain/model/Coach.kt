package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.SportType

data class Coach(
    val id: Long = 0,
    val userId: Long = 0,
    val name: String = "",
    val bio: String? = null,
    val sportType: SportType = SportType.BASKETBALL,
    val specialization: String? = null,
    val experienceYears: Int = 0,
    val pricePerHour: Double = 0.0,
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
    val images: List<CoachImage> = emptyList(),
    val certifications: List<CoachCertification> = emptyList(),
    val activeDiscount: Discount? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val primaryImageUrl: String?
        get() = images.firstOrNull { it.isPrimary }?.imageUrl ?: images.firstOrNull()?.imageUrl

    val discountedPrice: Double?
        get() = activeDiscount?.let { discount ->
            when {
                discount.discountPercent != null -> pricePerHour * (1 - discount.discountPercent / 100)
                discount.discountAmount != null -> (pricePerHour - discount.discountAmount).coerceAtLeast(0.0)
                else -> null
            }
        }
}

data class CoachImage(
    val id: Long = 0,
    val coachId: Long = 0,
    val imageUrl: String = "",
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0
)

data class CoachCertification(
    val id: Long = 0,
    val coachId: Long = 0,
    val name: String = "",
    val issuingBody: String? = null,
    val yearObtained: Int? = null,
    val certificateUrl: String? = null
)
