package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ExperienceDuration {
    @SerialName("less_than_1_year") LESS_THAN_1_YEAR,
    @SerialName("1_to_3_years") ONE_TO_3_YEARS,
    @SerialName("3_to_5_years") THREE_TO_5_YEARS,
    @SerialName("5_to_10_years") FIVE_TO_10_YEARS,
    @SerialName("10_plus_years") TEN_PLUS_YEARS;

    val displayName: String
        get() = when (this) {
            LESS_THAN_1_YEAR -> "< 1 year"
            ONE_TO_3_YEARS -> "1–3 years"
            THREE_TO_5_YEARS -> "3–5 years"
            FIVE_TO_10_YEARS -> "5–10 years"
            TEN_PLUS_YEARS -> "10+ years"
        }
}
