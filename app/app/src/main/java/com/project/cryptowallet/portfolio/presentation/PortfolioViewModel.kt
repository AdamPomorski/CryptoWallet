package com.project.cryptowallet.portfolio.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.cryptowallet.core.domain.models.HistoricalPrice
import com.project.cryptowallet.core.domain.models.OperationItem
import com.project.cryptowallet.core.domain.util.Result
import com.project.cryptowallet.core.domain.util.onError
import com.project.cryptowallet.core.domain.util.onSuccess
import com.project.cryptowallet.core.domain.util.toLocalDate
import com.project.cryptowallet.core.presentation.util.getDrawableIdForCoin
import com.project.cryptowallet.core.presentation.util.symbolToCoinId
import com.project.cryptowallet.crypto_list.domain.Coin
import com.project.cryptowallet.crypto_list.domain.CoinDataSource
import com.project.cryptowallet.crypto_list.presentation.models.toDisplayableNumber
import com.project.cryptowallet.portfolio.domain.OperationRepository
import com.project.cryptowallet.portfolio.domain.PriceRepository
import com.project.cryptowallet.portfolio.presentation.models.PieChartData
import com.project.cryptowallet.portfolio.presentation.models.PortfolioItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PortfolioViewModel(
    private val operationRepository: OperationRepository,
    private val priceRepository: PriceRepository,
    private val coinDataSource: CoinDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(PortfolioState())
    val state = _state
        .onStart { calculatePortfolioValueOverTime() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            PortfolioState()
        )

    var coinsList: List<Coin> = emptyList()


    init {
        // Fetch and calculate portfolio values when ViewModel is created
        viewModelScope.launch {
            //operationRepository.deleteAllOperations()
            //priceRepository.deleteAllHistoricalPrices()
            //insertTestData()
            //delay(2000)
//            val portfolioValueMap = calculatePortfolioValueOverTime()
//            _portfolioValueMap.value = portfolioValueMap
        }
    }

    fun refreshPortfolio() {
        viewModelScope.launch {
            _state.update {
                it.copy(isRefreshing = true)
            }
            calculatePortfolioValueOverTime() // Recalculate portfolio data
            _state.update {
                it.copy(isRefreshing = false)
            }
        }
    }


     fun calculatePortfolioValueOverTime() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }

            // Step 1: Fetch all operations from the database
            val operations = operationRepository.getAllOperations()

            // Step 2: Group operations by date
            val operationsByDate = operations.groupBy { it.date }

            // Step 3: Fetch historical prices for all coins
            val historicalPrices = mutableMapOf<String, List<HistoricalPrice>>()
            val coinIds = operations.map { it.coinId }.toSet() // Get unique coin IDs
            val coinIdsAndSymbols = operations.map { it.coinId to it.symbol }.toMap()
            val latestPriceDateForCoins = mutableMapOf<String, String>()

            val today = LocalDate.now()

            for (coinId in coinIds) {
                // Step 1: Get the latest date from operations for this coin
                val firstOperationDate = operationRepository.getOldestOperationDateForCoin(coinId)

                // Step 2: Get the latest date from historical prices for this coin
                val firstPriceDate = priceRepository.getOldestDateForCoin(coinId)

                val latestPriceDate = priceRepository.getLatestDateForCoin(coinId)

                latestPriceDateForCoins[coinId] = latestPriceDate?.toString() ?: LocalDate.now().toString()


                // Step 3: Compare the dates
                if ((firstOperationDate != null && (firstPriceDate == null || firstOperationDate > firstPriceDate)) || (latestPriceDate != null && latestPriceDate < LocalDate.now()
                        .minusDays(1).toString())
                ) {
                    // Sync historical data starting from the latest operation date
                    firstOperationDate?.let {
                        priceRepository.syncHistoricalPrice(
                            coinId,
                            it.toLocalDate()
                        )
                    }
                }

                // Fetch historical prices for this coin
                var prices = priceRepository.getHistoricalPrice(coinId).toMutableList()
                if (prices.isEmpty()) {
                    // If no prices are available, sync historical data without a specific start date
                    firstOperationDate?.let {
                        priceRepository.syncHistoricalPrice(
                            coinId,
                            it.toLocalDate()
                        )
                    }
                    prices = priceRepository.getHistoricalPrice(coinId).toMutableList()
                }
                val latestHistoricalDate = prices.maxByOrNull { it.date }?.date
                if (latestHistoricalDate == null || latestHistoricalDate.toString() != today.toString()) {
                    when (val latestPriceResult = priceRepository.getLatestCoinFromApi(coinId)) {
                        is Result.Success -> {
                            val latestPrice = latestPriceResult.data
                            prices.add(latestPrice)
                        }

                        is Result.Error -> {
                            Log.e(
                                "PortfolioViewModel",
                                "Failed to fetch latest price: ${latestPriceResult.error}"
                            )
                        }
                    }
                }


                // Save the historical prices for this coin
                historicalPrices[coinId] = prices
            }

            // Step 4: Calculate the portfolio value for each day
            val portfolioValueMap = mutableMapOf<LocalDate, Double>()
            val coinAmounts =
                mutableMapOf<String, Double>() // Tracks the current amount of each coin

            // Sort operations by date
            val sortedDates = operationsByDate.keys.sorted()

            val firstDate = sortedDates.firstOrNull() ?: LocalDate.now()

            val allDates = generateSequence(firstDate) { it.plusDays(1) }
                .takeWhile { it <= today }
                .toList()


            for (date in allDates) {
                // Apply operations for this date to update coin amounts
                val operationsForDate = operationsByDate[date] ?: emptyList()
                for (operation in operationsForDate) {
                    val currentAmount = coinAmounts.getOrDefault(operation.coinId, 0.0)
                    val newAmount = when (operation.type) {
                        "add" -> currentAmount + operation.amount
                        "subtract" -> currentAmount - operation.amount
                        else -> currentAmount
                    }
                    coinAmounts[operation.coinId] = newAmount
                }

                // Calculate the portfolio value for this date
                var portfolioValue = portfolioValueMap[date] ?: 0.0 // Start with existing value for this date
                for ((coinId, amount) in coinAmounts) {
                    val pricesForCoin = historicalPrices[coinId] ?: emptyList()
                    val priceForDate = pricesForCoin.find { it.date == date }?.price ?: 0.0
                    portfolioValue += amount * priceForDate
                }

                // Save the portfolio value for this date
                portfolioValueMap[date] = portfolioValue
            }


            // Update the state with the calculated portfolio values
            _state.update {
                it.copy(portfolioValueMap = portfolioValueMap)
            }



            val portfolioItems = mutableListOf<PortfolioItem>()
            val pieChartData = mutableListOf<PieChartData>()
            for ((coinId, amount) in coinAmounts) {
                if(amount == 0.0) continue
                val date = LocalDate.now()
                val pricesForCoin = historicalPrices[coinId] ?: emptyList()
                val priceForDate = pricesForCoin.find { it.date == date }?.price ?: 0.0
                val symbol = coinIdsAndSymbols[coinId] ?: ""
                val value = amount * priceForDate
                val portfolioItem = PortfolioItem(
                    symbol = symbol,
                    priceUsd = priceForDate.toDisplayableNumber(),
                    amount = amount.toDisplayableNumber(),
                    value = value.toDisplayableNumber(),
                    iconRes = getDrawableIdForCoin(symbol)
                )
                portfolioItems.add(portfolioItem)
                pieChartData.add(PieChartData(symbol, value.toFloat()))

            }
            _state.update {
                it.copy(
                    portfolioItems = portfolioItems,
                    pieChartData = pieChartData,
                    isLoading = false
                )

            }


        }
    }

    fun addAsset(symbol: String, amount: Double){
        viewModelScope.launch {


            if (coinsList.isEmpty()) {
                coinDataSource.getCoins().onSuccess {
                    coinsList = it
                }.onError { return@onError }
            }

            val priceUsd = coinsList.find { it.symbol == symbol }?.priceUsd ?: return@launch


            val newItem = PortfolioItem(
                symbol = symbol,
                amount = amount.toDisplayableNumber(),
                priceUsd = priceUsd.toDisplayableNumber(),
                value = (amount * priceUsd).toDisplayableNumber(),
                iconRes = getDrawableIdForCoin(symbol)
            )

            // Add operation to the database
            val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))


            operationRepository.addOperation(
                OperationItem(
                    coinId = symbolToCoinId(symbol, coinsList),
                    symbol = symbol,
                    amount = amount,
                    date = formattedDate.toLocalDate(),
                    type = "add"
                )
            )

            // Update portfolioItems in state
            _state.update { currentState ->
                currentState.copy(
                    portfolioItems = currentState.portfolioItems + newItem
                )
            }

        }
    }

    fun editAsset(symbol: String, initialAmount: Double, newAmount: Double) {
        val changeAmount = newAmount - initialAmount
        val operationType = if (changeAmount > 0) "add" else "subtract"
        val absoluteChangeAmount = kotlin.math.abs(changeAmount)


        val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // If there is a change, record it in the database
        if (absoluteChangeAmount > 0) {
            viewModelScope.launch {

                if (coinsList.isEmpty()) {
                    coinDataSource.getCoins().onSuccess {
                        coinsList = it
                    }.onError {
                        return@onError
                    }
                }
                operationRepository.addOperation(
                    OperationItem(
                        coinId = symbolToCoinId(symbol, coinsList),
                        symbol = symbol,
                        amount = absoluteChangeAmount,
                        date = formattedDate.toLocalDate(),
                        type = operationType
                    )
                )
            }
        }

        // Update UI state
        _state.update { currentState ->
            val updatedItems = currentState.portfolioItems.map {
                if (it.symbol == symbol) {
                    val newValue = it.priceUsd.value * newAmount
                    it.copy(
                        amount = newAmount.toDisplayableNumber(),
                        value = newValue.toDisplayableNumber()
                    )
                } else it
            }
            currentState.copy(portfolioItems = updatedItems)
        }
    }


    fun deleteAsset(symbol: String, amount: Double) {

        viewModelScope.launch {

            if (coinsList.isEmpty()) {
                coinDataSource.getCoins().onSuccess {
                    coinsList = it
                }.onError {
                    return@onError
                }
            }
            operationRepository.addOperation(
                OperationItem(
                    coinId = symbolToCoinId(symbol, coinsList),
                    symbol = symbol,
                    amount = amount,
                    date = LocalDate.now(),
                    type = "subtract"
                )
            )
        }


        _state.update { currentState ->
            val updatedItems = currentState.portfolioItems.filter { it.symbol != symbol }
            currentState.copy(portfolioItems = updatedItems)
        }
    }


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


    // Helper function to calculate the current amount of each coin
    private fun calculateCurrentCoinAmounts(operations: List<OperationItem>): Map<String, Double> {
        val coinAmounts = mutableMapOf<String, Double>()

        for (operation in operations) {
            val symbol = operation.coinId
            val amount = if (operation.type == "add") operation.amount else -operation.amount

            // Update the current amount for the coin
            coinAmounts[symbol] = coinAmounts.getOrDefault(symbol, 0.0) + amount
        }

        return coinAmounts
    }
}


