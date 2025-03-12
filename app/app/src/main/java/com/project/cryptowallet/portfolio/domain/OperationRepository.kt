package com.project.cryptowallet.portfolio.domain

import com.project.cryptowallet.core.domain.models.OperationItem
import com.project.cryptowallet.core.domain.util.DbError
import com.project.cryptowallet.core.domain.util.Result

interface OperationRepository {

    suspend fun addOperation(operation: OperationItem): Result<Unit, DbError>

    suspend fun getAllOperations(): Result<List<OperationItem>, DbError>

    suspend fun deleteAllOperations(): Result<Unit, DbError>

    suspend fun getOldestOperationDateForCoin(coinId: String): Result<String?, DbError>

    suspend fun deleteOperationsForCoin(coinId: String): Result<Unit, DbError>
}