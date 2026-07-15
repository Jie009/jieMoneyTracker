package com.budgettracker.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.budgettracker.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface CategoryDao {
    @Query(
        """
        SELECT * FROM categories
        WHERE cashbookId = :cashbookId AND isArchived = 0
        ORDER BY sortOrder ASC, name ASC
        """,
    )
    fun observeCategories(cashbookId: String): Flow<List<CategoryEntity>>

    @Query(
        """
        SELECT * FROM categories
        WHERE cashbookId = :cashbookId AND type = :type AND isArchived = 0
        ORDER BY sortOrder ASC, name ASC
        """,
    )
    fun observeCategoriesByType(cashbookId: String, type: String): Flow<List<CategoryEntity>>

    @Query(
        """
        SELECT * FROM categories
        WHERE cashbookId = :cashbookId AND type = :type AND isArchived = 0
        ORDER BY sortOrder ASC, name ASC
        """,
    )
    suspend fun getCategoriesByType(cashbookId: String, type: String): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun observeCategory(id: String): Flow<CategoryEntity?>

    @Upsert
    suspend fun upsertCategory(category: CategoryEntity)

    @Upsert
    suspend fun upsertCategories(categories: List<CategoryEntity>)

    @Query("UPDATE categories SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveCategory(id: String, updatedAt: Instant)
}
