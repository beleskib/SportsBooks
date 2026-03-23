package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.FavoriteEntityType
import com.example.sportsbook.domain.model.Favorite
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteDto(
    val id: Long = 0,
    @SerialName("userId") val userId: Long = 0,
    @SerialName("entityType") val entityType: String = "venue",
    @SerialName("entityId") val entityId: Long = 0,
    @SerialName("createdAt") val createdAt: String? = null
) {
    fun toDomain() = Favorite(
        id = id,
        userId = userId,
        entityType = FavoriteEntityType.fromValue(entityType),
        entityId = entityId,
        createdAt = createdAt
    )
}

@Serializable
data class ToggleFavoriteRequestDto(
    @SerialName("entityType") val entityType: String,
    @SerialName("entityId") val entityId: Long
)

@Serializable
data class ToggleFavoriteResponseDto(
    val favorited: Boolean = false
)

@Serializable
data class CheckFavoritesRequestDto(
    @SerialName("entityType") val entityType: String,
    @SerialName("entityIds") val entityIds: List<Long>
)

@Serializable
data class CheckFavoritesResponseDto(
    val favorited: Map<String, Boolean> = emptyMap()
)
