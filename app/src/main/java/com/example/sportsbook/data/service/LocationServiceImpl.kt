package com.example.sportsbook.data.service

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.example.sportsbook.domain.service.LocationService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationServiceImpl @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationService {

    @SuppressLint("MissingPermission")
    override fun getLastKnownLocation(): Flow<Location?> = callbackFlow {
        try {
            val location = fusedLocationClient.lastLocation.await()
            trySend(location)
        } catch (e: SecurityException) {
            trySend(null)
        }
        awaitClose()
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        return try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
        } catch (e: Exception) {
            null
        }
    }
}
