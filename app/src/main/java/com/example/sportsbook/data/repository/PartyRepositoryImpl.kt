package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.CreatePartyRequestDto
import com.example.sportsbook.data.remote.dto.InviteToPartyRequestDto
import com.example.sportsbook.data.remote.dto.JoinMatchWithPartyDto
import com.example.sportsbook.data.remote.dto.RespondToPartyInviteDto
import com.example.sportsbook.domain.model.Party
import com.example.sportsbook.domain.repository.PartyRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartyRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PartyRepository {

    override suspend fun createParty(name: String?, sportType: String?): Result<Party> = runCatching {
        apiService.createParty(CreatePartyRequestDto(name = name, sportType = sportType)).data.toDomain()
    }

    override suspend fun getActiveParties(): Result<List<Party>> = runCatching {
        apiService.getActiveParties().data.map { it.toDomain() }
    }

    override suspend fun getPartyById(id: Long): Result<Party> = runCatching {
        apiService.getPartyById(id).data.toDomain()
    }

    override suspend fun inviteToParty(partyId: Long, userIds: List<Long>): Result<Party> = runCatching {
        apiService.inviteToParty(partyId, InviteToPartyRequestDto(userIds = userIds)).data.toDomain()
    }

    override suspend fun respondToInvite(partyId: Long, accept: Boolean): Result<Party> = runCatching {
        apiService.respondToPartyInvite(partyId, RespondToPartyInviteDto(accept = accept)).data.toDomain()
    }

    override suspend fun disbandParty(partyId: Long): Result<Unit> = runCatching {
        apiService.disbandParty(partyId)
        Unit
    }

    override suspend fun joinMatchWithParty(matchId: Long, partyId: Long): Result<Unit> = runCatching {
        apiService.joinMatchWithParty(matchId, JoinMatchWithPartyDto(partyId = partyId))
        Unit
    }
}
