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

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeTransaction(id: String): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransaction(id: String): TransactionEntity?

    @Upsert
    suspend fun upsertTransaction(transaction: TransactionEntity)

    @Upsert
    suspend fun upsertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)
}
