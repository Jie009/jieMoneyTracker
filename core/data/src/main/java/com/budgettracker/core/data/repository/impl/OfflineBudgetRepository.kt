package com.budgettracker.core.data.repository.impl

import com.budgettracker.core.data.repository.BudgetRepository
import com.budgettracker.core.database.dao.BudgetDao
import com.budgettracker.core.database.mapper.asEntity
import com.budgettracker.core.database.mapper.asExternalModel
import com.budgettracker.core.model.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class OfflineBudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
) : BudgetRepository {
    override fun observeBudgets(cashbookId: String, month: YearMonth): Flow<List<Budget>> =
        budgetDao.observeBudgets(cashbookId = cashbookId, month = month).map { budgets ->
            budgets.map { it.asExternalModel() }
        }

    override suspend fun upsertBudget(budget: Budget) {
        budgetDao.upsertBudget(budget.asEntity())
    }

    override suspend fun deleteBudget(id: String) {
        budgetDao.deleteBudget(id)
    }
}
