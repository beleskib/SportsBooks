package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.model.Discount
import kotlinx.serialization.Serializable

@Serializable
data class DiscountDto(
    val id: Long,
    val venueId: Long? = null,
    val coachId: Long? = null,
    val title: String,
    val description: String? = null,
    val discountPercent: Double? = null,
    val discountAmount: Double? = null,
    val validFrom: String? = null,
    val validUntil: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Discount = Discount(
        id = id, venueId = venueId, coachId = coachId,
        title = title, description = description,
        discountPercent = discountPercent, discountAmount = discountAmount,
        validFrom = validFrom, validUntil = validUntil,
        isActive = isActive, createdAt = createdAt, updatedAt = updatedAt
    )
}
