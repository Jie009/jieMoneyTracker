package com.budgettracker.core.data.di

import com.budgettracker.core.data.repository.BudgetRepository
import com.budgettracker.core.data.repository.CashbookRepository
import com.budgettracker.core.data.repository.CategoryRepository
import com.budgettracker.core.data.repository.RecurringTransactionRepository
import com.budgettracker.core.data.repository.TransactionRepository
import com.budgettracker.core.data.repository.UserPreferencesRepository
import com.budgettracker.core.data.repository.impl.OfflineBudgetRepository
import com.budgettracker.core.data.repository.impl.OfflineCashbookRepository
import com.budgettracker.core.data.repository.impl.OfflineCategoryRepository
import com.budgettracker.core.data.repository.impl.OfflineRecurringTransactionRepository
import com.budgettracker.core.data.repository.impl.OfflineTransactionRepository
import com.budgettracker.core.data.repository.impl.SharedPreferencesUserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindCashbookRepository(
        repository: OfflineCashbookRepository,
    ): CashbookRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        repository: OfflineCategoryRepository,
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        repository: OfflineTransactionRepository,
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindRecurringTransactionRepository(
        repository: OfflineRecurringTransactionRepository,
    ): RecurringTransactionRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        repository: OfflineBudgetRepository,
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        repository: SharedPreferencesUserPreferencesRepository,
    ): UserPreferencesRepository
}
