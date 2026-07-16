package com.budgettracker.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.budgettracker.core.database.BudgetTrackerDatabase
import com.budgettracker.core.database.dao.BudgetDao
import com.budgettracker.core.database.dao.CashbookDao
import com.budgettracker.core.database.dao.CategoryDao
import com.budgettracker.core.database.dao.RecurringTransactionDao
import com.budgettracker.core.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "budget_tracker.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideBudgetTrackerDatabase(
        @ApplicationContext context: Context,
    ): BudgetTrackerDatabase = Room.databaseBuilder(
        context,
        BudgetTrackerDatabase::class.java,
        DATABASE_NAME,
    )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()

    @Provides
    fun provideCashbookDao(database: BudgetTrackerDatabase): CashbookDao = database.cashbookDao()

    @Provides
    fun provideCategoryDao(database: BudgetTrackerDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: BudgetTrackerDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideRecurringTransactionDao(database: BudgetTrackerDatabase): RecurringTransactionDao =
        database.recurringTransactionDao()

    @Provides
    fun provideBudgetDao(database: BudgetTrackerDatabase): BudgetDao = database.budgetDao()
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.addRecurringTransactionMetadataColumns()
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.addRecurringTransactionMetadataColumns()
    }
}

private fun SupportSQLiteDatabase.addRecurringTransactionMetadataColumns() {
    if (!hasColumn("recurring_transactions", "name")) {
        execSQL(
            """
            ALTER TABLE recurring_transactions
            ADD COLUMN name TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
    }
    if (!hasColumn("recurring_transactions", "maxOccurrences")) {
        execSQL(
            """
            ALTER TABLE recurring_transactions
            ADD COLUMN maxOccurrences INTEGER
            """.trimIndent(),
        )
    }
    if (!hasColumn("recurring_transactions", "generatedOccurrences")) {
        execSQL(
            """
            ALTER TABLE recurring_transactions
            ADD COLUMN generatedOccurrences INTEGER NOT NULL DEFAULT 0
            """.trimIndent(),
        )
    }
    execSQL(
        """
        UPDATE recurring_transactions
        SET name = COALESCE(NULLIF(note, ''), 'Recurring transaction')
        WHERE name = ''
        """.trimIndent(),
    )
}

private fun SupportSQLiteDatabase.hasColumn(tableName: String, columnName: String): Boolean =
    query("PRAGMA table_info(`$tableName`)").use { cursor ->
        val nameColumnIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (cursor.getString(nameColumnIndex) == columnName) return@use true
        }
        false
    }
