package com.budgettracker.core.data.repository.impl

import com.budgettracker.core.data.repository.RecurringTransactionRepository
import com.budgettracker.core.database.dao.RecurringTransactionDao
import com.budgettracker.core.database.mapper.asEntity
import com.budgettracker.core.database.mapper.asExternalModel
import com.budgettracker.core.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class OfflineRecurringTransactionRepository @Inject constructor(
    private val recurringTransactionDao: RecurringTransactionDao,
) : RecurringTransactionRepository {
    override fun observeRecurringTransactions(cashbookId: String): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.observeRecurringTransactions(cashbookId).map { recurringTransactions ->
            recurringTransactions.map { it.asExternalModel() }
        }

    override suspend fun getDueRecurringTransactions(date: LocalDate): List<RecurringTransaction> =
        recurringTransactionDao.getDueRecurringTransactions(date).map { it.asExternalModel() }

    override suspend fun upsertRecurringTransaction(recurringTransaction: RecurringTransaction) {
        recurringTransactionDao.upsertRecurringTransaction(recurringTransaction.asEntity())
    }

    override suspend fun deleteRecurringTransaction(id: String) {
        recurringTransactionDao.deleteRecurringTransaction(id)
    }
}
