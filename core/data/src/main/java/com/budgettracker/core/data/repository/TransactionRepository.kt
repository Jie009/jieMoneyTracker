package com.budgettracker.core.data.repository

import com.budgettracker.core.model.CategoryUsageStats
import com.budgettracker.core.model.Transaction
import com.budgettracker.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface TransactionRepository {
    fun observeTransactions(cashbookId: String): Flow<List<Transaction>>
    fun observeTransactionsInRange(
        cashbookId: String,
        startInclusive: Instant,
        endExclusive: Instant,
    ): Flow<List<Transaction>>

    fun observeTransaction(id: String): Flow<Transaction?>
    fun observeCategoryUsageStats(
        cashbookId: String,
        type: TransactionType,
        dayOfWeek: Int,
        hourOfDay: Int,
    ): Flow<List<CategoryUsageStats>>
    suspend fun getTransaction(id: String): Transaction?
    suspend fun countTransactionsByCategory(categoryId: String): Int
    suspend fun replaceCategory(
        categoryId: String,
        replacementCategoryId: String,
        updatedAt: Instant,
    )
    suspend fun upsertTransaction(transaction: Transaction)
    suspend fun upsertTransactions(transactions: List<Transaction>)
    suspend fun deleteTransaction(id: String)
}
