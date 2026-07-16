package com.budgettracker.core.database.mapper

import com.budgettracker.core.database.entity.RecurringTransactionEntity
import com.budgettracker.core.model.CurrencyCode
import com.budgettracker.core.model.Money
import com.budgettracker.core.model.RecurringFrequency
import com.budgettracker.core.model.RecurringTransaction
import com.budgettracker.core.model.TransactionType

fun RecurringTransactionEntity.asExternalModel(): RecurringTransaction = RecurringTransaction(
    id = id,
    cashbookId = cashbookId,
    name = name,
    categoryId = categoryId,
    amount = Money(
        minorUnits = amountMinor,
        currency = CurrencyCode.valueOf(currencyCode),
    ),
    type = TransactionType.valueOf(type),
    frequency = RecurringFrequency.valueOf(frequency),
    interval = interval,
    startDate = startDate,
    endDate = endDate,
    maxOccurrences = maxOccurrences,
    generatedOccurrences = generatedOccurrences,
    nextRunDate = nextRunDate,
    requireConfirmation = requireConfirmation,
    isPaused = isPaused,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun RecurringTransaction.asEntity(): RecurringTransactionEntity = RecurringTransactionEntity(
    id = id,
    cashbookId = cashbookId,
    name = name,
    categoryId = categoryId,
    amountMinor = amount.minorUnits,
    currencyCode = amount.currency.name,
    type = type.name,
    frequency = frequency.name,
    interval = interval,
    startDate = startDate,
    endDate = endDate,
    maxOccurrences = maxOccurrences,
    generatedOccurrences = generatedOccurrences,
    nextRunDate = nextRunDate,
    requireConfirmation = requireConfirmation,
    isPaused = isPaused,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
