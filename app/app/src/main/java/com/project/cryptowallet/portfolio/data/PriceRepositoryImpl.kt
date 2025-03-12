package com.project.cryptowallet.portfolio.data

import android.database.sqlite.SQLiteException
import com.project.cryptowallet.portfolio.data.local.PortfolioDatabase
import com.project.cryptowallet.portfolio.data.mappers.toHistoricalPrice
import com.project.cryptowallet.portfolio.data.mappers.toHistoricalPriceEntity
import com.project.cryptowallet.core.data.networking.constructUrl
import com.project.cryptowallet.core.data.networking.safeCall
import com.project.cryptowallet.core.domain.models.HistoricalPrice
import com.project.cryptowallet.core.domain.util.DbError
import com.project.cryptowallet.core.domain.util.Error
import com.project.cryptowallet.core.domain.util.NetworkError
import com.project.cryptowallet.core.domain.util.Result
import com.project.cryptowallet.core.domain.util.flatMap
import com.project.cryptowallet.core.domain.util.map
import com.project.cryptowallet.core.domain.util.onError
import com.project.cryptowallet.core.domain.util.onSuccess
import com.project.cryptowallet.crypto_list.data.mappers.toCoinPrice
import com.project.cryptowallet.crypto_list.data.networking.dto.CoinHistoryDto
import com.project.cryptowallet.crypto_list.data.networking.dto.CoinResponseDto
import com.project.cryptowallet.crypto_list.domain.CoinPrice
import com.project.cryptowallet.portfolio.domain.PriceRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.coroutines.resume

class PriceRepositoryImpl(
    private val db: PortfolioDatabase,
    private val httpClient: HttpClient
) : PriceRepository {

    private val historicalPricesDao = db.historicalPricesDao

    override suspend fun getHistoricalPrice(coinId: String): Result<List<HistoricalPrice>, DbError> {
        return withContext(Dispatchers.IO) {
            try {
                val prices = historicalPricesDao.getPricesForCoinSymbol(coinId)
                    .map { it.toHistoricalPrice() }
                Result.Success(prices)
            } catch (e: Exception) {
                val error = when (e) {
                    is SQLiteException -> DbError.QUERY_ERROR
                    is IOException -> DbError.READ_ERROR
                    else -> DbError.UNKNOWN
                }
                Result.Error(error)
            }
        }
    }


    override suspend fun getLatestCoinFromApi(coinId: String): Result<HistoricalPrice, NetworkError> {
        return withContext(Dispatchers.IO) {
            safeCall<CoinResponseDto> {
                httpClient.get(urlString = constructUrl("/assets/$coinId"))
            }.map { coinDto ->
                val newCoin = coinDto.data
                HistoricalPrice(
                    coinId = newCoin.id,
                    date = LocalDate.now(),
                    price = newCoin.priceUsd
                )
            }
        }
    }

    override suspend fun syncHistoricalPrice(
        coinId: String,
        firstOperationDate: LocalDate
    ): Result<List<HistoricalPrice>, Error> {
        return withContext(Dispatchers.IO) {

            val startMillis =
                firstOperationDate.atStartOfDay(ZoneId.of("UTC"))?.toInstant()?.toEpochMilli()
                    ?: ZonedDateTime.now().minusWeeks(1).toInstant().toEpochMilli()
            val endMillis = ZonedDateTime.now().toInstant().toEpochMilli()

            safeCall<CoinHistoryDto> {
                httpClient.get(urlString = constructUrl("/assets/$coinId/history")) {
                    parameter("interval", "d1")
                    parameter("start", startMillis)
                    parameter("end", endMillis)
                }
            }.map { response ->
                response.data.map { it.toCoinPrice() }
            }.flatMap { prices ->
                try {
                    val historicalPrices = prices.map { price ->
                        HistoricalPrice(
                            coinId = coinId,
                            date = price.dateTime.toLocalDate(),
                            price = price.priceUsd
                        )
                    }
                    historicalPricesDao.addHistoricalPrices(historicalPrices.map { it.toHistoricalPriceEntity() })
                    Result.Success(historicalPrices)
                } catch (e: Exception) {
                    Result.Error(DbError.WRITE_ERROR)
                } catch (e: Exception) {
                    Result.Error(DbError.UNKNOWN)
                }
            }
        }
    }


    override suspend fun deleteAllHistoricalPrices(): Result<Unit, DbError> {
        return withContext(Dispatchers.IO) {
            try {
                historicalPricesDao.deleteAllHistoricalPrices()
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

    override suspend fun getOldestDateForCoin(coinId: String): Result<String?, DbError> {
        return withContext(Dispatchers.IO) {
            try {
                Result.Success(historicalPricesDao.getOldestDateForCoin(coinId))
            } catch (e: Exception) {
                when (e) {
                    is SQLiteException -> Result.Error(DbError.QUERY_ERROR)
                    is IOException -> Result.Error(DbError.READ_ERROR)
                    else -> Result.Error(DbError.UNKNOWN)
                }
            }
        }
    }

    override suspend fun getLatestDateForCoin(coinId: String): Result<String?, DbError> {
        return withContext(Dispatchers.IO) {
            try {
                Result.Success(historicalPricesDao.getLatestDateForCoin(coinId))
            } catch (e: Exception) {
                when (e) {
                    is SQLiteException -> Result.Error(DbError.QUERY_ERROR)
                    is IOException -> Result.Error(DbError.READ_ERROR)
                    else -> Result.Error(DbError.UNKNOWN)
                }
            }
        }
    }

    override suspend fun getLatestPriceForCoin(coinId: String): Result<String, DbError> {
        return withContext(Dispatchers.IO) {
            try {
                val price = historicalPricesDao.getLatestPriceForCoin(coinId)?.toString() ?: "0.0"
                Result.Success(price)
            } catch (e: Exception) {
                when (e) {
                    is SQLiteException -> Result.Error(DbError.QUERY_ERROR)
                    is IOException -> Result.Error(DbError.READ_ERROR)
                    else -> Result.Error(DbError.UNKNOWN)
                }
            }
        }
    }
}
