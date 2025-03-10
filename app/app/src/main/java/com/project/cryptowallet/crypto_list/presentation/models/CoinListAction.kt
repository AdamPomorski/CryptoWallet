package com.project.cryptowallet.crypto_list.presentation.models



sealed interface CoinListAction {
    data class OnCoinClick(val coinUi: CoinUi): CoinListAction

}