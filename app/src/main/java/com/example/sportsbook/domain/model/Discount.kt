package com.example.sportsbook.domain.model

data class Discount(
    val id: Long = 0,
    val venueId: Long? = null,
    val coachId: Long? = null,
    val title: String = "",
    val description: String? = null,
    val discountPercent: Double? = null,
    val discountAmount: Double? = null,
    val validFrom: String? = null,
    val validUntil: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val isPercentage: Boolean
        get() = discountPercent != null

    val displayValue: String
        get() = when {
            discountPercent != null -> "${discountPercent.toInt()}% OFF"
            discountAmount != null -> "$${discountAmount} OFF"
            else -> ""
        }
}
