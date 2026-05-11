package com.example.sportsbook.domain.service

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationService {
    fun getLastKnownLocation(): Flow<Location?>
    suspend fun getCurrentLocation(): Location?
    /**
     * Reverse-geocode coordinates to a city name (e.g. "Ohrid", "Skopje").
     * Returns null if geocoding fails or no city is found.
     */
    suspend fun getCityName(latitude: Double, longitude: Double): String?
}
