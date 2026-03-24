package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.enums.ExperienceDuration
import com.example.sportsbook.domain.enums.PartnerType
import com.example.sportsbook.domain.enums.SkillLevel
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.enums.UserRole
import com.example.sportsbook.domain.model.PublicPlayerProfile
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.model.UserSportExpertise

interface UserRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(user: User): Result<User>
    suspend fun setRole(role: UserRole, partnerType: PartnerType? = null): Result<User>
    suspend fun registerUser(firebaseUid: String, email: String, displayName: String?): Result<User>
    suspend fun updateInterestedSports(sportTypes: List<SportType>): Result<List<SportType>>
    suspend fun completeOnboarding(
        displayName: String?,
        photoUrl: String?,
        dateOfBirth: String?,
        bio: String?,
        interestedSports: List<SportType>,
        expertise: List<Triple<SportType, SkillLevel, ExperienceDuration>>
    ): Result<User>
    suspend fun getSportExpertise(): Result<List<UserSportExpertise>>
    suspend fun setSportExpertise(
        expertise: List<Triple<SportType, SkillLevel, ExperienceDuration>>
    ): Result<List<UserSportExpertise>>
    suspend fun getPublicProfile(userId: Long): Result<PublicPlayerProfile>
    suspend fun getFollowCounts(): Result<Pair<Int, Int>>
}
