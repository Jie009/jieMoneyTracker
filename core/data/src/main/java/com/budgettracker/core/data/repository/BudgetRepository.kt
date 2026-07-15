package com.budgettracker.core.data.repository

import com.budgettracker.core.model.Budget
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BudgetRepository {
    fun observeBudgets(cashbookId: String, month: YearMonth): Flow<List<Budget>>
    suspend fun upsertBudget(budget: Budget)
    suspend fun deleteBudget(id: String)
}
