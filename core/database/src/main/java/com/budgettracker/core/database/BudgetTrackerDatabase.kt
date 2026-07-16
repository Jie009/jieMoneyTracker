package com.budgettracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.budgettracker.core.database.dao.BudgetDao
import com.budgettracker.core.database.dao.CashbookDao
import com.budgettracker.core.database.dao.CategoryDao
import com.budgettracker.core.database.dao.RecurringTransactionDao
import com.budgettracker.core.database.dao.TransactionDao
import com.budgettracker.core.database.entity.BudgetEntity
import com.budgettracker.core.database.entity.CashbookEntity
import com.budgettracker.core.database.entity.CategoryEntity
import com.budgettracker.core.database.entity.RecurringTransactionEntity
import com.budgettracker.core.database.entity.TransactionEntity

@Database(
    entities = [
        CashbookEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        RecurringTransactionEntity::class,
        BudgetEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
abstract class BudgetTrackerDatabase : RoomDatabase() {
    abstract fun cashbookDao(): CashbookDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun budgetDao(): BudgetDao
}
