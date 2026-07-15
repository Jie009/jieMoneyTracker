package com.budgettracker.core.model

import java.time.Instant

data class Transaction(
    val id: String,
    val cashbookId: String,
    val categoryId: String?,
    val amount: Money,
    val type: TransactionType,
    val dateTime: Instant,
    val note: String? = null,
    val source: TransactionSource = TransactionSource.Manual,
    val originalSourceId: String? = null,
    val importBatchId: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
