package com.project.cryptowallet.crypto_list.domain

import com.project.cryptowallet.core.domain.util.NetworkError
import com.project.cryptowallet.core.domain.util.Result
import java.time.ZonedDateTime

interface CoinDataSource {
    suspend fun getCoins(): Result<List<Coin>, NetworkError>
    suspend fun getCoinHistory(
        coinId: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Result<List<CoinPrice>, NetworkError>

    //suspend fun getWalletItems(): Flow<Result<List<WalletItem>, DbError>>

}