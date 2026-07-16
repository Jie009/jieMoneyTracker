package com.budgettracker.feature.addtransaction.add

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
import com.budgettracker.core.model.CurrencyCode
import com.budgettracker.core.model.Money
import com.budgettracker.core.model.Transaction
import com.budgettracker.core.model.TransactionSource
import com.budgettracker.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
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

    fun load(transactionId: String?) {
        if (_uiState.value.loadedTransactionId == transactionId && _uiState.value.cashbookId != null) return

        viewModelScope.launch {
            val cashbook = cashbookRepository.getSelectedCashbook() ?: return@launch
            cashbookId.value = cashbook.id

            val existingTransaction = transactionId?.let { transactionRepository.getTransaction(it) }
            if (existingTransaction == null) {
                val firstCategory = categoryRepository
                    .getCategoriesByType(cashbook.id, TransactionType.Expense)
                    .firstOrNull()
                _uiState.value = AddTransactionUiState(
                    loadedTransactionId = transactionId,
                    cashbookId = cashbook.id,
                    selectedCategoryId = firstCategory?.id,
                    dateMillis = todayUtcMillis(),
                )
            } else {
                _uiState.value = existingTransaction.asUiState()
            }
        }
    }

    fun updateType(type: TransactionType) {
        _uiState.update { state ->
            state.copy(transactionType = type)
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

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val cashbookId = state.cashbookId ?: return
        val categoryId = state.selectedCategoryId ?: return
        val amountMinor = runCatching { AmountFormatter.parseMinorUnits(state.amountInput) }.getOrNull() ?: return
        if (amountMinor <= 0) return

        viewModelScope.launch {
            val now = Instant.now()
            val existing = state.loadedTransactionId?.let { transactionRepository.getTransaction(it) }
            val transaction = Transaction(
                id = existing?.id ?: "transaction_${UUID.randomUUID()}",
                cashbookId = cashbookId,
                categoryId = categoryId,
                amount = Money(amountMinor, CurrencyCode.MYR),
                type = state.transactionType,
                dateTime = state.dateMillis.toInstantAtSystemStartOfDay(),
                note = state.note.trim().ifBlank { null },
                source = existing?.source ?: TransactionSource.Manual,
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
            dateMillis = dateMillis,
        )
    }
}

data class AddTransactionUiState(
    val loadedTransactionId: String? = null,
    val cashbookId: String? = null,
    val selectedCategoryId: String? = null,
    val amountInput: String = "",
    val transactionType: TransactionType = TransactionType.Expense,
    val note: String = "",
    val dateMillis: Long = todayUtcMillis(),
)

private fun Long.toInstantAtSystemStartOfDay(): Instant =
    Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()

private fun todayUtcMillis(): Long =
    LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
