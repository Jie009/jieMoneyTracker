package com.budgettracker.core.data.repository.impl

import com.budgettracker.core.data.repository.CashbookRepository
import com.budgettracker.core.database.dao.CashbookDao
import com.budgettracker.core.database.mapper.asEntity
import com.budgettracker.core.database.mapper.asExternalModel
import com.budgettracker.core.model.Cashbook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class OfflineCashbookRepository @Inject constructor(
    private val cashbookDao: CashbookDao,
) : CashbookRepository {
    private val selectedCashbookId = MutableStateFlow<String?>(null)

    override fun observeActiveCashbooks(): Flow<List<Cashbook>> =
        cashbookDao.observeActiveCashbooks().map { cashbooks ->
            cashbooks.map { it.asExternalModel() }
        }

    override fun observeSelectedCashbook(): Flow<Cashbook?> =
        combine(selectedCashbookId, observeActiveCashbooks()) { selectedId, cashbooks ->
            cashbooks.selectedOrFallback(selectedId)
        }

    override suspend fun getActiveCashbooks(): List<Cashbook> =
        cashbookDao.getActiveCashbooks().map { it.asExternalModel() }

    override suspend fun getSelectedCashbook(): Cashbook? =
        getActiveCashbooks().selectedOrFallback(selectedCashbookId.value)

    override fun selectCashbook(id: String) {
        selectedCashbookId.value = id
    }

    override fun observeCashbook(id: String): Flow<Cashbook?> =
        cashbookDao.observeCashbook(id).map { it?.asExternalModel() }

    override suspend fun upsertCashbook(cashbook: Cashbook) {
        cashbookDao.upsertCashbook(cashbook.asEntity())
    }

    override suspend fun hasAnyCashbook(): Boolean = cashbookDao.getCashbookCount() > 0

    override suspend fun archiveCashbook(id: String, updatedAt: Instant) {
        cashbookDao.archiveCashbook(id = id, updatedAt = updatedAt)
    }

    override suspend fun deleteCashbook(id: String) {
        cashbookDao.deleteCashbook(id)
    }
}

private fun List<Cashbook>.selectedOrFallback(selectedId: String?): Cashbook? =
    firstOrNull { it.id == selectedId }
        ?: firstOrNull { it.isDefault }
        ?: firstOrNull()
