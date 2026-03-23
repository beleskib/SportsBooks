package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.domain.model.OnboardingStatus
import com.example.sportsbook.domain.model.StripeConnectStatus
import com.example.sportsbook.domain.repository.StripeConnectRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StripeConnectRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : StripeConnectRepository {

    override suspend fun onboard(): Result<String> = runCatching {
        apiService.stripeConnectOnboard().data.onboardingUrl
    }

    override suspend fun getStatus(): Result<StripeConnectStatus> = runCatching {
        val dto = apiService.getStripeConnectStatus().data
        StripeConnectStatus(
            stripeAccountId = dto.stripeAccountId,
            onboardingStatus = OnboardingStatus.fromString(dto.onboardingStatus),
            payoutsEnabled = dto.payoutsEnabled,
            dashboardUrl = dto.dashboardUrl,
        )
    }

    override suspend fun getDashboardLink(): Result<String> = runCatching {
        apiService.getStripeDashboardLink().data.dashboardUrl
    }
}
