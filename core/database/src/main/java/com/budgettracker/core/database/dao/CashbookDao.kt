package com.budgettracker.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.budgettracker.core.database.entity.CashbookEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface CashbookDao {
    @Query("SELECT * FROM cashbooks WHERE isArchived = 0 ORDER BY isDefault DESC, name ASC")
    fun observeActiveCashbooks(): Flow<List<CashbookEntity>>

    @Query("SELECT * FROM cashbooks WHERE isArchived = 0 ORDER BY isDefault DESC, name ASC")
    suspend fun getActiveCashbooks(): List<CashbookEntity>

    @Query("SELECT * FROM cashbooks WHERE id = :id")
    fun observeCashbook(id: String): Flow<CashbookEntity?>

    @Upsert
    suspend fun upsertCashbook(cashbook: CashbookEntity)

    @Query("SELECT COUNT(*) FROM cashbooks")
    suspend fun getCashbookCount(): Int

    @Query("UPDATE cashbooks SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveCashbook(id: String, updatedAt: Instant)

    @Query("DELETE FROM cashbooks WHERE id = :id")
    suspend fun deleteCashbook(id: String)
}
