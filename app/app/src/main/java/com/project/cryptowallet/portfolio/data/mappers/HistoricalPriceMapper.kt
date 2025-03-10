package com.project.cryptowallet.portfolio.data.mappers

import com.project.cryptowallet.portfolio.data.local.HistoricalPriceEntity
import com.project.cryptowallet.core.domain.models.HistoricalPrice
import com.project.cryptowallet.core.domain.util.toLocalDate

fun HistoricalPriceEntity.toHistoricalPrice(): HistoricalPrice {



    return HistoricalPrice(
        coinId = coinId,
        date = date.toLocalDate(),
        price = price
    )
}

fun HistoricalPrice.toHistoricalPriceEntity(): HistoricalPriceEntity {
    return HistoricalPriceEntity(
        coinId = coinId,
        date = date.toString(),
        price = price
    )
}
