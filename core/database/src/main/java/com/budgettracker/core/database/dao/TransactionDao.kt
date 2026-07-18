package com.budgettracker.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.budgettracker.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT * FROM transactions
        WHERE cashbookId = :cashbookId
        ORDER BY dateTime DESC, createdAt DESC
        """,
    )
    fun observeTransactions(cashbookId: String): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE cashbookId = :cashbookId AND dateTime >= :startInclusive AND dateTime < :endExclusive
        ORDER BY dateTime DESC, createdAt DESC
        """,
    )
    fun observeTransactionsInRange(
        cashbookId: String,
        startInclusive: Instant,
        endExclusive: Instant,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT
            categoryId,
            SUM(
                CASE
                    WHEN CAST(
                        strftime(
                            '%H',
                            CASE
                                WHEN CAST(strftime('%H', dateTime, 'localtime') AS INTEGER) = 0
                                    AND CAST(strftime('%M', dateTime, 'localtime') AS INTEGER) = 0
                                THEN createdAt
                                ELSE dateTime
                            END,
                            'localtime'
                        ) AS INTEGER
                    ) = :hourOfDay
                    THEN 1000000
                    ELSE 1
                END
            ) AS transactionCount,
            MAX(
                CASE
                    WHEN CAST(strftime('%H', dateTime, 'localtime') AS INTEGER) = 0
                        AND CAST(strftime('%M', dateTime, 'localtime') AS INTEGER) = 0
                    THEN createdAt
                    ELSE dateTime
                END
            ) AS latestTransactionAt
        FROM transactions
        WHERE cashbookId = :cashbookId
            AND type = :type
            AND categoryId IS NOT NULL
            AND CAST(strftime('%w', dateTime, 'localtime') AS INTEGER) = :dayOfWeek
        GROUP BY categoryId
        ORDER BY transactionCount DESC, latestTransactionAt DESC
        """,
    )
    fun observeCategoryUsageStats(
        cashbookId: String,
        type: String,
        dayOfWeek: Int,
        hourOfDay: Int,
    ): Flow<List<TransactionCategoryUsageStats>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeTransaction(id: String): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransaction(id: String): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countTransactionsByCategory(categoryId: String): Int

    @Query(
        """
        UPDATE transactions
        SET categoryId = :replacementCategoryId, updatedAt = :updatedAt
        WHERE categoryId = :categoryId
        """,
    )
    suspend fun replaceCategory(
        categoryId: String,
        replacementCategoryId: String,
        updatedAt: Instant,
    )

    @Upsert
    suspend fun upsertTransaction(transaction: TransactionEntity)

    @Upsert
    suspend fun upsertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)
}

data class TransactionCategoryUsageStats(
    val categoryId: String?,
    val transactionCount: Long,
    val latestTransactionAt: Instant?,
)
