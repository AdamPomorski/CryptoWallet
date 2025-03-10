package com.project.cryptowallet.portfolio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoricalPricesDao {



    @Query("SELECT * FROM historical_prices WHERE coinId = :coinId")
    suspend fun getPricesForCoinSymbol(coinId: String): List<HistoricalPriceEntity>

    @Query("SELECT MIN(date) AS latest_date FROM historical_prices WHERE coinId = :coinId ")
    suspend fun getOldestDateForCoin(coinId: String): String?

    @Query("SELECT MAX(date) AS latest_date FROM historical_prices WHERE coinId = :coinId ")
    suspend fun getLatestDateForCoin(coinId: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHistoricalPrice(historicalPriceEntity: HistoricalPriceEntity)

    @Query("SELECT MIN(date) AS latest_date FROM operation_items WHERE coinId = :coinId ")
    suspend fun getOldestOperationDateForCoin(coinId: String): String?

    @Query("DELETE FROM historical_prices")
    suspend fun deleteAllHistoricalPrices()

    @Query("SELECT price FROM historical_prices WHERE coinId = :coinId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestPriceForCoin(coinId: String): Double?

}