package com.budgettracker.core.model

import java.time.Instant
import java.time.LocalDate

enum class RecurringFrequency {
    Daily,
    Weekly,
    Monthly,
    Yearly,
}

data class RecurringTransaction(
    val id: String,
    val cashbookId: String,
    val name: String,
    val categoryId: String?,
    val amount: Money,
    val type: TransactionType,
    val frequency: RecurringFrequency,
    val interval: Int,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val maxOccurrences: Int? = null,
    val generatedOccurrences: Int = 0,
    val nextRunDate: LocalDate,
    val requireConfirmation: Boolean,
    val isPaused: Boolean = false,
    val note: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
