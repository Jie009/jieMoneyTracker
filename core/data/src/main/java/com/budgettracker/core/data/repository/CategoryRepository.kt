package com.budgettracker.core.data.repository

import com.budgettracker.core.model.Category
import com.budgettracker.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface CategoryRepository {
    fun observeCategories(cashbookId: String): Flow<List<Category>>
    fun observeCategoriesByType(cashbookId: String, type: TransactionType): Flow<List<Category>>
    suspend fun getCategoriesByType(cashbookId: String, type: TransactionType): List<Category>
    fun observeCategory(id: String): Flow<Category?>
    suspend fun upsertCategory(category: Category)
    suspend fun upsertCategories(categories: List<Category>)
    suspend fun archiveCategory(id: String, updatedAt: Instant)
}
