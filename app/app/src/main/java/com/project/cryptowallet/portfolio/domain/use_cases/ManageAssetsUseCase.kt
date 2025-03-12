package com.project.cryptowallet.portfolio.domain.use_cases

import androidx.lifecycle.viewModelScope
import com.project.cryptowallet.core.domain.models.OperationItem
import com.project.cryptowallet.core.domain.util.Error
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
import com.project.cryptowallet.portfolio.presentation.PortfolioState
import com.project.cryptowallet.portfolio.presentation.models.PortfolioItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ManageAssetsUseCase(
    private val operationRepository: OperationRepository,
    private val coinDataSource: CoinDataSource
) {
    private var coinsList: List<Coin> = emptyList()

    suspend fun addAsset(symbol: String, amount: Double): Flow<Result<PortfolioItem, Error>> {
        // Extract logic for adding assets
        return flow {
            if (coinsList.isEmpty()) {
                coinDataSource.getCoins().onSuccess {
                    coinsList = it
                }.onError {
                    emit(Result.Error(it))
                    return@onError
                }
            }

            val priceUsd = coinsList.find { it.symbol == symbol }?.priceUsd ?: return@flow


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
            emit(Result.Success(newItem))

        }


    }

    suspend fun editAsset(
        symbol: String,
        initialAmount: Double,
        newAmount: Double
    ): Flow<Result<Boolean, Error>> {
        return flow {
            val changeAmount = newAmount - initialAmount
            val operationType = if (changeAmount > 0) "add" else "subtract"
            val absoluteChangeAmount = kotlin.math.abs(changeAmount)


            val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            // If there is a change, record it in the database
            if (absoluteChangeAmount > 0) {

                if (coinsList.isEmpty()) {
                    coinDataSource.getCoins().onSuccess {
                        coinsList = it
                    }.onError {
                        emit(Result.Error(it))
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
                emit(Result.Success(true))
            } else {
                emit(Result.Success(false))
            }


            // Update UI state


        }
        // Extract logic for editing assets

    }

    suspend fun deleteAsset(symbol: String, amount: Double): Flow<Result<Boolean, Error>> {
        return flow {
            if (coinsList.isEmpty()) {
                coinDataSource.getCoins().onSuccess {
                    coinsList = it
                }.onError {
                    emit(Result.Error(it))
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
            emit(Result.Success(true))
        }
        // Extract logic for deleting assets
    }
}