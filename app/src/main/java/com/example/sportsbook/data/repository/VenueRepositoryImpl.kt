package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.VenueRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VenueRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : VenueRepository {

    override suspend fun getVenuesBySport(sportType: SportType): Result<List<Venue>> = runCatching {
        apiService.getVenuesBySport(sportType.name.lowercase()).data.map { it.toDomain() }
    }

    override suspend fun getVenueById(id: Long): Result<Venue> = runCatching {
        apiService.getVenueById(id).data.toDomain()
    }

    override suspend fun getTopDeals(): Result<List<Venue>> = runCatching {
        apiService.getTopDealVenues().data.map { it.toDomain() }
    }

    override suspend fun searchVenues(query: String): Result<List<Venue>> = runCatching {
        apiService.searchVenues(query).data.map { it.toDomain() }
    }

    override suspend fun createVenue(venue: Venue): Result<Venue> {
        // TODO: Implement venue creation with multipart form for images
        return Result.failure(NotImplementedError("Venue creation not yet implemented"))
    }

    override suspend fun updateVenue(venue: Venue): Result<Venue> {
        // TODO: Implement venue update
        return Result.failure(NotImplementedError("Venue update not yet implemented"))
    }

    override suspend fun getMyVenues(): Result<List<Venue>> = runCatching {
        apiService.getMyVenues().data.map { it.toDomain() }
    }
}
