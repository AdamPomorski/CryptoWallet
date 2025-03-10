package com.project.cryptowallet.crypto_list.data.repository

import com.project.cryptowallet.core.data.networking.constructUrl
import com.project.cryptowallet.core.data.networking.safeCall
import com.project.cryptowallet.core.domain.util.NetworkError
import com.project.cryptowallet.core.domain.util.Result
import com.project.cryptowallet.core.domain.util.map
import com.project.cryptowallet.core.domain.util.onSuccess
import com.project.cryptowallet.crypto_list.data.mappers.toCoin
import com.project.cryptowallet.crypto_list.data.mappers.toCoinPrice
import com.project.cryptowallet.crypto_list.data.networking.dto.CoinHistoryDto
import com.project.cryptowallet.crypto_list.data.networking.dto.CoinsResponseDto
import com.project.cryptowallet.crypto_list.domain.Coin
import com.project.cryptowallet.crypto_list.domain.CoinDataSource
import com.project.cryptowallet.crypto_list.domain.CoinPrice
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import java.time.ZoneId
import java.time.ZonedDateTime

class CoinDataSourceImpl(
    private val httpClient: HttpClient,

): CoinDataSource {

    private var cachedCoins: List<Coin>? = null
    private var lastFetchTime: Long = 0
    private val cacheDurationMillis = 24 * 60 * 60 * 1000 // 24 hours



    override suspend fun getCoins(): Result<List<Coin>, NetworkError> {
        val currentTime = System.currentTimeMillis()

        // Return cached data if it's still valid
        if (cachedCoins != null && (currentTime - lastFetchTime) < cacheDurationMillis) {
            return Result.Success(cachedCoins!!)
        }

        return safeCall<CoinsResponseDto> {


            httpClient.get(
                urlString = constructUrl("/assets")
            )
        }.map { response ->
            response.data.map { it.toCoin() }
        }.onSuccess { coins ->
            cachedCoins = coins
            lastFetchTime = currentTime
        }
    }

    override suspend fun getCoinHistory(
        coinId: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Result<List<CoinPrice>, NetworkError> {

        val startMillis = start
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()

        val endMillis = end
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()


        return safeCall<CoinHistoryDto> {
            httpClient.get(
                urlString = constructUrl("/assets/$coinId/history")
            ){
                parameter("interval","d1")
                parameter("start",startMillis)
                parameter("end",endMillis)

            }
        }.map { response ->
            response.data.map { it.toCoinPrice() }
        }

    }

//    override suspend fun getWalletItems(): Flow<Result<List<WalletItem>, DbError>> {
//        return flow {
//            val walletItems = dao.getWalletItems().map { it.toWalletItem() }
//            if(walletItems.isEmpty()){
//                emit(Result.Error(DbError.DATABASE_NOT_FOUND))
//            }else {
//                emit(Result.Success(walletItems))
//            }
//        }
//
//    }

}