package com.example.sportsbook.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddImageRequestDto(
    val imageUrl: String,
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0
)

@Serializable
data class ImageActionResponseDto(
    val id: Long? = null,
    val imageId: Long? = null,
    val message: String? = null
)
