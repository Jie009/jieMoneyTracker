package com.budgettracker.core.data.repository

import com.budgettracker.core.model.Cashbook
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface CashbookRepository {
    fun observeActiveCashbooks(): Flow<List<Cashbook>>
    fun observeSelectedCashbook(): Flow<Cashbook?>
    suspend fun getActiveCashbooks(): List<Cashbook>
    suspend fun getSelectedCashbook(): Cashbook?
    fun selectCashbook(id: String)
    fun observeCashbook(id: String): Flow<Cashbook?>
    suspend fun upsertCashbook(cashbook: Cashbook)
    suspend fun hasAnyCashbook(): Boolean
    suspend fun archiveCashbook(id: String, updatedAt: Instant)
    suspend fun deleteCashbook(id: String)
}
