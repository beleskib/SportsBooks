package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.SavedCard
import com.example.sportsbook.domain.model.SetupIntentResult

interface CardRepository {
    suspend fun getSavedCards(): Result<List<SavedCard>>
    suspend fun createSetupIntent(): Result<SetupIntentResult>
    suspend fun deleteCard(paymentMethodId: String): Result<Unit>
    suspend fun setDefaultCard(paymentMethodId: String): Result<Unit>
}
