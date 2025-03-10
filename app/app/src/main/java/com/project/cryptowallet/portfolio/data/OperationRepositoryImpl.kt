package com.project.cryptowallet.portfolio.data

import com.project.cryptowallet.portfolio.data.local.PortfolioDatabase
import com.project.cryptowallet.portfolio.data.mappers.toOperationItem
import com.project.cryptowallet.portfolio.data.mappers.toOperationItemEntity
import com.project.cryptowallet.core.domain.models.OperationItem
import com.project.cryptowallet.portfolio.domain.OperationRepository

class OperationRepositoryImpl(
    private val db: PortfolioDatabase
):OperationRepository {

    private val operationsDao = db.operationsDao

    override suspend fun addOperation(operation: OperationItem) {
        operationsDao.addOperation(operation.toOperationItemEntity())
    }

    override suspend fun getAllOperations(): List<OperationItem> {
        return operationsDao.getAllOperations().map { it.toOperationItem() }

    }
    override suspend fun deleteAllOperations() {
        operationsDao.deleteAllOperations()
    }

    override suspend fun getOldestOperationDateForCoin(coinId: String): String? {
        return operationsDao.getOldestOperationDateForCoin(coinId)
    }

    override suspend fun deleteOperationsForCoin(coinId: String) {
        operationsDao.deleteOperationsForCoin(coinId)
    }



}