package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.repository.CoachRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoachRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : CoachRepository {

    override suspend fun getCoachesBySport(sportType: SportType): Result<List<Coach>> = runCatching {
        apiService.getCoachesBySport(sportType.name.lowercase()).data.map { it.toDomain() }
    }

    override suspend fun getCoachById(id: Long): Result<Coach> = runCatching {
        apiService.getCoachById(id).data.toDomain()
    }

    override suspend fun getTopDeals(): Result<List<Coach>> = runCatching {
        apiService.getTopDealCoaches().data.map { it.toDomain() }
    }

    override suspend fun searchCoaches(query: String): Result<List<Coach>> = runCatching {
        apiService.searchCoaches(query).data.map { it.toDomain() }
    }

    override suspend fun createCoach(coach: Coach): Result<Coach> {
        // TODO: Implement coach creation
        return Result.failure(NotImplementedError("Coach creation not yet implemented"))
    }

    override suspend fun updateCoach(coach: Coach): Result<Coach> {
        // TODO: Implement coach update
        return Result.failure(NotImplementedError("Coach update not yet implemented"))
    }

    override suspend fun getMyCoachProfile(): Result<Coach?> = runCatching {
        apiService.getMyCoachProfile().data?.toDomain()
    }
}
