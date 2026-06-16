package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.model.SavedCard
import com.example.sportsbook.domain.model.SetupIntentResult
import kotlinx.serialization.Serializable

@Serializable
data class SavedCardDto(
    val id: String,
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val isDefault: Boolean = false
) {
    fun toDomain(): SavedCard = SavedCard(
        id = id,
        brand = brand,
        last4 = last4,
        expMonth = expMonth,
        expYear = expYear,
        isDefault = isDefault
    )
}

@Serializable
data class SetupIntentResultDto(
    val setupIntentId: String,
    val clientSecret: String,
    val customerId: String,
    val ephemeralKey: String
) {
    fun toDomain(): SetupIntentResult = SetupIntentResult(
        setupIntentId = setupIntentId,
        clientSecret = clientSecret,
        customerId = customerId,
        ephemeralKey = ephemeralKey
    )
}
