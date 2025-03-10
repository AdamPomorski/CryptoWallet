package com.project.cryptowallet.crypto_list.presentation.coin_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.cryptowallet.core.domain.util.onError
import com.project.cryptowallet.core.domain.util.onSuccess
import com.project.cryptowallet.crypto_list.domain.CoinDataSource
import com.project.cryptowallet.crypto_list.presentation.coin_detail.DataPoint
import com.project.cryptowallet.crypto_list.presentation.models.CoinListAction
import com.project.cryptowallet.crypto_list.presentation.models.CoinUi
import com.project.cryptowallet.crypto_list.presentation.models.toCoinUI
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CoinListViewModel(
    private val coinDataSource: CoinDataSource
): ViewModel(){
    private val _state = MutableStateFlow(CoinListState())
    val state = _state
        .onStart { loadCoins() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            CoinListState()
        )

    private val _events = Channel<CoinListEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CoinListAction){
        when(action){
            is CoinListAction.OnCoinClick -> {
                selectCoin(action.coinUi)

            }

        }
    }

    private fun selectCoin(coinUi: CoinUi){
        _state.update { it.copy(
            selectedCoin = coinUi
        ) }
        viewModelScope.launch {
            coinDataSource
                .getCoinHistory(
                    coinId = coinUi.id,
                    start = ZonedDateTime.now().minusDays(30L),
                    end = ZonedDateTime.now()
                )
                .onSuccess { history->
                    val dataPoints = history
                        .sortedBy { it.dateTime }
                        .map {
                            DataPoint(
                                x = it.dateTime.hour.toFloat(),
                                y = it.priceUsd.toFloat(),
                                xLabel = DateTimeFormatter
                                    .ofPattern("d/M")
                                    .format(it.dateTime)
                            )
                        }

                    _state.update {
                        it.copy(selectedCoin = it.selectedCoin?.copy(
                            coinPriceHistory = dataPoints
                        ))
                    }
                }
                .onError { error->
                    _events.send(CoinListEvent.Error(error))
                }
        }

    }



    private fun loadCoins() {
        viewModelScope.launch {
            _state.update { it.copy(
                isLoading = true
            )
            }
            coinDataSource
                .getCoins()
                .onSuccess { coins ->
                    _state.update { it.copy(
                        isLoading = false,
                        coins = coins.map { it.toCoinUI() }
                        ) }


                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false)}
                    _events.send(CoinListEvent.Error(error))

                }
        }
    }
    fun refreshCoins(){
        if (state.value.coins.isEmpty()){
            loadCoins()
        }
    }

}