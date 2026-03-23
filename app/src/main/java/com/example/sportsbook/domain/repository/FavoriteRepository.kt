package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.Favorite

interface FavoriteRepository {
    suspend fun getMyFavorites(entityType: String? = null): Result<List<Favorite>>
    suspend fun toggleFavorite(entityType: String, entityId: Long): Result<Boolean>
    suspend fun checkFavorites(entityType: String, entityIds: List<Long>): Result<Map<Long, Boolean>>
}
