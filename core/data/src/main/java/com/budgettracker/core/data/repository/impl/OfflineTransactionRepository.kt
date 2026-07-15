package com.budgettracker.core.data.repository.impl

import com.budgettracker.core.data.repository.TransactionRepository
import com.budgettracker.core.database.dao.TransactionDao
import com.budgettracker.core.database.mapper.asEntity
import com.budgettracker.core.database.mapper.asExternalModel
import com.budgettracker.core.model.Transaction
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

    override fun observeTransaction(id: String): Flow<Transaction?> =
        transactionDao.observeTransaction(id).map { it?.asExternalModel() }

    override suspend fun getTransaction(id: String): Transaction? =
        transactionDao.getTransaction(id)?.asExternalModel()

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
