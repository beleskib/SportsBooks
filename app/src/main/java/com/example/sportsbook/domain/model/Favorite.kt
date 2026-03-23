package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.FavoriteEntityType

data class Favorite(
    val id: Long = 0,
    val userId: Long = 0,
    val entityType: FavoriteEntityType = FavoriteEntityType.VENUE,
    val entityId: Long = 0,
    val createdAt: String? = null
)
