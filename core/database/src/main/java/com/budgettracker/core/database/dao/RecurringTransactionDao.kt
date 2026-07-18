package com.budgettracker.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.budgettracker.core.database.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

@Dao
interface RecurringTransactionDao {
    @Query(
        """
        SELECT * FROM recurring_transactions
        WHERE cashbookId = :cashbookId
        ORDER BY isPaused ASC, nextRunDate ASC
        """,
    )
    fun observeRecurringTransactions(cashbookId: String): Flow<List<RecurringTransactionEntity>>

    @Query(
        """
        SELECT * FROM recurring_transactions
        WHERE isPaused = 0 AND nextRunDate <= :date
        ORDER BY nextRunDate ASC
        """,
    )
    suspend fun getDueRecurringTransactions(date: LocalDate): List<RecurringTransactionEntity>

    @Upsert
    suspend fun upsertRecurringTransaction(recurringTransaction: RecurringTransactionEntity)

    @Query("SELECT COUNT(*) FROM recurring_transactions WHERE categoryId = :categoryId")
    suspend fun countRecurringTransactionsByCategory(categoryId: String): Int

    @Query(
        """
        UPDATE recurring_transactions
        SET categoryId = :replacementCategoryId, updatedAt = :updatedAt
        WHERE categoryId = :categoryId
        """,
    )
    suspend fun replaceCategory(
        categoryId: String,
        replacementCategoryId: String,
        updatedAt: Instant,
    )

    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun deleteRecurringTransaction(id: String)
}
