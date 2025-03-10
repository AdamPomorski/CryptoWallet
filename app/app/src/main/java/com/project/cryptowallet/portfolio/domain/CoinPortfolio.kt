package com.project.cryptowallet.portfolio.domain

data class CoinPortfolio(
    val symbol: String,
    val priceUsd: Double,
    val amount: Double,
    val value: Double

)
