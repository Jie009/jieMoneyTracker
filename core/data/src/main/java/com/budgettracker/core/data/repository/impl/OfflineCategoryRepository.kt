package com.budgettracker.core.data.repository.impl

import com.budgettracker.core.data.repository.CategoryRepository
import com.budgettracker.core.database.dao.CategoryDao
import com.budgettracker.core.database.mapper.asEntity
import com.budgettracker.core.database.mapper.asExternalModel
import com.budgettracker.core.model.Category
import com.budgettracker.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class OfflineCategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
) : CategoryRepository {
    override fun observeCategories(cashbookId: String): Flow<List<Category>> =
        categoryDao.observeCategories(cashbookId).map { categories ->
            categories.map { it.asExternalModel() }
        }

    override fun observeCategoriesByType(
        cashbookId: String,
        type: TransactionType,
    ): Flow<List<Category>> =
        categoryDao.observeCategoriesByType(
            cashbookId = cashbookId,
            type = type.name,
        ).map { categories ->
            categories.map { it.asExternalModel() }
        }

    override suspend fun getCategoriesByType(
        cashbookId: String,
        type: TransactionType,
    ): List<Category> =
        categoryDao.getCategoriesByType(
            cashbookId = cashbookId,
            type = type.name,
        ).map { it.asExternalModel() }

    override fun observeCategory(id: String): Flow<Category?> =
        categoryDao.observeCategory(id).map { it?.asExternalModel() }

    override suspend fun upsertCategory(category: Category) {
        categoryDao.upsertCategory(category.asEntity())
    }

    override suspend fun upsertCategories(categories: List<Category>) {
        categoryDao.upsertCategories(categories.map { it.asEntity() })
    }

    override suspend fun archiveCategory(id: String, updatedAt: Instant) {
        categoryDao.archiveCategory(id = id, updatedAt = updatedAt)
    }
}
