package com.project.cryptowallet.crypto_list.presentation.coin_list

import androidx.compose.runtime.Immutable
import com.project.cryptowallet.crypto_list.presentation.models.CoinUi

@Immutable
data class CoinListState(
    val isLoading: Boolean = false,
    val coins: List<CoinUi> = emptyList(),
    val selectedCoin:CoinUi? = null

    )
