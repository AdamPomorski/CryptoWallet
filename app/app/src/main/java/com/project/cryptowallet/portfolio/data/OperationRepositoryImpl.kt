package com.project.cryptowallet.portfolio.data

import android.database.sqlite.SQLiteException
import com.project.cryptowallet.portfolio.data.local.PortfolioDatabase
import com.project.cryptowallet.portfolio.data.mappers.toOperationItem
import com.project.cryptowallet.portfolio.data.mappers.toOperationItemEntity
import com.project.cryptowallet.core.domain.models.OperationItem
import com.project.cryptowallet.core.domain.util.DbError
import com.project.cryptowallet.core.domain.util.Result
import com.project.cryptowallet.portfolio.domain.OperationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class OperationRepositoryImpl(
    private val db: PortfolioDatabase
) : OperationRepository {

    private val operationsDao = db.operationsDao

    override suspend fun addOperation(operation: OperationItem): Result<Unit, DbError> {
        return withContext(Dispatchers.IO) {
            try {
                operationsDao.addOperation(operation.toOperationItemEntity())
                Result.Success(Unit)
            } catch (e: Exception) {
                when (e) {
                    is SQLiteException -> Result.Error(DbError.WRITE_ERROR)
                    is IOException -> Result.Error(DbError.WRITE_ERROR)
                    else -> Result.Error(DbError.UNKNOWN)
                }
            }
        }
    }

    override suspend fun getAllOperations(): Result<List<OperationItem>, DbError> {
        return withContext(Dispatchers.IO) {

            try {
                val operations = operationsDao.getAllOperations().map { it.toOperationItem() }
                Result.Success(operations)
            } catch (e: Exception) {
                when (e) {
                    is SQLiteException -> Result.Error(DbError.QUERY_ERROR)
                    is IOException -> Result.Error(DbError.READ_ERROR)
                    else -> Result.Error(DbError.UNKNOWN)
                }
            }
        }
    }

    override suspend fun deleteAllOperations(): Result<Unit, DbError> {
        return withContext(Dispatchers.IO) {
            try {
                operationsDao.deleteAllOperations()
                Result.Success(Unit)
            } catch (e: Exception) {
                when (e) {
                    is SQLiteException -> Result.Error(DbError.WRITE_ERROR)
                    is IOException -> Result.Error(DbError.WRITE_ERROR)
                    else -> Result.Error(DbError.UNKNOWN)
                }
            }
        }
    }

    override suspend fun getOldestOperationDateForCoin(coinId: String): Result<String?, DbError> {
        return withContext(Dispatchers.IO) {
            try {
                val date = operationsDao.getOldestOperationDateForCoin(coinId)
                Result.Success(date)
            } catch (e: Exception) {
                when (e) {
                    is SQLiteException -> Result.Error(DbError.QUERY_ERROR)
                    is IOException -> Result.Error(DbError.READ_ERROR)
                    else -> Result.Error(DbError.UNKNOWN)
                }
            }
        }
    }

    override suspend fun deleteOperationsForCoin(coinId: String): Result<Unit, DbError> {
        return withContext(Dispatchers.IO) {
            try {
                operationsDao.deleteOperationsForCoin(coinId)
                Result.Success(Unit)
            } catch (e: Exception) {
                when (e) {
                    is SQLiteException -> Result.Error(DbError.WRITE_ERROR)
                    is IOException -> Result.Error(DbError.WRITE_ERROR)
                    else -> Result.Error(DbError.UNKNOWN)
                }
            }
        }
    }
}
