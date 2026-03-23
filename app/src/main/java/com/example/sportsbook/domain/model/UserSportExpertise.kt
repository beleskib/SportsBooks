package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.ExperienceDuration
import com.example.sportsbook.domain.enums.SkillLevel
import com.example.sportsbook.domain.enums.SportType

data class UserSportExpertise(
    val id: Long = 0,
    val userId: Long = 0,
    val sportType: SportType,
    val skillLevel: SkillLevel,
    val experienceDuration: ExperienceDuration,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
