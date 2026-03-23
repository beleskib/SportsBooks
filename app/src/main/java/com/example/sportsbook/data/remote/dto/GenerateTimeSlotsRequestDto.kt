package com.example.sportsbook.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerateTimeSlotsRequestDto(
    val venueId: Long? = null,
    val coachId: Long? = null,
    val dateFrom: String,
    val dateTo: String,
    val startHour: Int? = null,
    val endHour: Int? = null,
    val daysOfWeek: List<Int>? = null
)
