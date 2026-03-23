package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.StripeConnectStatus

interface StripeConnectRepository {
    suspend fun onboard(): Result<String>
    suspend fun getStatus(): Result<StripeConnectStatus>
    suspend fun getDashboardLink(): Result<String>
}
