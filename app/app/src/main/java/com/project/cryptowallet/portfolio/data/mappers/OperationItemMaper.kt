package com.project.cryptowallet.portfolio.data.mappers

import com.project.cryptowallet.portfolio.data.local.OperationItemEntity
import com.project.cryptowallet.core.domain.models.OperationItem
import com.project.cryptowallet.core.domain.util.toLocalDate

fun OperationItemEntity.toOperationItem(): OperationItem {



    return OperationItem(
        coinId = coinId,
        symbol = symbol,
        amount = amount,
        date = date.toLocalDate(),
        type = type
    )
}

fun OperationItem.toOperationItemEntity(): OperationItemEntity {
    return OperationItemEntity(
        coinId = coinId,
        symbol = symbol,
        amount = amount,
        date = date.toString(),
        type = type
    )
}