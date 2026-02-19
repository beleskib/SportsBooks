package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.Sport

interface SportRepository {
    suspend fun getSports(): Result<List<Sport>>
    suspend fun getSportById(id: Long): Result<Sport>
}
