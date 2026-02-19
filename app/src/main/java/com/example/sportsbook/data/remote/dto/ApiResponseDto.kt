package com.example.sportsbook.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponseDto<T>(
    val success: Boolean,
    val data: T,
    val message: String? = null
)

@Serializable
data class PaginatedResponseDto<T>(
    val success: Boolean,
    val data: List<T>,
    val pagination: PaginationMetaDto
)

@Serializable
data class PaginationMetaDto(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)
