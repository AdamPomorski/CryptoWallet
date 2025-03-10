package com.project.cryptowallet.portfolio.domain

import com.project.cryptowallet.core.domain.models.OperationItem

interface OperationRepository {

    suspend fun addOperation(operation: OperationItem)

    suspend fun getAllOperations(): List<OperationItem>

    suspend fun deleteAllOperations()

    suspend fun getOldestOperationDateForCoin(coinId: String): String?

    suspend fun deleteOperationsForCoin(coinId: String)
}