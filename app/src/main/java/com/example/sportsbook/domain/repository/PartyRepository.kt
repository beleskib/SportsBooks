package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.Party

interface PartyRepository {
    suspend fun createParty(name: String?, sportType: String?): Result<Party>
    suspend fun getActiveParty(): Result<Party?>
    suspend fun getPartyById(id: Long): Result<Party>
    suspend fun inviteToParty(partyId: Long, userIds: List<Long>): Result<Party>
    suspend fun respondToInvite(partyId: Long, accept: Boolean): Result<Party>
    suspend fun disbandParty(partyId: Long): Result<Unit>
    suspend fun joinMatchWithParty(matchId: Long, partyId: Long): Result<Unit>
}
