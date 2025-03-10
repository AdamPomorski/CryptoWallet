package com.project.cryptowallet.portfolio.presentation

import androidx.compose.runtime.Immutable
import com.project.cryptowallet.portfolio.presentation.models.PieChartData
import com.project.cryptowallet.portfolio.presentation.models.PortfolioItem
import java.time.LocalDate

@Immutable
data class PortfolioState(
    val portfolioValueMap: Map<LocalDate, Double> = emptyMap(),
    val portfolioItems: List<PortfolioItem> = emptyList(),
    val pieChartData: List<PieChartData> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)