package com.budgettracker.core.database.mapper

import com.budgettracker.core.database.entity.CashbookEntity
import com.budgettracker.core.model.Cashbook
import com.budgettracker.core.model.CurrencyCode

fun CashbookEntity.asExternalModel(): Cashbook = Cashbook(
    id = id,
    name = name,
    currency = CurrencyCode.valueOf(currencyCode),
    color = color,
    icon = icon,
    isDefault = isDefault,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Cashbook.asEntity(): CashbookEntity = CashbookEntity(
    id = id,
    name = name,
    currencyCode = currency.name,
    color = color,
    icon = icon,
    isDefault = isDefault,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
