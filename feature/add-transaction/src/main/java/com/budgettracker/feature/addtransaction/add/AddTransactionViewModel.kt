package com.budgettracker.feature.addtransaction.add

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.core.data.repository.CashbookRepository
import com.budgettracker.core.data.repository.CategoryRepository
import com.budgettracker.core.data.repository.TransactionRepository
import com.budgettracker.core.data.repository.UserPreferencesRepository
import com.budgettracker.core.domain.money.AmountFormatter
import com.budgettracker.core.domain.transaction.TransactionValidator
import com.budgettracker.core.model.AmountInputMode
import com.budgettracker.core.model.Category
import com.budgettracker.core.model.CategoryUsageStats
import com.budgettracker.core.model.CurrencyCode
import com.budgettracker.core.model.Money
import com.budgettracker.core.model.Transaction
import com.budgettracker.core.model.TransactionSource
import com.budgettracker.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModel @Inject constructor(
    private val cashbookRepository: CashbookRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val cashbookId = MutableStateFlow<String?>(null)
    private val rankingTimeContext = MutableStateFlow(CategoryRankingTimeContext.now())
    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    val amountInputMode: StateFlow<AmountInputMode> = userPreferencesRepository.observeAmountInputMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AmountInputMode.NormalDecimal,
        )

    val categories = cashbookId.flatMapLatest { id ->
        id?.let { categoryRepository.observeCategories(it) } ?: flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val transactionType = _uiState
        .map { it.transactionType }
        .distinctUntilChanged()

    private val categoryUsageStats = combine(
        cashbookId,
        transactionType,
        rankingTimeContext,
    ) { id, type, context ->
        CategoryUsageQuery(
            cashbookId = id,
            transactionType = type,
            timeContext = context,
        )
    }.flatMapLatest { query ->
        query.cashbookId?.let { id ->
            transactionRepository.observeCategoryUsageStats(
                cashbookId = id,
                type = query.transactionType,
                dayOfWeek = query.timeContext.sqliteDayOfWeek,
                hourOfDay = query.timeContext.hourOfDay,
            )
        } ?: flowOf(emptyList())
    }

    val rankedCategories = combine(
        categories,
        categoryUsageStats,
        transactionType,
    ) { categories, usageStats, type ->
        categories.rankedForTransactionType(
            usageStats = usageStats,
            type = type,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    init {
        viewModelScope.launch {
            while (true) {
                rankingTimeContext.value = CategoryRankingTimeContext.now()
                delay(RankingTimeContextRefreshMillis)
            }
        }
    }

    fun load(
        transactionId: String?,
        prefill: AddTransactionPrefill? = null,
    ) {
        val state = _uiState.value
        rankingTimeContext.value = CategoryRankingTimeContext.now()
        if (
            transactionId != null &&
            prefill == null &&
            state.loadedTransactionId == transactionId &&
            state.cashbookId != null
        ) {
            return
        }

        viewModelScope.launch {
            val cashbook = cashbookRepository.getSelectedCashbook() ?: return@launch
            rankingTimeContext.value = CategoryRankingTimeContext.now()
            cashbookId.value = cashbook.id

            val existingTransaction = transactionId?.let { transactionRepository.getTransaction(it) }
            if (existingTransaction == null) {
                val transactionType = prefill?.transactionType ?: TransactionType.Expense
                _uiState.value = AddTransactionUiState(
                    loadedTransactionId = transactionId,
                    cashbookId = cashbook.id,
                    selectedCategoryId = null,
                    amountInput = prefill?.amountInput.orEmpty(),
                    transactionType = transactionType,
                    note = prefill?.note.orEmpty(),
                    source = prefill?.let { TransactionSource.Notification } ?: TransactionSource.Manual,
                    dateMillis = todayUtcMillis(),
                )
            } else {
                _uiState.value = existingTransaction.asUiState()
            }
        }
    }

    fun updateType(type: TransactionType) {
        _uiState.update { state ->
            state.copy(
                transactionType = type,
                selectedCategoryId = null,
            )
        }
    }

    fun updateAmountInput(value: String) {
        _uiState.update { state -> state.copy(amountInput = value) }
    }

    fun updateSelectedCategory(id: String) {
        _uiState.update { state -> state.copy(selectedCategoryId = id) }
    }

    fun updateNote(note: String) {
        _uiState.update { state -> state.copy(note = note) }
    }

    fun updateDateMillis(dateMillis: Long) {
        _uiState.update { state -> state.copy(dateMillis = dateMillis) }
    }

    fun createCategory(name: String, icon: String, color: String, type: TransactionType) {
        val cashbookId = _uiState.value.cashbookId ?: return
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        viewModelScope.launch {
            val now = Instant.now()
            val category = Category(
                id = "category_${type.name.lowercase()}_${UUID.randomUUID()}",
                cashbookId = cashbookId,
                name = trimmedName,
                type = type,
                icon = icon,
                color = color,
                sortOrder = categories.value.size,
                createdAt = now,
                updatedAt = now,
            )
            categoryRepository.upsertCategory(category)
            _uiState.update { state -> state.copy(selectedCategoryId = category.id) }
        }
    }

    fun updateCategory(id: String, name: String, icon: String, color: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return
        val existingCategory = categories.value.firstOrNull { it.id == id } ?: return

        viewModelScope.launch {
            categoryRepository.upsertCategory(
                existingCategory.copy(
                    name = trimmedName,
                    icon = icon,
                    color = color,
                    updatedAt = Instant.now(),
                ),
            )
        }
    }

    fun reorderCategories(orderedCategoryIds: List<String>) {
        val categoryById = categories.value.associateBy { it.id }
        val now = Instant.now()
        val reorderedCategories = orderedCategoryIds.mapIndexedNotNull { index, id ->
            categoryById[id]?.copy(
                sortOrder = index,
                updatedAt = now,
            )
        }
        if (reorderedCategories.isEmpty()) return

        viewModelScope.launch {
            categoryRepository.upsertCategories(reorderedCategories)
        }
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val cashbookId = state.cashbookId ?: return
        val categoryId = state.selectedCategoryId ?: return
        val amountMinor = runCatching { AmountFormatter.parseMinorUnits(state.amountInput) }.getOrNull() ?: return
        if (amountMinor <= 0) return

        viewModelScope.launch {
            val now = Instant.now()
            val existing = state.loadedTransactionId?.let { transactionRepository.getTransaction(it) }
            val transactionTime = existing?.dateTime?.toLocalTime() ?: LocalTime.now()
            val transaction = Transaction(
                id = existing?.id ?: "transaction_${UUID.randomUUID()}",
                cashbookId = cashbookId,
                categoryId = categoryId,
                amount = Money(amountMinor, CurrencyCode.MYR),
                type = state.transactionType,
                dateTime = state.dateMillis.toInstantAtSystemTime(transactionTime),
                note = state.note.trim().ifBlank { null },
                source = existing?.source ?: state.source,
                originalSourceId = existing?.originalSourceId,
                importBatchId = existing?.importBatchId,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            )
            TransactionValidator.validate(transaction)
            transactionRepository.upsertTransaction(transaction)
            onSaved()
        }
    }

    private fun Transaction.asUiState(): AddTransactionUiState {
        val dateMillis = dateTime
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        return AddTransactionUiState(
            loadedTransactionId = id,
            cashbookId = cashbookId,
            selectedCategoryId = categoryId,
            amountInput = AmountFormatter.formatPlain(amount),
            transactionType = type,
            note = note.orEmpty(),
            source = source,
            dateMillis = dateMillis,
        )
    }
}

@Immutable
data class AddTransactionPrefill(
    val amountInput: String,
    val transactionType: TransactionType,
    val note: String = "",
)

@Immutable
data class AddTransactionUiState(
    val loadedTransactionId: String? = null,
    val cashbookId: String? = null,
    val selectedCategoryId: String? = null,
    val amountInput: String = "",
    val transactionType: TransactionType = TransactionType.Expense,
    val note: String = "",
    val source: TransactionSource = TransactionSource.Manual,
    val dateMillis: Long = todayUtcMillis(),
)

private data class CategoryUsageQuery(
    val cashbookId: String?,
    val transactionType: TransactionType,
    val timeContext: CategoryRankingTimeContext,
)

private data class CategoryRankingTimeContext(
    val sqliteDayOfWeek: Int,
    val hourOfDay: Int,
) {
    companion object {
        fun now(): CategoryRankingTimeContext {
            val now = LocalDateTime.now()
            return CategoryRankingTimeContext(
                sqliteDayOfWeek = now.dayOfWeek.value % 7,
                hourOfDay = now.hour,
            )
        }
    }
}

private fun List<Category>.rankedForTransactionType(
    usageStats: List<CategoryUsageStats>,
    type: TransactionType,
): List<Category> {
    val usageByCategoryId = usageStats.associateBy { it.categoryId }

    return filter { it.type == type }
        .sortedWith(
            compareByDescending<Category> { category ->
                if (usageByCategoryId.containsKey(category.id)) 1 else 0
            }.thenByDescending { category ->
                usageByCategoryId[category.id]?.transactionCount ?: 0L
            }.thenByDescending { category ->
                usageByCategoryId[category.id]?.latestTransactionAt ?: Instant.EPOCH
            }.thenBy { category ->
                category.sortOrder
            }.thenBy { category ->
                category.name.lowercase()
            },
        )
}

private fun Instant.toLocalTime(): LocalTime =
    atZone(ZoneId.systemDefault()).toLocalTime()

private fun Long.toInstantAtSystemTime(time: LocalTime): Instant =
    Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .atTime(time)
        .atZone(ZoneId.systemDefault())
        .toInstant()

private fun todayUtcMillis(): Long =
    LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private const val RankingTimeContextRefreshMillis = 60_000L
