package com.project.cryptowallet.portfolio.domain

import com.project.cryptowallet.core.domain.models.HistoricalPrice
import com.project.cryptowallet.core.domain.util.DbError
import com.project.cryptowallet.core.domain.util.Error
import com.project.cryptowallet.core.domain.util.NetworkError
import com.project.cryptowallet.core.domain.util.Result
import java.time.LocalDate

interface PriceRepository {

    suspend fun getHistoricalPrice(coinId: String): Result<List<HistoricalPrice>, DbError>

    suspend fun syncHistoricalPrice(coinId: String, firstOperationDate: LocalDate): Result<List<HistoricalPrice>, Error>

    suspend fun deleteAllHistoricalPrices(): Result<Unit, DbError>

    suspend fun getOldestDateForCoin(coinId: String): Result<String?, DbError>

    suspend fun getLatestDateForCoin(coinId: String): Result<String?, DbError>

    suspend fun getLatestPriceForCoin(coinId: String): Result<String?, DbError>

    suspend fun getLatestCoinFromApi(coinId: String): Result<HistoricalPrice, NetworkError>

}