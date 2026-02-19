package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.SportType

data class Sport(
    val id: Long = 0,
    val name: String = "",
    val sportType: SportType = SportType.BASKETBALL,
    val iconUrl: String? = null,
    val description: String? = null,
    val isActive: Boolean = true,
    val displayOrder: Int = 0
)
