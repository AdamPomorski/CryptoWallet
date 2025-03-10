package com.project.cryptowallet.core.domain.models

import java.time.LocalDate

data class HistoricalPrice(
    val coinId: String,
    val date: LocalDate,
    val price: Double
) {
}