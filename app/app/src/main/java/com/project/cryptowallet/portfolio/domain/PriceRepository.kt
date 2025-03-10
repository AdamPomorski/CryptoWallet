package com.project.cryptowallet.portfolio.domain

import com.project.cryptowallet.core.domain.models.HistoricalPrice
import com.project.cryptowallet.core.domain.util.NetworkError
import com.project.cryptowallet.core.domain.util.Result
import java.time.LocalDate

interface PriceRepository {

    suspend fun getHistoricalPrice(coinId: String): List<HistoricalPrice>

    suspend fun syncHistoricalPrice(coinId: String, firstOperationDate: LocalDate)

    suspend fun addHistoricalPrice(historicalPrice: HistoricalPrice)

    suspend fun deleteAllHistoricalPrices()

    suspend fun getOldestDateForCoin(coinId: String): String?

    suspend fun getLatestDateForCoin(coinId: String): String?

    suspend fun getLatestPriceForCoin(coinId: String): String?

    suspend fun getLatestCoinFromApi(coinId: String): Result<HistoricalPrice, NetworkError>

}