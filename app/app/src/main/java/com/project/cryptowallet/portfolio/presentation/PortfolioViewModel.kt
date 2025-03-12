package com.project.cryptowallet.portfolio.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.cryptowallet.core.domain.models.HistoricalPrice
import com.project.cryptowallet.core.domain.models.OperationItem
import com.project.cryptowallet.core.domain.util.DbError
import com.project.cryptowallet.core.domain.util.Result
import com.project.cryptowallet.core.domain.util.onError
import com.project.cryptowallet.core.domain.util.onSuccess
import com.project.cryptowallet.core.domain.util.toLocalDate
import com.project.cryptowallet.core.presentation.util.getDrawableIdForCoin
import com.project.cryptowallet.core.presentation.util.symbolToCoinId
import com.project.cryptowallet.crypto_list.domain.Coin
import com.project.cryptowallet.crypto_list.domain.CoinDataSource
import com.project.cryptowallet.crypto_list.presentation.coin_list.CoinListEvent
import com.project.cryptowallet.crypto_list.presentation.models.toDisplayableNumber
import com.project.cryptowallet.portfolio.domain.OperationRepository
import com.project.cryptowallet.portfolio.domain.PriceRepository
import com.project.cryptowallet.portfolio.domain.use_cases.CalculatePortfolioUseCase
import com.project.cryptowallet.portfolio.domain.use_cases.ManageAssetsUseCase
import com.project.cryptowallet.portfolio.presentation.models.PieChartData
import com.project.cryptowallet.portfolio.presentation.models.PortfolioItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PortfolioViewModel(
    private val operationRepository: OperationRepository,
    private val priceRepository: PriceRepository,
    private val calculatePortfolioUseCase: CalculatePortfolioUseCase,
    private val manageAssetsUseCase: ManageAssetsUseCase,
) : ViewModel() {

//    private val _state = MutableStateFlow(PortfolioState())
//    val state = _state
//        .onStart { calculatePortfolioValueOverTime() }
//        .stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5000L),
//            PortfolioState()
//        )
//
//    private val _events = Channel<PortfolioEvent>()
//    val events = _events.receiveAsFlow()

//    var coinsList: List<Coin> = emptyList()
//    var syncCounter: Int = 0

    private val _state = MutableStateFlow(PortfolioState())
    val state = _state
        .onStart {  }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            PortfolioState()
        )

    private val _events = Channel<PortfolioEvent>()
    val events = _events.receiveAsFlow()



    init {
        // Fetch and calculate portfolio values when ViewModel is created
        viewModelScope.launch {
            //operationRepository.deleteAllOperations()
            priceRepository.deleteAllHistoricalPrices()
            //insertTestData()
            //delay(2000)
//            val portfolioValueMap = calculatePortfolioValueOverTime()
//            _portfolioValueMap.value = portfolioValueMap
        }
    }

    fun refreshPortfolio() {
        viewModelScope.launch {
            calculatePortfolioUseCase()
                .onStart {  }
                .collect { result ->
                    when (result) {
                        is Result.Loading -> _state.update { it.copy(isLoading = true) }
                        is Result.Success -> {
                            _state.update { currentState ->
                                currentState.copy(
                                    portfolioValueMap = result.data.portfolioValueMap, // New object
                                    portfolioItems = result.data.portfolioItems, // New object
                                    pieChartData = result.data.pieChartData, // New object
                                    isLoading = false,
                                    isRefreshing = false
                                )
                            }
                        }
                        is Result.Error -> {
                            _events.send(PortfolioEvent.Error(result.error))
                            _state.update { it.copy(isLoading = false, isRefreshing = false) }
                        }
                }
        }
    }
}

//    fun refreshPortfolio() {
//        viewModelScope.launch {
//            _state.update {
//                it.copy(isRefreshing = true)
//            }
//            calculatePortfolioValueOverTime() // Recalculate portfolio data
//            _state.update {
//                it.copy(isRefreshing = false)
//            }
//        }
//    }


//    fun calculatePortfolioValueOverTime() {
//        viewModelScope.launch {
//            delay(2000) // Simulate a delay
//            _state.update { it.copy(isLoading = true) }
//
//            // Step 1: Fetch all operations from the database
//            val operationsResult = operationRepository.getAllOperations()
//
//            if (operationsResult is Result.Error) {
//                _events.send(PortfolioEvent.Error(operationsResult.error))
//                _state.update { it.copy(isLoading = false) }
//                return@launch
//            }
//
//            val operations = (operationsResult as Result.Success).data
//            val operationsByDate = operations.groupBy { it.date }
//
//            val historicalPrices = mutableMapOf<String, List<HistoricalPrice>>()
//            val coinIds = operations.map { it.coinId }.toSet()
//            val coinIdsAndSymbols = operations.associate { it.coinId to it.symbol }
//            val latestPriceDateForCoins = mutableMapOf<String, String>()
//            val today = LocalDate.now()
//
//            for (coinId in coinIds) {
//                syncCounter = 0
//                val firstOperationDateResult = operationRepository.getOldestOperationDateForCoin(coinId)
//                if (firstOperationDateResult is Result.Error) {
//                    _events.send(PortfolioEvent.Error(firstOperationDateResult.error))
//                    continue
//                }
//                val firstOperationDate = (firstOperationDateResult as Result.Success).data
//
//
//                val latestPriceDateResult = priceRepository.getLatestDateForCoin(coinId)
//                val latestPriceDate = (latestPriceDateResult as? Result.Success)?.data
//
//                latestPriceDateForCoins[coinId] = latestPriceDate?.toString() ?: today.toString()
//
//                val pricesResult = withContext(Dispatchers.IO){
//                    priceRepository.getHistoricalPrice(coinId)
//                }
//                if (pricesResult is Result.Error) {
//                    _events.send(PortfolioEvent.Error(pricesResult.error))
//                    continue
//                }
//                var prices = (pricesResult as Result.Success).data.toMutableList()
//                syncCounter++
//
//
//                if (prices.isEmpty() && firstOperationDate != null) {
//                    val syncResult = priceRepository.syncHistoricalPrice(coinId, firstOperationDate.toLocalDate())
//                    if (syncResult is Result.Error) {
//                        _events.send(PortfolioEvent.Error(syncResult.error))
//                        continue
//                    }
//                    prices = (syncResult as Result.Success).data.toMutableList()
//                }
//
//                if (prices.isNotEmpty() && latestPriceDate != null && latestPriceDate < today.minusDays(1).toString()) {
//                    val syncResult = priceRepository.syncHistoricalPrice(coinId, latestPriceDate.toLocalDate().plusDays(1))
//                    if (syncResult is Result.Error) {
//                        _events.send(PortfolioEvent.Error(syncResult.error))
//                        continue
//                    }
//
//                    prices = (syncResult as Result.Success).data.toMutableList()
//                }
//
//                val latestHistoricalDate = prices.maxByOrNull { it.date }?.date
//                if (latestHistoricalDate == null || latestHistoricalDate.toString() != today.toString()) {
//                    val latestPriceResult = priceRepository.getLatestCoinFromApi(coinId)
//                    when (latestPriceResult){
//                        is Result.Error -> {
//                            _events.send(PortfolioEvent.Error(latestPriceResult.error))
//                            continue
//                        }
//                        is Result.Success -> {
//                            val latestPrice = latestPriceResult.data
//                            prices.add(latestPrice)
//                        }
//                    }
//                }
//
//                historicalPrices[coinId] = prices
//            }
//
//            // Step 4: Calculate the portfolio value for each day
//            val portfolioValueMap = mutableMapOf<LocalDate, Double>()
//            val coinAmounts = mutableMapOf<String, Double>()
//
//            val sortedDates = operationsByDate.keys.sorted()
//            val firstDate = sortedDates.firstOrNull() ?: today
//            val allDates = generateSequence(firstDate) { it.plusDays(1) }
//                .takeWhile { it <= today }
//                .toList()
//
//            for (date in allDates) {
//                val operationsForDate = operationsByDate[date] ?: emptyList()
//                for (operation in operationsForDate) {
//                    val currentAmount = coinAmounts.getOrDefault(operation.coinId, 0.0)
//                    val newAmount = when (operation.type) {
//                        "add" -> currentAmount + operation.amount
//                        "subtract" -> currentAmount - operation.amount
//                        else -> currentAmount
//                    }
//                    coinAmounts[operation.coinId] = newAmount
//                }
//
//                var portfolioValue = portfolioValueMap[date] ?: 0.0
//                for ((coinId, amount) in coinAmounts) {
//                    val pricesForCoin = historicalPrices[coinId] ?: emptyList()
//                    val priceForDate = pricesForCoin.find { it.date == date }?.price ?: 0.0
//                    portfolioValue += amount * priceForDate
//                }
//
//                portfolioValueMap[date] = portfolioValue
//            }
//
//            // Step 5: Prepare Portfolio Items
//            val portfolioItems = mutableListOf<PortfolioItem>()
//            val pieChartData = mutableListOf<PieChartData>()
//
//            for ((coinId, amount) in coinAmounts) {
//                if (amount == 0.0) continue
//                val pricesForCoin = historicalPrices[coinId] ?: emptyList()
//                val priceForDate = pricesForCoin.find { it.date == today }?.price ?: 0.0
//                val symbol = coinIdsAndSymbols[coinId] ?: ""
//                val value = amount * priceForDate
//                val portfolioItem = PortfolioItem(
//                    symbol = symbol,
//                    priceUsd = priceForDate.toDisplayableNumber(),
//                    amount = amount.toDisplayableNumber(),
//                    value = value.toDisplayableNumber(),
//                    iconRes = getDrawableIdForCoin(symbol)
//                )
//                portfolioItems.add(portfolioItem)
//                pieChartData.add(PieChartData(symbol, value.toFloat()))
//            }
//
//            _state.update {
//                it.copy(
//                    portfolioItems = portfolioItems,
//                    pieChartData = pieChartData,
//                    portfolioValueMap = portfolioValueMap,
//                    isLoading = false
//                )
//            }
//        }
//    }

    fun addAsset(symbol: String, amount: Double) {
        viewModelScope.launch {
            manageAssetsUseCase.addAsset(symbol, amount)
                .onStart { _state.update { it.copy(isLoading = true) } }
                .collect { result ->
                    when (result) {
                        is Result.Success -> _state.update { it.copy(portfolioItems = it.portfolioItems + result.data) }
                        is Result.Error -> {
                            _events.send(PortfolioEvent.Error(result.error))
                            _state.update { it.copy(isLoading = false) }
                        }
                        is Result.Loading -> _state.update { it.copy(isLoading = true) }
                    }
                }
            refreshPortfolio()
        }
    }

    fun editAsset(symbol: String, initialAmount: Double, newAmount: Double) {
        viewModelScope.launch {
            manageAssetsUseCase.editAsset(symbol, initialAmount, newAmount)
                .onStart { _state.update { it.copy(isLoading = true) } }
                .collect { result ->
                    when (result) {
                        is Result.Success -> _state.update {
                            it.copy(portfolioItems = it.portfolioItems.map { item ->
                                if (item.symbol == symbol) item.copy(
                                    amount = newAmount.toDisplayableNumber(),
                                    value = (newAmount * item.priceUsd.value).toDisplayableNumber()
                                ) else item
                            })
                        }

                        is Result.Error -> {
                            _events.send(PortfolioEvent.Error(result.error))
                            _state.update { it.copy(isLoading = false) }
                        }

                        is Result.Loading -> _state.update { it.copy(isLoading = true) }
                    }
                }
            //refreshPortfolio()
        }


    }

    fun deleteAsset(symbol: String, amount: Double) {

        viewModelScope.launch {
            manageAssetsUseCase.deleteAsset(symbol, amount)
                .onStart { _state.update { it.copy(isLoading = true) } }
                .collect { result ->
                    when (result) {
                        is Result.Success -> _state.update { it.copy(portfolioItems = it.portfolioItems.filter { it.symbol != symbol }) }
                        is Result.Error -> {
                            _events.send(PortfolioEvent.Error(result.error))
                            _state.update { it.copy(isLoading = false) }
                        }
                        is Result.Loading -> _state.update { it.copy(isLoading = true) }
                    }
                }
        }

    }


//    fun addAsset(symbol: String, amount: Double){
//        viewModelScope.launch {
//
//
//            if (coinsList.isEmpty()) {
//                coinDataSource.getCoins().onSuccess {
//                    coinsList = it
//                }.onError { return@onError }
//            }
//
//            val priceUsd = coinsList.find { it.symbol == symbol }?.priceUsd ?: return@launch
//
//
//            val newItem = PortfolioItem(
//                symbol = symbol,
//                amount = amount.toDisplayableNumber(),
//                priceUsd = priceUsd.toDisplayableNumber(),
//                value = (amount * priceUsd).toDisplayableNumber(),
//                iconRes = getDrawableIdForCoin(symbol)
//            )
//
//            // Add operation to the database
//            val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//
//
//            operationRepository.addOperation(
//                OperationItem(
//                    coinId = symbolToCoinId(symbol, coinsList),
//                    symbol = symbol,
//                    amount = amount,
//                    date = formattedDate.toLocalDate(),
//                    type = "add"
//                )
//            )
//
//            // Update portfolioItems in state
//            _state.update { currentState ->
//                currentState.copy(
//                    portfolioItems = currentState.portfolioItems + newItem
//                )
//            }
//
//        }
//    }

//    fun editAsset(symbol: String, initialAmount: Double, newAmount: Double) {
//        val changeAmount = newAmount - initialAmount
//        val operationType = if (changeAmount > 0) "add" else "subtract"
//        val absoluteChangeAmount = kotlin.math.abs(changeAmount)
//
//
//        val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//
//        // If there is a change, record it in the database
//        if (absoluteChangeAmount > 0) {
//            viewModelScope.launch {
//
//                if (coinsList.isEmpty()) {
//                    coinDataSource.getCoins().onSuccess {
//                        coinsList = it
//                    }.onError {
//                        return@onError
//                    }
//                }
//                operationRepository.addOperation(
//                    OperationItem(
//                        coinId = symbolToCoinId(symbol, coinsList),
//                        symbol = symbol,
//                        amount = absoluteChangeAmount,
//                        date = formattedDate.toLocalDate(),
//                        type = operationType
//                    )
//                )
//            }
//        }
//
//        // Update UI state
//        _state.update { currentState ->
//            val updatedItems = currentState.portfolioItems.map {
//                if (it.symbol == symbol) {
//                    val newValue = it.priceUsd.value * newAmount
//                    it.copy(
//                        amount = newAmount.toDisplayableNumber(),
//                        value = newValue.toDisplayableNumber()
//                    )
//                } else it
//            }
//            currentState.copy(portfolioItems = updatedItems)
//        }
//    }




//    fun deleteAsset(symbol: String, amount: Double) {
//
//        viewModelScope.launch {
//
//            if (coinsList.isEmpty()) {
//                coinDataSource.getCoins().onSuccess {
//                    coinsList = it
//                }.onError {
//                    return@onError
//                }
//            }
//            operationRepository.addOperation(
//                OperationItem(
//                    coinId = symbolToCoinId(symbol, coinsList),
//                    symbol = symbol,
//                    amount = amount,
//                    date = LocalDate.now(),
//                    type = "subtract"
//                )
//            )
//        }
//
//
//        _state.update { currentState ->
//            val updatedItems = currentState.portfolioItems.filter { it.symbol != symbol }
//            currentState.copy(portfolioItems = updatedItems)
//        }
//    }


    // Function to insert test data
    fun insertTestData() {
        viewModelScope.launch {
            // Insert test operations

            // Insert test historical prices
//            priceRepository.addHistoricalPrice(
//                HistoricalPrice(
//                    coinId = "bitcoin",
//                    date = "2025-02-26".toLocalDate(),
//                    price = 45000.0
//                )
//            )
//            priceRepository.addHistoricalPrice(
//                HistoricalPrice(
//                    coinId = "bitcoin",
//                    date = "2025-02-27".toLocalDate(),
//                    price = 46000.0
//                )
//            )
            operationRepository.addOperation(
                OperationItem(
                    coinId = "bitcoin",
                    symbol = "BTC",
                    amount = 1.0,
                    date = "2025-02-01".toLocalDate(),
                    type = "add"
                )
            )
            operationRepository.addOperation(
                OperationItem(
                    coinId = "bitcoin",
                    symbol = "BTC",
                    amount = 0.6,
                    date = "2025-03-02".toLocalDate(),
                    type = "subtract"
                )
            )

            operationRepository.addOperation(
                OperationItem(
                    coinId = "ethereum",
                    symbol = "ETH",
                    amount = 0.6,
                    date = "2025-03-01".toLocalDate(),
                    type = "add"
                )
            )
            operationRepository.addOperation(
                OperationItem(
                    coinId = "ethereum",
                    symbol = "ETH",
                    amount = 0.2,
                    date = "2025-03-03".toLocalDate(),
                    type = "add"
                )
            )


        }
    }



}


