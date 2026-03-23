package com.example.sportsbook.domain.model

data class PlayerLevel(
    val id: Long = 0,
    val userId: Long = 0,
    val totalXp: Int = 0,
    val currentLevel: Int = 1,
    val xpToNextLevel: Int = 100,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    // XP needed for current level = currentLevel^2 * 100
    // XP progress within current level
    val xpForCurrentLevel: Int
        get() {
            val prevLevelXp = ((currentLevel - 1) * (currentLevel - 1)) * 100
            return totalXp - prevLevelXp
        }

    val xpRangeForLevel: Int
        get() {
            val prevLevelXp = ((currentLevel - 1) * (currentLevel - 1)) * 100
            val nextLevelXp = (currentLevel * currentLevel) * 100
            return nextLevelXp - prevLevelXp
        }

    val progressFraction: Float
        get() = if (xpRangeForLevel > 0) xpForCurrentLevel.toFloat() / xpRangeForLevel else 0f
}
