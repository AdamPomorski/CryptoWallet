package com.project.cryptowallet.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "operation_items")
data class OperationItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int ?= null,
    val coinId: String,
    val symbol: String,
    val amount: Double,
    val date: String,
    val type: String
)
