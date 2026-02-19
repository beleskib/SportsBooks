package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Venue

interface VenueRepository {
    suspend fun getVenuesBySport(sportType: SportType): Result<List<Venue>>
    suspend fun getVenueById(id: Long): Result<Venue>
    suspend fun getTopDeals(): Result<List<Venue>>
    suspend fun searchVenues(query: String): Result<List<Venue>>
    suspend fun createVenue(venue: Venue): Result<Venue>
    suspend fun updateVenue(venue: Venue): Result<Venue>
    suspend fun getMyVenues(): Result<List<Venue>>
}
