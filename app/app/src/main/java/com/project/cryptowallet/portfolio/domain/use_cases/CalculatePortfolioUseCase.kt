package com.project.cryptowallet.portfolio.domain.use_cases

import com.project.cryptowallet.core.domain.models.HistoricalPrice
import com.project.cryptowallet.core.domain.util.DbError
import com.project.cryptowallet.core.domain.util.Error
import com.project.cryptowallet.portfolio.presentation.PortfolioState
import kotlinx.coroutines.flow.Flow
import com.project.cryptowallet.core.domain.util.Result
import com.project.cryptowallet.core.domain.util.toLocalDate
import com.project.cryptowallet.core.presentation.util.getDrawableIdForCoin
import com.project.cryptowallet.crypto_list.presentation.models.toDisplayableNumber
import com.project.cryptowallet.portfolio.domain.OperationRepository
import com.project.cryptowallet.portfolio.domain.PriceRepository
import com.project.cryptowallet.portfolio.presentation.PortfolioEvent
import com.project.cryptowallet.portfolio.presentation.models.PieChartData
import com.project.cryptowallet.portfolio.presentation.models.PortfolioItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate

class CalculatePortfolioUseCase(
    private val operationRepository: OperationRepository,
    private val priceRepository: PriceRepository
) {
    operator fun invoke(): Flow<Result<PortfolioState, Error>> = flow {

        emit(Result.Loading(true)) // Emit loading state

        val operationsResult = operationRepository.getAllOperations()
        if (operationsResult is Result.Error) {
            emit(Result.Error(operationsResult.error))
            return@flow
        }

        val operations = (operationsResult as Result.Success).data
        val operationsByDate = operations.groupBy { it.date }

        val historicalPrices = mutableMapOf<String, List<HistoricalPrice>>()
        val coinIds = operations.map { it.coinId }.toSet()
        val coinIdsAndSymbols = operations.associate { it.coinId to it.symbol }
        val latestPriceDateForCoins = mutableMapOf<String, String>()
        val today = LocalDate.now()

        for (coinId in coinIds) {
            val firstOperationDateResult = operationRepository.getOldestOperationDateForCoin(coinId)
            if (firstOperationDateResult is Result.Error) {
                emit(Result.Error(firstOperationDateResult.error))
                continue
            }
            val firstOperationDate = (firstOperationDateResult as Result.Success).data

            val latestPriceDateResult = priceRepository.getLatestDateForCoin(coinId)
            val latestPriceDate = (latestPriceDateResult as? Result.Success)?.data

            latestPriceDateForCoins[coinId] = latestPriceDate ?: today.toString()

            val pricesResult = priceRepository.getHistoricalPrice(coinId)
            if (pricesResult is Result.Error) {
                emit(Result.Error(pricesResult.error))
                continue
            }
            var prices = (pricesResult as Result.Success).data.toMutableList()

            if (prices.isEmpty() && firstOperationDate != null) {
                val syncResult = priceRepository.syncHistoricalPrice(coinId, firstOperationDate.toLocalDate())
                if (syncResult is Result.Error) {
                    emit(Result.Error(syncResult.error))
                    continue
                }
                prices = (syncResult as Result.Success).data.toMutableList()
            }

            if (prices.isNotEmpty() && latestPriceDate != null && latestPriceDate < today.minusDays(1).toString()
            ) {
                val syncResult = priceRepository.syncHistoricalPrice(
                    coinId,
                    latestPriceDate.toLocalDate().plusDays(1)
                )
                if (syncResult is Result.Error) {
                    emit(Result.Error(syncResult.error))
                    continue
                }

                prices = (syncResult as Result.Success).data.toMutableList()
            }

            val latestHistoricalDate = prices.maxByOrNull { it.date }?.date
            if (latestHistoricalDate == null || latestHistoricalDate.toString() != today.toString()) {
                val latestPriceResult = priceRepository.getLatestCoinFromApi(coinId)
                when (latestPriceResult) {
                    is Result.Error -> {
                        emit(Result.Error(latestPriceResult.error))
                        continue
                    }

                    is Result.Success -> {
                        val latestPrice = latestPriceResult.data
                        prices.add(latestPrice)
                    }

                    is Result.Loading -> {
                        emit(Result.Loading(true))
                        continue
                    }
                }
            }


            historicalPrices[coinId] = prices
        }

        val portfolioValueMap = mutableMapOf<LocalDate, Double>()
        val coinAmounts = mutableMapOf<String, Double>()

        val sortedDates = operationsByDate.keys.sorted()
        val firstDate = sortedDates.firstOrNull() ?: today
        val allDates = generateSequence(firstDate) { it.plusDays(1) }
            .takeWhile { it <= today }
            .toList()

        for (date in allDates) {
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

            var portfolioValue = portfolioValueMap[date] ?: 0.0
            for ((coinId, amount) in coinAmounts) {
                val pricesForCoin = historicalPrices[coinId] ?: emptyList()
                val priceForDate = pricesForCoin.find { it.date == date }?.price ?: 0.0
                portfolioValue += amount * priceForDate
            }

            portfolioValueMap[date] = portfolioValue
        }

        val portfolioItems = mutableListOf<PortfolioItem>()
        val pieChartData = mutableListOf<PieChartData>()

        for ((coinId, amount) in coinAmounts) {
            if (amount == 0.0) continue
            val pricesForCoin = historicalPrices[coinId] ?: emptyList()
            val priceForDate = pricesForCoin.find { it.date == today }?.price ?: 0.0
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

        emit(
            Result.Success(
                PortfolioState(
                    portfolioItems = portfolioItems,
                    pieChartData = pieChartData,
                    portfolioValueMap = portfolioValueMap
                )
            )
        )
    }.catch { e ->
        emit(Result.Error(DbError.UNKNOWN))
    }.flowOn(Dispatchers.IO)
}
