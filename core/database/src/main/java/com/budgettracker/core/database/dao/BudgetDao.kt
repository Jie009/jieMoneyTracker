package com.budgettracker.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.budgettracker.core.database.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

@Dao
interface BudgetDao {
    @Query(
        """
        SELECT * FROM budgets
        WHERE cashbookId = :cashbookId AND month = :month
        ORDER BY categoryId IS NOT NULL, categoryId ASC
        """,
    )
    fun observeBudgets(cashbookId: String, month: YearMonth): Flow<List<BudgetEntity>>

    @Upsert
    suspend fun upsertBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudget(id: String)
}
