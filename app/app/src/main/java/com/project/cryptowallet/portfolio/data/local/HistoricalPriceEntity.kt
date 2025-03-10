package com.project.cryptowallet.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "historical_prices")
data class HistoricalPriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int ?= null,
    val coinId: String,
    val date: String,
    val price: Double
)
