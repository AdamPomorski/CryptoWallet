package com.project.cryptowallet.crypto_list.data.networking.dto

import kotlinx.serialization.Serializable

@Serializable
data class CoinsResponseDto (
    val data: List<CoinDto>
)

