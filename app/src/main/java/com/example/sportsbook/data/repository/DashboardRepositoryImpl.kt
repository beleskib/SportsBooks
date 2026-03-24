package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.domain.model.PartnerDashboardStats
import com.example.sportsbook.domain.repository.DashboardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : DashboardRepository {
    override suspend fun getPartnerStats(): Result<PartnerDashboardStats> = runCatching {
        apiService.getPartnerDashboardStats().data.toDomain()
    }
}
