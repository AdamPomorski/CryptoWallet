package com.project.cryptowallet.crypto_list.data.networking.dto

import kotlinx.serialization.Serializable

@Serializable
data class CoinHistoryDto (
    val data: List<CoinPriceDto>
)

