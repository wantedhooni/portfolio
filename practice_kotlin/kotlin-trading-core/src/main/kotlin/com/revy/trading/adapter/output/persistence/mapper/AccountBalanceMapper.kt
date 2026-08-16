package com.revy.trading.adapter.output.persistence.mapper

import com.revy.trading.adapter.output.persistence.entity.AccountBalanceEntity
import com.revy.trading.domain.balance.AccountBalance
import com.revy.trading.domain.common.Money

fun AccountBalanceEntity.toDomain(): AccountBalance = AccountBalance(
    accountId = accountId,
    available = Money(availableAmount, currency),
    reserved = Money(reservedAmount, currency),
)

fun AccountBalance.toEntity(): AccountBalanceEntity = AccountBalanceEntity(
    accountId = accountId,
    availableAmount = available().amount,
    reservedAmount = reserved().amount,
    currency = available().currency,
)
