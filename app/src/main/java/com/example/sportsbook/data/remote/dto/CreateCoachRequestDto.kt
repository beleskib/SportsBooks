package com.example.sportsbook.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCoachRequestDto(
    val name: String,
    val bio: String? = null,
    val sportType: String,
    val specialization: String? = null,
    val experienceYears: Int? = null,
    val pricePerHour: Double,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null
)
