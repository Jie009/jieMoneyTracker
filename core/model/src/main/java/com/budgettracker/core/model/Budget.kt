package com.budgettracker.core.model

import java.time.Instant
import java.time.YearMonth

data class Budget(
    val id: String,
    val cashbookId: String,
    val categoryId: String?,
    val month: YearMonth,
    val amount: Money,
    val createdAt: Instant,
    val updatedAt: Instant,
)
