package com.project.cryptowallet.portfolio.presentation.models

import androidx.annotation.DrawableRes
import com.project.cryptowallet.crypto_list.presentation.models.DisplayableNumber

data class PortfolioItem(
    val symbol: String,
    val priceUsd: DisplayableNumber,
    val amount: DisplayableNumber,
    val value: DisplayableNumber,
    @DrawableRes val iconRes: Int
)
