package com.budgettracker.core.domain.transaction

import com.budgettracker.core.model.Transaction

object TransactionValidator {
    fun validate(transaction: Transaction) {
        require(transaction.cashbookId.isNotBlank()) { "Cashbook is required." }
        require(transaction.amount.minorUnits > 0) { "Amount must be greater than zero." }
        require(transaction.amount.currency.name.isNotBlank()) { "Currency is required." }
    }
}
