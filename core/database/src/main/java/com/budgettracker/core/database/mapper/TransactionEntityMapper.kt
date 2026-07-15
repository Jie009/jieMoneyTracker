package com.budgettracker.core.database.mapper

import com.budgettracker.core.database.entity.TransactionEntity
import com.budgettracker.core.model.CurrencyCode
import com.budgettracker.core.model.Money
import com.budgettracker.core.model.Transaction
import com.budgettracker.core.model.TransactionSource
import com.budgettracker.core.model.TransactionType

fun TransactionEntity.asExternalModel(): Transaction = Transaction(
    id = id,
    cashbookId = cashbookId,
    categoryId = categoryId,
    amount = Money(
        minorUnits = amountMinor,
        currency = CurrencyCode.valueOf(currencyCode),
    ),
    type = TransactionType.valueOf(type),
    dateTime = dateTime,
    note = note,
    source = TransactionSource.valueOf(source),
    originalSourceId = originalSourceId,
    importBatchId = importBatchId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Transaction.asEntity(): TransactionEntity = TransactionEntity(
    id = id,
    cashbookId = cashbookId,
    categoryId = categoryId,
    amountMinor = amount.minorUnits,
    currencyCode = amount.currency.name,
    type = type.name,
    dateTime = dateTime,
    note = note,
    source = source.name,
    originalSourceId = originalSourceId,
    importBatchId = importBatchId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
