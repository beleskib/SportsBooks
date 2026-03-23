package com.example.sportsbook.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateVenueRequestDto(
    val name: String,
    val description: String? = null,
    val sportType: String,
    val pricePerHour: Double,
    val address: String,
    val city: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phoneNumber: String? = null,
    val email: String? = null
)
