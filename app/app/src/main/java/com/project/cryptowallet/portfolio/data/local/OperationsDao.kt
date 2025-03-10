package com.project.cryptowallet.portfolio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OperationsDao {


    @Query("SELECT * FROM operation_items")
    suspend fun getAllOperations(): List<OperationItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOperation(operation: OperationItemEntity)

    @Query("DELETE FROM operation_items")
    suspend fun deleteAllOperations()

    @Query("SELECT MIN(date) AS latest_date FROM operation_items WHERE coinId = :coinId ")
    suspend fun getOldestOperationDateForCoin(coinId: String): String?

    @Query("DELETE FROM operation_items WHERE coinId = :coinId")
    suspend fun deleteOperationsForCoin(coinId: String)


}