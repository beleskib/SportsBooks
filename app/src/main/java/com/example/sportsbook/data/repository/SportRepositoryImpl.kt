package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.domain.model.Sport
import com.example.sportsbook.domain.repository.SportRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SportRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : SportRepository {

    override suspend fun getSports(): Result<List<Sport>> = runCatching {
        apiService.getSports().data.map { it.toDomain() }
    }

    override suspend fun getSportById(id: Long): Result<Sport> = runCatching {
        apiService.getSportById(id).data.toDomain()
    }
}
