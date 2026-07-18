package com.budgettracker.core.data.repository.impl

import com.budgettracker.core.data.repository.TransactionRepository
import com.budgettracker.core.database.dao.TransactionDao
import com.budgettracker.core.database.mapper.asEntity
import com.budgettracker.core.database.mapper.asExternalModel
import com.budgettracker.core.model.CategoryUsageStats
import com.budgettracker.core.model.Transaction
import com.budgettracker.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class OfflineTransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
) : TransactionRepository {
    override fun observeTransactions(cashbookId: String): Flow<List<Transaction>> =
        transactionDao.observeTransactions(cashbookId).map { transactions ->
            transactions.map { it.asExternalModel() }
        }

    override fun observeTransactionsInRange(
        cashbookId: String,
        startInclusive: Instant,
        endExclusive: Instant,
    ): Flow<List<Transaction>> =
        transactionDao.observeTransactionsInRange(
            cashbookId = cashbookId,
            startInclusive = startInclusive,
            endExclusive = endExclusive,
        ).map { transactions ->
            transactions.map { it.asExternalModel() }
        }

    override fun observeCategoryUsageStats(
        cashbookId: String,
        type: TransactionType,
        dayOfWeek: Int,
        hourOfDay: Int,
    ): Flow<List<CategoryUsageStats>> =
        transactionDao.observeCategoryUsageStats(
            cashbookId = cashbookId,
            type = type.name,
            dayOfWeek = dayOfWeek,
            hourOfDay = hourOfDay,
        ).map { stats ->
            stats.mapNotNull { item ->
                val categoryId = item.categoryId ?: return@mapNotNull null
                val latestTransactionAt = item.latestTransactionAt ?: return@mapNotNull null
                CategoryUsageStats(
                    categoryId = categoryId,
                    transactionCount = item.transactionCount,
                    latestTransactionAt = latestTransactionAt,
                )
            }
        }

    override fun observeTransaction(id: String): Flow<Transaction?> =
        transactionDao.observeTransaction(id).map { it?.asExternalModel() }

    override suspend fun getTransaction(id: String): Transaction? =
        transactionDao.getTransaction(id)?.asExternalModel()

    override suspend fun countTransactionsByCategory(categoryId: String): Int =
        transactionDao.countTransactionsByCategory(categoryId)

    override suspend fun replaceCategory(
        categoryId: String,
        replacementCategoryId: String,
        updatedAt: Instant,
    ) {
        transactionDao.replaceCategory(
            categoryId = categoryId,
            replacementCategoryId = replacementCategoryId,
            updatedAt = updatedAt,
        )
    }

    override suspend fun upsertTransaction(transaction: Transaction) {
        transactionDao.upsertTransaction(transaction.asEntity())
    }

    override suspend fun upsertTransactions(transactions: List<Transaction>) {
        transactionDao.upsertTransactions(transactions.map { it.asEntity() })
    }

    override suspend fun deleteTransaction(id: String) {
        transactionDao.deleteTransaction(id)
    }
}
