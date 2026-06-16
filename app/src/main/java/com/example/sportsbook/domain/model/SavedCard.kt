package com.example.sportsbook.domain.model

data class SavedCard(
    val id: String = "",
    val brand: String = "unknown",
    val last4: String = "????",
    val expMonth: Int = 0,
    val expYear: Int = 0,
    val isDefault: Boolean = false
)

data class SetupIntentResult(
    val setupIntentId: String = "",
    val clientSecret: String = "",
    val customerId: String = "",
    val ephemeralKey: String = ""
)
