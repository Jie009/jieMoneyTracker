package com.budgettracker.core.database.mapper

import com.budgettracker.core.database.entity.CategoryEntity
import com.budgettracker.core.model.Category
import com.budgettracker.core.model.TransactionType

fun CategoryEntity.asExternalModel(): Category = Category(
    id = id,
    cashbookId = cashbookId,
    name = name,
    defaultNameKey = defaultNameKey,
    type = TransactionType.valueOf(type),
    icon = icon,
    color = color,
    sortOrder = sortOrder,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Category.asEntity(): CategoryEntity = CategoryEntity(
    id = id,
    cashbookId = cashbookId,
    name = name,
    defaultNameKey = defaultNameKey,
    type = type.name,
    icon = icon,
    color = color,
    sortOrder = sortOrder,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
