package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.model.Achievement
import com.example.sportsbook.domain.model.PlayerAchievement
import com.example.sportsbook.domain.model.PlayerLevel
import com.example.sportsbook.domain.model.PlayerStats
import com.example.sportsbook.domain.model.XpTransaction
import com.example.sportsbook.domain.repository.CheckAchievementsResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerLevelDto(
    val id: Long = 0,
    @SerialName("userId") val userId: Long = 0,
    @SerialName("totalXp") val totalXp: Int = 0,
    @SerialName("currentLevel") val currentLevel: Int = 1,
    @SerialName("xpToNextLevel") val xpToNextLevel: Int = 100,
    @SerialName("createdAt") val createdAt: String = "",
    @SerialName("updatedAt") val updatedAt: String = ""
) {
    fun toDomain(): PlayerLevel = PlayerLevel(
        id = id,
        userId = userId,
        totalXp = totalXp,
        currentLevel = currentLevel,
        xpToNextLevel = xpToNextLevel,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Serializable
data class XpTransactionDto(
    val id: Long = 0,
    @SerialName("userId") val userId: Long = 0,
    val amount: Int = 0,
    @SerialName("sourceType") val sourceType: String = "",
    @SerialName("sourceId") val sourceId: Long? = null,
    val description: String? = null,
    @SerialName("createdAt") val createdAt: String = ""
) {
    fun toDomain(): XpTransaction = XpTransaction(
        id = id,
        userId = userId,
        amount = amount,
        sourceType = sourceType,
        sourceId = sourceId,
        description = description,
        createdAt = createdAt
    )
}

@Serializable
data class AchievementDto(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val category: String = "",
    @SerialName("xpReward") val xpReward: Int = 0,
    @SerialName("criteriaType") val criteriaType: String = "",
    @SerialName("criteriaValue") val criteriaValue: Int = 0,
    @SerialName("isActive") val isActive: Boolean = true,
    @SerialName("createdAt") val createdAt: String = "",
    @SerialName("updatedAt") val updatedAt: String = ""
) {
    fun toDomain(): Achievement = Achievement(
        id = id,
        name = name,
        description = description,
        icon = icon,
        category = category,
        xpReward = xpReward,
        criteriaType = criteriaType,
        criteriaValue = criteriaValue,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Serializable
data class PlayerAchievementDto(
    val id: Long = 0,
    @SerialName("userId") val userId: Long = 0,
    @SerialName("achievementId") val achievementId: Long = 0,
    @SerialName("earnedAt") val earnedAt: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val category: String = "",
    @SerialName("xpReward") val xpReward: Int = 0
) {
    fun toDomain(): PlayerAchievement = PlayerAchievement(
        id = id,
        userId = userId,
        achievementId = achievementId,
        earnedAt = earnedAt,
        name = name,
        description = description,
        icon = icon,
        category = category,
        xpReward = xpReward
    )
}

@Serializable
data class PlayerStatsDto(
    @SerialName("totalBookings") val totalBookings: Int = 0,
    @SerialName("completedBookings") val completedBookings: Int = 0,
    @SerialName("totalMatches") val totalMatches: Int = 0,
    @SerialName("completedMatches") val completedMatches: Int = 0,
    @SerialName("totalReviews") val totalReviews: Int = 0,
    @SerialName("totalFriends") val totalFriends: Int = 0,
    @SerialName("uniqueSportsBooked") val uniqueSportsBooked: Int = 0,
    @SerialName("hasEarlyBirdBooking") val hasEarlyBirdBooking: Boolean = false,
    @SerialName("hasNightOwlBooking") val hasNightOwlBooking: Boolean = false,
    @SerialName("totalHoursPlayed") val totalHoursPlayed: Int = 0,
    @SerialName("memberSinceDays") val memberSinceDays: Int = 0,
    @SerialName("favoriteSport") val favoriteSport: String? = null
) {
    fun toDomain(): PlayerStats = PlayerStats(
        totalBookings = totalBookings,
        completedBookings = completedBookings,
        totalMatches = totalMatches,
        completedMatches = completedMatches,
        totalReviews = totalReviews,
        totalFriends = totalFriends,
        uniqueSportsBooked = uniqueSportsBooked,
        hasEarlyBirdBooking = hasEarlyBirdBooking,
        hasNightOwlBooking = hasNightOwlBooking,
        totalHoursPlayed = totalHoursPlayed,
        memberSinceDays = memberSinceDays,
        favoriteSport = favoriteSport
    )
}

@Serializable
data class CheckAchievementsResponseDto(
    @SerialName("newlyEarned") val newlyEarned: List<PlayerAchievementDto> = emptyList(),
    val level: PlayerLevelDto = PlayerLevelDto()
) {
    fun toDomain(): CheckAchievementsResult = CheckAchievementsResult(
        newlyEarned = newlyEarned.map { it.toDomain() },
        level = level.toDomain()
    )
}

@Serializable
data class RedeemXpRequestDto(
    @SerialName("bookingId") val bookingId: Long,
    @SerialName("xpAmount") val xpAmount: Int
)

@Serializable
data class RedeemXpResponseDto(
    @SerialName("xpSpent") val xpSpent: Int = 0,
    @SerialName("discountAmount") val discountAmount: Double = 0.0,
    @SerialName("remainingXp") val remainingXp: Int = 0,
    @SerialName("newLevel") val newLevel: Int = 1
)
