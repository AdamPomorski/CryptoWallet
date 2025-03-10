package com.project.cryptowallet.crypto_list.presentation.coin_list

import com.project.cryptowallet.core.domain.util.NetworkError

sealed interface CoinListEvent {
    data class Error(val error:NetworkError): CoinListEvent
}