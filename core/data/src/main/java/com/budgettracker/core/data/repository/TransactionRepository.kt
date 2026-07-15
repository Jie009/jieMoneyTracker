package com.budgettracker.core.data.repository

import com.budgettracker.core.model.Transaction
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
    suspend fun getTransaction(id: String): Transaction?
    suspend fun upsertTransaction(transaction: Transaction)
    suspend fun upsertTransactions(transactions: List<Transaction>)
    suspend fun deleteTransaction(id: String)
}
