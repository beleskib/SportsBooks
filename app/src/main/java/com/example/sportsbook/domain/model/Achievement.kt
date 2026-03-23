package com.example.sportsbook.domain.model

data class Achievement(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val category: String = "",
    val xpReward: Int = 0,
    val criteriaType: String = "",
    val criteriaValue: Int = 0,
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)
