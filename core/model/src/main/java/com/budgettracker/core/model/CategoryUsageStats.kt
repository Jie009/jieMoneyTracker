package com.budgettracker.core.model

import java.time.Instant

data class CategoryUsageStats(
    val categoryId: String,
    val transactionCount: Long,
    val latestTransactionAt: Instant,
)
