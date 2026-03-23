package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.CheckFavoritesRequestDto
import com.example.sportsbook.data.remote.dto.ToggleFavoriteRequestDto
import com.example.sportsbook.domain.model.Favorite
import com.example.sportsbook.domain.repository.FavoriteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : FavoriteRepository {

    override suspend fun getMyFavorites(entityType: String?): Result<List<Favorite>> = runCatching {
        apiService.getMyFavorites(entityType).data.map { it.toDomain() }
    }

    override suspend fun toggleFavorite(entityType: String, entityId: Long): Result<Boolean> = runCatching {
        apiService.toggleFavorite(ToggleFavoriteRequestDto(entityType, entityId)).data.favorited
    }

    override suspend fun checkFavorites(entityType: String, entityIds: List<Long>): Result<Map<Long, Boolean>> = runCatching {
        val response = apiService.checkFavorites(CheckFavoritesRequestDto(entityType, entityIds)).data
        response.favorited.mapKeys { it.key.toLong() }
    }
}
