package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.ListingApprovalStatus
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Discount
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.model.VenueEquipment
import com.example.sportsbook.domain.model.VenueImage
import kotlinx.serialization.Serializable

@Serializable
data class VenueDto(
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
    val approvalStatus: ListingApprovalStatus = ListingApprovalStatus.APPROVED,
    val approvalDecidedAt: String? = null,
    val approvalDecidedByUserId: Long? = null,
    val approvalRejectionReason: String? = null,
    val images: List<VenueImageDto> = emptyList(),
    val equipment: List<VenueEquipmentDto> = emptyList(),
    val activeDiscount: DiscountDto? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Venue = Venue(
        id = id,
        ownerId = ownerId,
        name = name,
        description = description,
        sportType = sportType,
        pricePerHour = pricePerHour,
        address = address,
        city = city,
        country = country,
        latitude = latitude,
        longitude = longitude,
        phoneNumber = phoneNumber,
        email = email,
        avgRating = avgRating,
        totalReviews = totalReviews,
        isActive = isActive,
        approvalStatus = approvalStatus,
        approvalDecidedAt = approvalDecidedAt,
        approvalDecidedByUserId = approvalDecidedByUserId,
        approvalRejectionReason = approvalRejectionReason,
        images = images.map { it.toDomain() },
        equipment = equipment.map { it.toDomain() },
        activeDiscount = activeDiscount?.toDomain(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Serializable
data class VenueImageDto(
    val id: Long = 0,
    val venueId: Long = 0,
    val imageUrl: String = "",
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0
) {
    fun toDomain(): VenueImage = VenueImage(
        id = id, venueId = venueId, imageUrl = imageUrl,
        isPrimary = isPrimary, displayOrder = displayOrder
    )
}

@Serializable
data class VenueEquipmentDto(
    val id: Long = 0,
    val venueId: Long = 0,
    val name: String = "",
    val description: String? = null,
    val isIncluded: Boolean = true
) {
    fun toDomain(): VenueEquipment = VenueEquipment(
        id = id, venueId = venueId, name = name,
        description = description, isIncluded = isIncluded
    )
}
