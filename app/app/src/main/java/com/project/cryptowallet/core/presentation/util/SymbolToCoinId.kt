package com.project.cryptowallet.core.presentation.util

import com.project.cryptowallet.crypto_list.domain.Coin

fun symbolToCoinId(symbol: String, coinsList: List<Coin>): String {
    return coinsList.find { it.symbol == symbol }?.id ?: ""
}