package com.budgettracker.core.database.di

import android.content.Context
import androidx.room.Room
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
    ).build()

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
