package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.model.Party
import com.example.sportsbook.domain.model.PartyMember
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PartyDto(
    @SerialName("id") val id: Long = 0,
    @SerialName("leaderId") val leaderId: Long = 0,
    @SerialName("leaderName") val leaderName: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("sportType") val sportType: String? = null,
    @SerialName("status") val status: String = "forming",
    @SerialName("matchId") val matchId: Long? = null,
    @SerialName("members") val members: List<PartyMemberDto> = emptyList(),
    @SerialName("createdAt") val createdAt: String? = null
) {
    fun toDomain(): Party = Party(
        id = id,
        leaderId = leaderId,
        leaderName = leaderName,
        name = name,
        sportType = sportType,
        status = status,
        matchId = matchId,
        members = members.map { it.toDomain() },
        createdAt = createdAt
    )
}

@Serializable
data class PartyMemberDto(
    @SerialName("id") val id: Long = 0,
    @SerialName("userId") val userId: Long = 0,
    @SerialName("userName") val userName: String? = null,
    @SerialName("userPhotoUrl") val userPhotoUrl: String? = null,
    @SerialName("status") val status: String = "invited",
    @SerialName("respondedAt") val respondedAt: String? = null
) {
    fun toDomain(): PartyMember = PartyMember(
        id = id,
        userId = userId,
        userName = userName,
        userPhotoUrl = userPhotoUrl,
        status = status,
        respondedAt = respondedAt
    )
}

@Serializable
data class CreatePartyRequestDto(
    @SerialName("name") val name: String? = null,
    @SerialName("sportType") val sportType: String? = null
)

@Serializable
data class InviteToPartyRequestDto(
    @SerialName("userIds") val userIds: List<Long>
)

@Serializable
data class RespondToPartyInviteDto(
    @SerialName("accept") val accept: Boolean
)

@Serializable
data class JoinMatchWithPartyDto(
    @SerialName("partyId") val partyId: Long
)
