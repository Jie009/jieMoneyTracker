package com.budgettracker.feature.charts.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.core.data.repository.CashbookRepository
import com.budgettracker.core.data.repository.CategoryRepository
import com.budgettracker.core.data.repository.TransactionRepository
import com.budgettracker.core.model.Category
import com.budgettracker.core.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChartsViewModel @Inject constructor(
    cashbookRepository: CashbookRepository,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {
    private val selectedCashbook = cashbookRepository.observeSelectedCashbook()

    private val transactions = selectedCashbook.flatMapLatest { cashbook ->
        cashbook?.let { transactionRepository.observeTransactions(it.id) } ?: flowOf(emptyList())
    }

    private val categories = selectedCashbook.flatMapLatest { cashbook ->
        cashbook?.let { categoryRepository.observeCategories(it.id) } ?: flowOf(emptyList())
    }

    val uiState = combine(
        selectedCashbook,
        transactions,
        categories,
    ) { cashbook, transactions, categories ->
        ChartsUiState(
            cashbookName = cashbook?.name ?: "No cashbook",
            transactions = transactions,
            categories = categories,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChartsUiState(),
    )
}

data class ChartsUiState(
    val cashbookName: String = "",
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
)
