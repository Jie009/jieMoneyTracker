package com.budgettracker.feature.home.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.core.data.repository.CashbookRepository
import com.budgettracker.core.data.repository.CategoryRepository
import com.budgettracker.core.data.repository.TransactionRepository
import com.budgettracker.core.domain.money.AmountFormatter
import com.budgettracker.core.domain.report.MonthlySummaryCalculator
import com.budgettracker.core.model.Category
import com.budgettracker.core.model.Cashbook
import com.budgettracker.core.model.Transaction
import com.budgettracker.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
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
) : ViewModel() {
    private val cashbooks = cashbookRepository.observeActiveCashbooks()
    private val selectedCashbook = cashbooks.map { cashbooks -> cashbooks.firstOrNull() }
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
        val currentMonth = YearMonth.now()
        val currentMonthTransactions = transactions.filter { transaction ->
            YearMonth.from(transaction.dateTime.atZone(ZoneId.systemDefault()).toLocalDate()) == currentMonth
        }
        val summary = MonthlySummaryCalculator.calculate(currentMonthTransactions)
        val categoryById = categories.associateBy { it.id }

        HomeUiState(
            monthlySummary = MonthlySummaryUiModel(
                year = currentMonth.year.toString(),
                month = currentMonth.month.name.take(3).lowercase().replaceFirstChar(Char::titlecase),
                expenses = AmountFormatter.formatPlain(summary.expense),
                income = AmountFormatter.formatPlain(summary.income),
                balance = AmountFormatter.formatPlain(summary.balance),
                currency = summary.balance.currency.name,
                cashbookName = cashbook?.name ?: "No cashbook",
                remainingBudget = "Not set",
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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

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
        createdAt = localDateTime,
        icon = iconFor(category?.icon),
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

private fun iconFor(name: String?): ImageVector =
    when (name) {
        "restaurant" -> Icons.Filled.Restaurant
        "fastfood" -> Icons.Filled.Fastfood
        "local_cafe" -> Icons.Filled.LocalCafe
        "directions_car" -> Icons.Filled.DirectionsCar
        "local_gas_station" -> Icons.Filled.LocalGasStation
        "local_parking" -> Icons.Filled.LocalParking
        "shopping_bag" -> Icons.Filled.ShoppingBag
        "subscriptions" -> Icons.Filled.Subscriptions
        "medical_services" -> Icons.Filled.MedicalServices
        "home" -> Icons.Filled.Home
        "payments" -> Icons.Filled.Payments
        "redeem" -> Icons.Filled.Redeem
        "undo" -> Icons.AutoMirrored.Filled.Undo
        "more_horiz" -> Icons.Filled.MoreHoriz
        else -> Icons.Filled.Category
    }

private fun List<HomeTransactionUiModel>.withDaySummaries(): List<HomeTransactionUiModel> {
    val summariesByDate = groupBy { it.date }.mapValues { (_, transactions) ->
        val income = transactions
            .filter { it.isIncome }
            .sumOf { it.amount.extractMinorUnits() }
        val expense = transactions
            .filterNot { it.isIncome }
            .sumOf { it.amount.extractMinorUnits() }
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

private fun String.extractMinorUnits(): Long {
    val numeric = filter { it.isDigit() || it == '.' }
    return runCatching { AmountFormatter.parseMinorUnits(numeric) }.getOrDefault(0L)
}

private fun Long.formatMinor(): String =
    AmountFormatter.formatPlain(com.budgettracker.core.model.Money(this))

private val HomeDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
