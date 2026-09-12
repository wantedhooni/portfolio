package com.revy.batch.chapter06.batch

import com.revy.batch.chapter06.domain.AccountSummary
import com.revy.batch.chapter06.domain.TransactionDao
import org.springframework.batch.infrastructure.item.ItemProcessor

open class TransactionApplierProcessor(
    private val transactionDao: TransactionDao,
) : ItemProcessor<AccountSummary, AccountSummary> {

    override fun process(accountSummary: AccountSummary): AccountSummary? {
        transactionDao
            .getTransactionsByAccountNumber(accountSummary.accountNumber)
            ?.forEach { it ->
                accountSummary.addCurrentBalance(it?.amount ?: 0.toDouble())
            }
        return accountSummary;
    }

}