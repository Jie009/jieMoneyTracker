package com.budgettracker.feature.home.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.core.data.recurring.RecurringTransactionProcessor
import com.budgettracker.core.data.repository.CashbookRepository
import com.budgettracker.core.data.repository.CategoryRepository
import com.budgettracker.core.data.repository.TransactionRepository
import com.budgettracker.core.domain.money.AmountFormatter
import com.budgettracker.core.domain.report.MonthlySummaryCalculator
import com.budgettracker.core.model.Category
import com.budgettracker.core.model.Transaction
import com.budgettracker.core.model.TransactionType
import com.budgettracker.core.ui.category.asCategoryIcon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    cashbookRepository: CashbookRepository,
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    private val recurringTransactionProcessor: RecurringTransactionProcessor,
) : ViewModel() {
    private val selectedCashbook = cashbookRepository.observeSelectedCashbook()
    private val currentMonth = YearMonth.now()
    private val transactions = selectedCashbook.flatMapLatest { cashbook ->
        cashbook?.let { transactionRepository.observeTransactions(it.id) } ?: flowOf(emptyList())
    }
    private val currentMonthTransactions = selectedCashbook.flatMapLatest { cashbook ->
        cashbook?.let {
            transactionRepository.observeTransactionsInRange(
                cashbookId = it.id,
                startInclusive = currentMonth.startInstant(),
                endExclusive = currentMonth.plusMonths(1).startInstant(),
            )
        } ?: flowOf(emptyList())
    }
    private val categories = selectedCashbook.flatMapLatest { cashbook ->
        cashbook?.let { categoryRepository.observeCategories(it.id) } ?: flowOf(emptyList())
    }

    val uiState = combine(
        selectedCashbook,
        transactions,
        currentMonthTransactions,
        categories,
    ) { cashbook, transactions, currentMonthTransactions, categories ->
        val summary = MonthlySummaryCalculator.calculate(currentMonthTransactions)
        val categoryById = categories.associateBy { it.id }

        HomeUiState(
            monthlySummary = MonthlySummaryUiModel(
                year = currentMonth.year.toString(),
                month = currentMonth.month.name.take(3).lowercase().replaceFirstChar(Char::titlecase),
                    expenses = AmountFormatter.formatPlainGrouped(summary.expense),
                    income = AmountFormatter.formatPlainGrouped(summary.income),
                    balance = AmountFormatter.formatPlainGrouped(summary.balance),
                currency = summary.balance.currency.name,
                cashbookName = cashbook?.name ?: "No cashbook",
            ),
            transactions = transactions
                .map { transaction ->
                    transaction.asHomeUiModel(
                        category = transaction.categoryId?.let(categoryById::get),
                    )
                }
                .withDaySummaries(),
            isLoading = false,
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            recurringTransactionProcessor.processDueRecurringTransactions()
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
        }
    }
}

data class HomeUiState(
    val monthlySummary: MonthlySummaryUiModel = MonthlySummaryUiModel(),
    val transactions: List<HomeTransactionUiModel> = emptyList(),
    val isLoading: Boolean = true,
)

private fun Transaction.asHomeUiModel(category: Category?): HomeTransactionUiModel {
    val localDateTime = dateTime.atZone(ZoneId.systemDefault()).toLocalDateTime()
    val localDate = localDateTime.toLocalDate()
    val isIncome = type == TransactionType.Income
    return HomeTransactionUiModel(
        id = id,
        date = localDate,
        dayLabel = localDate.asHomeDayLabel(),
        daySummary = "",
        title = category?.name ?: "Uncategorized",
        note = note.orEmpty(),
        amount = AmountFormatter.formatDisplay(
            money = amount,
            signed = true,
            isIncome = isIncome,
        ),
        amountMinor = amount.minorUnits,
        createdAt = localDateTime,
        icon = category?.icon?.asCategoryIcon() ?: "category".asCategoryIcon(),
        categoryColor = category?.color ?: "#64748B",
        isIncome = isIncome,
        showDayHeader = false,
    )
}

private fun LocalDate.asHomeDayLabel(): String =
    when (this) {
        LocalDate.now() -> "Today"
        LocalDate.now().minusDays(1) -> "Yesterday"
        else -> format(HomeDayFormatter)
    }

private fun List<HomeTransactionUiModel>.withDaySummaries(): List<HomeTransactionUiModel> {
    val summariesByDate = groupBy { it.date }.mapValues { (_, transactions) ->
        val income = transactions
            .filter { it.isIncome }
            .sumOf { it.amountMinor }
        val expense = transactions
            .filterNot { it.isIncome }
            .sumOf { it.amountMinor }
        when {
            income > 0 && expense > 0 -> "Income: RM ${income.formatMinor()} · Expenses: RM ${expense.formatMinor()}"
            income > 0 -> "Income: RM ${income.formatMinor()}"
            else -> "Expenses: RM ${expense.formatMinor()}"
        }
    }

    return map { transaction ->
        transaction.copy(daySummary = summariesByDate.getValue(transaction.date))
    }
}

private fun Long.formatMinor(): String =
    AmountFormatter.formatPlainGrouped(com.budgettracker.core.model.Money(this))

private fun YearMonth.startInstant(): Instant =
    atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

private val HomeDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
