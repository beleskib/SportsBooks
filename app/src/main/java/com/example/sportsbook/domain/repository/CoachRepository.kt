package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Coach

interface CoachRepository {
    suspend fun getAllCoaches(): Result<List<Coach>>
    suspend fun getCoachesBySport(sportType: SportType): Result<List<Coach>>
    suspend fun getCoachById(id: Long): Result<Coach>
    suspend fun getTopDeals(): Result<List<Coach>>
    suspend fun searchCoaches(query: String): Result<List<Coach>>
    suspend fun createCoach(coach: Coach): Result<Coach>
    suspend fun updateCoach(coach: Coach): Result<Coach>
    suspend fun getMyCoachProfile(): Result<Coach?>
}
