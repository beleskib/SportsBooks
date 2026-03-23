package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.CompleteOnboardingRequestDto
import com.example.sportsbook.data.remote.dto.CreateUserRequestDto
import com.example.sportsbook.data.remote.dto.SetRoleRequestDto
import com.example.sportsbook.data.remote.dto.SportExpertiseEntryDto
import com.example.sportsbook.data.remote.dto.UpdateInterestedSportsRequestDto
import com.example.sportsbook.data.remote.dto.UpdateUserRequestDto
import com.example.sportsbook.domain.enums.ExperienceDuration
import com.example.sportsbook.domain.enums.PartnerType
import com.example.sportsbook.domain.enums.SkillLevel
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.enums.UserRole
import com.example.sportsbook.domain.model.PublicPlayerProfile
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.model.UserSportExpertise
import com.example.sportsbook.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getProfile(): Result<User> = runCatching {
        apiService.getProfile().data.toDomain()
    }

    override suspend fun updateProfile(user: User): Result<User> = runCatching {
        apiService.updateProfile(
            UpdateUserRequestDto(
                displayName = user.displayName,
                photoUrl = user.photoUrl,
                phoneNumber = user.phoneNumber,
                bio = user.bio,
                dateOfBirth = user.dateOfBirth
            )
        ).data.toDomain()
    }

    override suspend fun setRole(role: UserRole, partnerType: PartnerType?): Result<User> = runCatching {
        apiService.setRole(SetRoleRequestDto(role = role, partnerType = partnerType)).data.toDomain()
    }

    override suspend fun registerUser(firebaseUid: String, email: String, displayName: String?): Result<User> = runCatching {
        apiService.registerUser(
            CreateUserRequestDto(
                firebaseUid = firebaseUid,
                email = email,
                displayName = displayName
            )
        ).data.toDomain()
    }

    override suspend fun updateInterestedSports(sportTypes: List<SportType>): Result<List<SportType>> = runCatching {
        apiService.updateInterestedSports(
            UpdateInterestedSportsRequestDto(sportTypes = sportTypes)
        ).data.interestedSports
    }

    override suspend fun completeOnboarding(
        displayName: String?,
        photoUrl: String?,
        dateOfBirth: String?,
        bio: String?,
        interestedSports: List<SportType>,
        expertise: List<Triple<SportType, SkillLevel, ExperienceDuration>>
    ): Result<User> = runCatching {
        apiService.completeOnboarding(
            CompleteOnboardingRequestDto(
                displayName = displayName,
                photoUrl = photoUrl,
                dateOfBirth = dateOfBirth,
                bio = bio,
                interestedSports = interestedSports,
                expertise = expertise.map { (sport, skill, duration) ->
                    SportExpertiseEntryDto(
                        sportType = sport,
                        skillLevel = skill,
                        experienceDuration = duration
                    )
                }
            )
        ).data.toDomain()
    }

    override suspend fun getSportExpertise(): Result<List<UserSportExpertise>> = runCatching {
        apiService.getSportExpertise().data.sportExpertise.map { it.toDomain() }
    }

    override suspend fun setSportExpertise(
        expertise: List<Triple<SportType, SkillLevel, ExperienceDuration>>
    ): Result<List<UserSportExpertise>> = runCatching {
        apiService.setSportExpertise(
            mapOf("expertise" to expertise.map { (sport, skill, duration) ->
                SportExpertiseEntryDto(
                    sportType = sport,
                    skillLevel = skill,
                    experienceDuration = duration
                )
            })
        ).data.sportExpertise.map { it.toDomain() }
    }

    override suspend fun getPublicProfile(userId: Long): Result<PublicPlayerProfile> = runCatching {
        apiService.getPublicProfile(userId).data.toDomain()
    }
}
