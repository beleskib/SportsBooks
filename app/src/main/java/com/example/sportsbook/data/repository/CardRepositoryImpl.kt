package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.domain.model.SavedCard
import com.example.sportsbook.domain.model.SetupIntentResult
import com.example.sportsbook.domain.repository.CardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : CardRepository {

    override suspend fun getSavedCards(): Result<List<SavedCard>> = runCatching {
        apiService.getSavedCards().data.map { it.toDomain() }
    }

    override suspend fun createSetupIntent(): Result<SetupIntentResult> = runCatching {
        apiService.createCardSetupIntent().data.toDomain()
    }

    override suspend fun deleteCard(paymentMethodId: String): Result<Unit> = runCatching {
        apiService.deleteCard(paymentMethodId)
    }

    override suspend fun setDefaultCard(paymentMethodId: String): Result<Unit> = runCatching {
        apiService.setDefaultCard(paymentMethodId)
    }
}
