package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.PartnerDashboardStats

interface DashboardRepository {
    suspend fun getPartnerStats(): Result<PartnerDashboardStats>
}
