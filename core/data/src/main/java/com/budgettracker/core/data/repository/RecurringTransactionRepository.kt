package com.budgettracker.core.data.repository

import com.budgettracker.core.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

interface RecurringTransactionRepository {
    fun observeRecurringTransactions(cashbookId: String): Flow<List<RecurringTransaction>>
    suspend fun getDueRecurringTransactions(date: LocalDate): List<RecurringTransaction>
    suspend fun countRecurringTransactionsByCategory(categoryId: String): Int
    suspend fun replaceCategory(
        categoryId: String,
        replacementCategoryId: String,
        updatedAt: Instant,
    )
    suspend fun upsertRecurringTransaction(recurringTransaction: RecurringTransaction)
    suspend fun deleteRecurringTransaction(id: String)
}
