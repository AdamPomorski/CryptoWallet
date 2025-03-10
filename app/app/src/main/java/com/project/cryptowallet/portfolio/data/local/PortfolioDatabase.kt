package com.project.cryptowallet.portfolio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [OperationItemEntity::class, HistoricalPriceEntity::class],
    version = 1
)
abstract class PortfolioDatabase: RoomDatabase() {
    abstract val historicalPricesDao: HistoricalPricesDao
    abstract val operationsDao: OperationsDao
}