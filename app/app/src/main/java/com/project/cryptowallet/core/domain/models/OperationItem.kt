package com.project.cryptowallet.core.domain.models

import java.time.LocalDate

data class OperationItem(
    val coinId: String,
    val symbol: String,
    val amount: Double,
    val date: LocalDate,
    val type: String
) {
}