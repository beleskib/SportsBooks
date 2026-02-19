package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Sport
import kotlinx.serialization.Serializable

@Serializable
data class SportCategoryDto(
    val id: Long,
    val name: String,
    val sportType: SportType,
    val iconUrl: String? = null,
    val description: String? = null,
    val isActive: Boolean = true,
    val displayOrder: Int = 0
) {
    fun toDomain(): Sport = Sport(
        id = id,
        name = name,
        sportType = sportType,
        iconUrl = iconUrl,
        description = description,
        isActive = isActive,
        displayOrder = displayOrder
    )
}
