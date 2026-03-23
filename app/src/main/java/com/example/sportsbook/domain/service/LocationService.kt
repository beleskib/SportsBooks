package com.example.sportsbook.domain.service

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationService {
    fun getLastKnownLocation(): Flow<Location?>
    suspend fun getCurrentLocation(): Location?
}
