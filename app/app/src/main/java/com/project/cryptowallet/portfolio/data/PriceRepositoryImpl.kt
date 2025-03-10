package com.project.cryptowallet.portfolio.data

import com.project.cryptowallet.portfolio.data.local.PortfolioDatabase
import com.project.cryptowallet.portfolio.data.mappers.toHistoricalPrice
import com.project.cryptowallet.portfolio.data.mappers.toHistoricalPriceEntity
import com.project.cryptowallet.core.data.networking.constructUrl
import com.project.cryptowallet.core.data.networking.safeCall
import com.project.cryptowallet.core.domain.models.HistoricalPrice
import com.project.cryptowallet.core.domain.util.NetworkError
import com.project.cryptowallet.core.domain.util.Result
import com.project.cryptowallet.core.domain.util.map
import com.project.cryptowallet.core.domain.util.onError
import com.project.cryptowallet.core.domain.util.onSuccess
import com.project.cryptowallet.crypto_list.data.mappers.toCoinPrice
import com.project.cryptowallet.crypto_list.data.networking.dto.CoinHistoryDto
import com.project.cryptowallet.crypto_list.data.networking.dto.CoinResponseDto
import com.project.cryptowallet.portfolio.domain.PriceRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class PriceRepositoryImpl(
    private val db: PortfolioDatabase,
    private val httpClient: HttpClient
): PriceRepository {
    private val historicalPricesDao = db.historicalPricesDao


    override suspend fun getHistoricalPrice(coinId: String): List<HistoricalPrice> {
        return historicalPricesDao.getPricesForCoinSymbol(coinId).map { it.toHistoricalPrice() }
    }

    override suspend fun getLatestCoinFromApi(coinId: String): Result<HistoricalPrice, NetworkError> {
        return safeCall<CoinResponseDto> {
            httpClient.get(urlString = constructUrl("/assets/$coinId"))
        }
            .map { coinDto ->
                val newCoin = coinDto.data
                HistoricalPrice(
                    coinId = newCoin.id,
                    date = LocalDate.now(),
                    price = newCoin.priceUsd
                )
            }
            .onError {
                  // ✅ Returns null if there's an error
            } // ✅ Extracts the value if it's a success, otherwise returns null
    }



    override suspend fun syncHistoricalPrice(coinId: String, firstOperationDate: LocalDate) {




        val startMillis = firstOperationDate.atStartOfDay(ZoneId.of("UTC"))?.toInstant()?.toEpochMilli() ?: ZonedDateTime.now().minusWeeks(1).toInstant().toEpochMilli()
        val endMillis = ZonedDateTime.now().toInstant().toEpochMilli()



        safeCall<CoinHistoryDto> {
            httpClient.get(
                urlString = constructUrl("/assets/$coinId/history")
            ){
                parameter("interval","d1")
                parameter("start",startMillis)
                parameter("end",endMillis)

            }
        }.map { response ->
            response.data.map { it.toCoinPrice() }
        }.onSuccess {
            val prices = it.map { price ->
                HistoricalPrice(
                    coinId = coinId,
                    date = price.dateTime.toLocalDate(),
                    price = price.priceUsd
                )
            }
            for (price in prices) {
                historicalPricesDao.addHistoricalPrice(price.toHistoricalPriceEntity())
            }
        }.onError { error ->

            //TODO: Handle error


        }




    }



    override suspend fun addHistoricalPrice(historicalPrice: HistoricalPrice) {
        historicalPricesDao.addHistoricalPrice(historicalPrice.toHistoricalPriceEntity())
    }

    override suspend fun deleteAllHistoricalPrices() {
        historicalPricesDao.deleteAllHistoricalPrices()
    }

    override suspend fun getOldestDateForCoin(coinId: String): String? {
        return historicalPricesDao.getOldestDateForCoin(coinId)
    }

    override suspend fun getLatestDateForCoin(coinId: String): String? {
        return historicalPricesDao.getLatestDateForCoin(coinId)
    }

    override suspend fun getLatestPriceForCoin(coinId: String): String {
        return historicalPricesDao.getLatestPriceForCoin(coinId).toString()
    }




}