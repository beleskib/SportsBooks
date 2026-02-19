package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.SportType

data class Venue(
    val id: Long = 0,
    val ownerId: Long = 0,
    val name: String = "",
    val description: String? = null,
    val sportType: SportType = SportType.BASKETBALL,
    val pricePerHour: Double = 0.0,
    val address: String = "",
    val city: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val avgRating: Double = 0.0,
    val totalReviews: Int = 0,
    val isActive: Boolean = true,
    val images: List<VenueImage> = emptyList(),
    val equipment: List<VenueEquipment> = emptyList(),
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

data class VenueImage(
    val id: Long = 0,
    val venueId: Long = 0,
    val imageUrl: String = "",
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0
)

data class VenueEquipment(
    val id: Long = 0,
    val venueId: Long = 0,
    val name: String = "",
    val description: String? = null,
    val isIncluded: Boolean = true
)
