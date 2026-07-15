package com.budgettracker.core.database.mapper

import com.budgettracker.core.database.entity.BudgetEntity
import com.budgettracker.core.model.Budget
import com.budgettracker.core.model.CurrencyCode
import com.budgettracker.core.model.Money

fun BudgetEntity.asExternalModel(): Budget = Budget(
    id = id,
    cashbookId = cashbookId,
    categoryId = categoryId,
    month = month,
    amount = Money(
        minorUnits = amountMinor,
        currency = CurrencyCode.valueOf(currencyCode),
    ),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Budget.asEntity(): BudgetEntity = BudgetEntity(
    id = id,
    cashbookId = cashbookId,
    categoryId = categoryId,
    month = month,
    amountMinor = amount.minorUnits,
    currencyCode = amount.currency.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
