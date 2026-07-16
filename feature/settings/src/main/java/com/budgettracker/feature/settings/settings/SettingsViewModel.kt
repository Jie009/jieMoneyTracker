package com.budgettracker.feature.settings.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.core.data.seed.InitialDataSeeder
import com.budgettracker.core.data.repository.CashbookRepository
import com.budgettracker.core.data.repository.CategoryRepository
import com.budgettracker.core.data.repository.RecurringTransactionRepository
import com.budgettracker.core.data.repository.TransactionRepository
import com.budgettracker.core.data.repository.UserPreferencesRepository
import com.budgettracker.core.domain.importexport.LegacyImportDuplicateKey
import com.budgettracker.core.domain.importexport.LegacyImportPreview
import com.budgettracker.core.domain.importexport.LegacyMoneyManagerCsvReader
import com.budgettracker.core.domain.importexport.LegacyMoneyManagerCsvWriter
import com.budgettracker.core.domain.importexport.LegacyMoneyManagerRecord
import com.budgettracker.core.domain.importexport.LegacyMoneyManagerXlsxReader
import com.budgettracker.core.domain.money.AmountFormatter
import com.budgettracker.core.model.AmountInputMode
import com.budgettracker.core.model.Category
import com.budgettracker.core.model.Cashbook
import com.budgettracker.core.model.CurrencyCode
import com.budgettracker.core.model.Money
import com.budgettracker.core.model.RecurringFrequency
import com.budgettracker.core.model.RecurringTransaction
import com.budgettracker.core.model.Transaction
import com.budgettracker.core.model.TransactionSource
import com.budgettracker.core.model.TransactionType
import com.budgettracker.core.ui.category.normalizedCategoryColor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cashbookRepository: CashbookRepository,
    private val categoryRepository: CategoryRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val initialDataSeeder: InitialDataSeeder,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val amountInputMode: StateFlow<AmountInputMode> = userPreferencesRepository.observeAmountInputMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AmountInputMode.NormalDecimal,
        )

    private val selectedCashbook = cashbookRepository.observeSelectedCashbook()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private val activeCashbooks = cashbookRepository.observeActiveCashbooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val cashbookUiState: StateFlow<CashbookUiState> = combine(
        activeCashbooks,
        selectedCashbook,
    ) { cashbooks, selected ->
        CashbookUiState(
            cashbooks = cashbooks,
            selectedCashbookId = selected?.id,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CashbookUiState(),
    )

    val categories: StateFlow<List<Category>> = selectedCashbook.flatMapLatest { cashbook ->
        cashbook?.let { categoryRepository.observeCategories(it.id) } ?: flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val recurringTransactions: StateFlow<List<RecurringTransactionUiModel>> = combine(
        selectedCashbook,
        categories,
    ) { cashbook, currentCategories ->
        cashbook to currentCategories.associateBy { it.id }
    }.flatMapLatest { (cashbook, categoryById) ->
        cashbook?.let {
            recurringTransactionRepository.observeRecurringTransactions(it.id).mapToUiModels(categoryById)
        } ?: flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun saveRecurringTransaction(input: RecurringTransactionInput) {
        val cashbook = selectedCashbook.value ?: return
        val trimmedName = input.name.trim()
        if (trimmedName.isBlank()) return
        val amountMinor = runCatching { AmountFormatter.parseMinorUnits(input.amount) }.getOrNull() ?: return
        if (amountMinor <= 0) return

        viewModelScope.launch {
            val now = Instant.now()
            val existing = input.id?.let { id ->
                recurringTransactions.value.firstOrNull { it.id == id }?.raw
            }
            recurringTransactionRepository.upsertRecurringTransaction(
                RecurringTransaction(
                    id = existing?.id ?: "recurring_${UUID.randomUUID()}",
                    cashbookId = existing?.cashbookId ?: cashbook.id,
                    name = trimmedName,
                    categoryId = input.categoryId,
                    amount = Money(amountMinor, CurrencyCode.MYR),
                    type = input.type,
                    frequency = input.frequency,
                    interval = input.interval,
                    startDate = input.startDate,
                    endDate = existing?.endDate,
                    maxOccurrences = input.maxOccurrences,
                    nextRunDate = input.startDate,
                    requireConfirmation = input.requireConfirmation,
                    isPaused = existing?.isPaused ?: false,
                    generatedOccurrences = existing?.generatedOccurrences ?: 0,
                    note = input.note.trim().ifBlank { null },
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                ),
            )
            _uiState.update { state ->
                state.copy(
                    message = if (existing == null) {
                        "Recurring transaction saved."
                    } else {
                        "Recurring transaction updated."
                    },
                )
            }
        }
    }

    fun setRecurringPaused(recurringTransaction: RecurringTransactionUiModel, paused: Boolean) {
        viewModelScope.launch {
            val existing = recurringTransaction.raw
            recurringTransactionRepository.upsertRecurringTransaction(
                existing.copy(
                    isPaused = paused,
                    updatedAt = Instant.now(),
                ),
            )
        }
    }

    fun deleteRecurringTransaction(id: String) {
        viewModelScope.launch {
            recurringTransactionRepository.deleteRecurringTransaction(id)
            _uiState.update { state -> state.copy(message = "Recurring transaction deleted.") }
        }
    }

    fun updateAmountInputMode(mode: AmountInputMode) {
        userPreferencesRepository.setAmountInputMode(mode)
        _uiState.update { state -> state.copy(message = "Amount input type updated.") }
    }

    fun updateCategory(category: Category, name: String, icon: String, color: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        viewModelScope.launch {
            categoryRepository.upsertCategory(
                category.copy(
                    name = trimmedName,
                    icon = icon,
                    color = color.normalizedCategoryColor(fallback = category.color),
                    updatedAt = Instant.now(),
                ),
            )
            _uiState.update { state -> state.copy(message = "Category updated.") }
        }
    }

    fun archiveCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.archiveCategory(
                id = category.id,
                updatedAt = Instant.now(),
            )
            _uiState.update { state -> state.copy(message = "Category archived.") }
        }
    }

    fun selectCashbook(cashbookId: String) {
        cashbookRepository.selectCashbook(cashbookId)
        _uiState.update { state -> state.copy(message = "Cashbook switched.") }
    }

    fun createCashbook(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        viewModelScope.launch {
            val now = Instant.now()
            val cashbook = Cashbook(
                id = "cashbook_${UUID.randomUUID()}",
                name = trimmedName,
                currency = CurrencyCode.MYR,
                color = "#2563EB",
                icon = "wallet",
                isDefault = activeCashbooks.value.isEmpty(),
                createdAt = now,
                updatedAt = now,
            )
            cashbookRepository.upsertCashbook(cashbook)
            initialDataSeeder.seedDefaultCategories(cashbookId = cashbook.id, now = now)
            cashbookRepository.selectCashbook(cashbook.id)
            _uiState.update { state -> state.copy(message = "Cashbook created.") }
        }
    }

    fun previewImport(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = "Reading import file...") }
            val result = runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Unable to read selected file.")
                val preview = if (bytes.isXlsx()) {
                    LegacyMoneyManagerXlsxReader.read(ByteArrayInputStream(bytes))
                } else {
                    LegacyMoneyManagerCsvReader.read(bytes.toString(StandardCharsets.UTF_8))
                }
                val defaultCashbook = cashbookRepository.getSelectedCashbook()
                    ?: error("Create a cashbook before importing.")
                val duplicateRows = detectDuplicateRows(preview.records, defaultCashbook.id)
                ImportPreviewUiState(
                    records = preview.records,
                    errors = preview.errors.map { "Row ${it.rowNumber}: ${it.message}" },
                    duplicateRows = duplicateRows,
                    ignoredPhotoRows = preview.records.count { it.photos != null },
                    targetCashbookId = defaultCashbook.id,
                    targetCashbookName = defaultCashbook.name,
                )
            }

            _uiState.update { state ->
                result.fold(
                    onSuccess = { preview ->
                        state.copy(
                            isBusy = false,
                            importPreview = preview,
                            message = "Parsed ${preview.records.size} rows, ${preview.errors.size} errors, ${preview.duplicateRows.size} possible duplicates.",
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            isBusy = false,
                            message = error.toImportMessage(),
                        )
                    },
                )
            }
        }
    }

    fun confirmImport() {
        val preview = _uiState.value.importPreview ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = "Importing transactions...") }
            val result = runCatching {
                val imported = importRecords(
                    records = preview.records.filterNot { it.rowNumber in preview.duplicateRows },
                    cashbookId = preview.targetCashbookId,
                )
                imported
            }

            _uiState.update { state ->
                result.fold(
                    onSuccess = { imported ->
                        state.copy(
                            isBusy = false,
                            importPreview = null,
                            message = "Imported $imported transactions. Skipped ${preview.duplicateRows.size} duplicates.",
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            isBusy = false,
                            message = error.message ?: "Import failed.",
                        )
                    },
                )
            }
        }
    }

    fun clearImportPreview() {
        _uiState.update { it.copy(importPreview = null, message = null) }
    }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = "Exporting transactions...") }
            val result = runCatching {
                val cashbook = cashbookRepository.getSelectedCashbook()
                    ?: error("No cashbook to export.")
                val transactions = transactionRepository.observeTransactions(cashbook.id).first()
                val expenseCategories = categoryRepository.getCategoriesByType(cashbook.id, TransactionType.Expense)
                val incomeCategories = categoryRepository.getCategoriesByType(cashbook.id, TransactionType.Income)
                val categoryById = (expenseCategories + incomeCategories).associateBy { it.id }
                val rows = listOf(SupportedExportHeaders) + transactions.map { transaction ->
                    transaction.toExportRow(
                        cashbook = cashbook,
                        category = transaction.categoryId?.let(categoryById::get),
                    )
                }
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(LegacyMoneyManagerCsvWriter.write(rows).toByteArray(StandardCharsets.UTF_8))
                } ?: error("Unable to open export destination.")
                transactions.size
            }

            _uiState.update { state ->
                result.fold(
                    onSuccess = { count ->
                        state.copy(isBusy = false, message = "Exported $count transactions.")
                    },
                    onFailure = { error ->
                        state.copy(isBusy = false, message = error.message ?: "Export failed.")
                    },
                )
            }
        }
    }

    private suspend fun detectDuplicateRows(
        records: List<LegacyMoneyManagerRecord>,
        cashbookId: String,
    ): Set<Int> {
        if (records.isEmpty()) return emptySet()
        val minDate = records.minOf { it.date }
        val maxDate = records.maxOf { it.date }.plusDays(1)
        val existingKeys = transactionRepository
            .observeTransactionsInRange(
                cashbookId = cashbookId,
                startInclusive = minDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                endExclusive = maxDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            )
            .first()
            .map { it.duplicateKey(cashbookId) }
            .toSet()

        return records
            .filter { record -> record.duplicateKey(cashbookId) in existingKeys }
            .map { it.rowNumber }
            .toSet()
    }

    private suspend fun importRecords(records: List<LegacyMoneyManagerRecord>, cashbookId: String): Int {
        val categoriesByKey = mutableMapOf<Pair<TransactionType, String>, Category>()
        TransactionType.entries.forEach { type ->
            categoryRepository.getCategoriesByType(cashbookId, type).forEach { category ->
                categoriesByKey[type to category.name.lowercase()] = category
            }
        }

        val now = Instant.now()
        val transactions = records.map { record ->
            val category = categoriesByKey.getOrPut(record.type to record.categoryName.lowercase()) {
                Category(
                    id = "category_import_${record.type.name.lowercase()}_${UUID.randomUUID()}",
                    cashbookId = cashbookId,
                    name = record.categoryName,
                    type = record.type,
                    icon = defaultIconFor(record.categoryName, record.type),
                    color = "#64748B",
                    sortOrder = categoriesByKey.size,
                    createdAt = now,
                    updatedAt = now,
                ).also { categoryRepository.upsertCategory(it) }
            }
            Transaction(
                id = "transaction_import_${UUID.randomUUID()}",
                cashbookId = cashbookId,
                categoryId = category.id,
                amount = record.amount,
                type = record.type,
                dateTime = record.date.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                note = record.note,
                source = TransactionSource.Import,
                originalSourceId = "money_manager_row_${record.rowNumber}",
                importBatchId = "import_${now.toEpochMilli()}",
                createdAt = now,
                updatedAt = now,
            )
        }
        transactionRepository.upsertTransactions(transactions)
        return transactions.size
    }

    private fun ByteArray.isXlsx(): Boolean =
        size >= 4 && this[0] == 0x50.toByte() && this[1] == 0x4B.toByte()

    private fun Throwable.toImportMessage(): String {
        val rawMessage = message.orEmpty()
        if (rawMessage.startsWith("http://") || rawMessage.startsWith("https://")) {
            return "Import preview failed. Please check that the file is a valid CSV or XLSX export."
        }
        return rawMessage.ifBlank {
            "Import preview failed. Please check that the file is a valid CSV or XLSX export."
        }
    }

    private fun Transaction.duplicateKey(cashbookId: String): LegacyImportDuplicateKey =
        LegacyImportDuplicateKey(
            cashbookId = cashbookId,
            date = dateTime.atZone(ZoneId.systemDefault()).toLocalDate(),
            amountMinor = amount.minorUnits,
            type = type,
            note = note.orEmpty().trim(),
        )

    private fun LegacyMoneyManagerRecord.duplicateKey(cashbookId: String): LegacyImportDuplicateKey =
        LegacyImportDuplicateKey(
            cashbookId = cashbookId,
            date = date,
            amountMinor = amount.minorUnits,
            type = type,
            note = note.orEmpty().trim(),
        )

    private fun Transaction.toExportRow(cashbook: Cashbook, category: Category?): List<String> =
        listOf(
            category?.name.orEmpty(),
            note.orEmpty(),
            AmountFormatter.formatPlain(amount),
            amount.currency.name,
            if (type == TransactionType.Expense) "Expenses" else "Income",
            cashbook.name,
            dateTime.atZone(ZoneId.systemDefault()).toLocalDate().format(ExportDateFormatter),
            "",
        )

    private fun defaultIconFor(categoryName: String, type: TransactionType): String {
        val normalized = categoryName.lowercase()
        return when {
            type == TransactionType.Income -> "payments"
            "food" in normalized -> "restaurant"
            "transport" in normalized || "car" in normalized -> "directions_car"
            "petrol" in normalized || "打油" in normalized -> "local_gas_station"
            "doctor" in normalized -> "medical_services"
            "shop" in normalized -> "shopping_bag"
            "rent" in normalized -> "home"
            else -> "category"
        }
    }

    private companion object {
        val SupportedExportHeaders = listOf(
            "Category",
            "Note",
            "Amount",
            "Currency",
            "Type",
            "Account",
            "Date",
            "Photos",
        )
        val ExportDateFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
    }
}

private fun kotlinx.coroutines.flow.Flow<List<RecurringTransaction>>.mapToUiModels(
    categoryById: Map<String, Category>,
): kotlinx.coroutines.flow.Flow<List<RecurringTransactionUiModel>> =
    this.map { recurringTransactions ->
        recurringTransactions.map { recurringTransaction ->
            val category = recurringTransaction.categoryId?.let(categoryById::get)
            RecurringTransactionUiModel(
                id = recurringTransaction.id,
                title = recurringTransaction.name,
                categoryId = recurringTransaction.categoryId,
                categoryName = category?.name,
                amount = AmountFormatter.formatPlain(recurringTransaction.amount),
                type = recurringTransaction.type,
                frequency = recurringTransaction.frequency,
                interval = recurringTransaction.interval,
                nextRunDate = recurringTransaction.nextRunDate,
                maxOccurrences = recurringTransaction.maxOccurrences,
                generatedOccurrences = recurringTransaction.generatedOccurrences,
                note = recurringTransaction.note,
                requireConfirmation = recurringTransaction.requireConfirmation,
                isPaused = recurringTransaction.isPaused,
                raw = recurringTransaction,
            )
        }
    }

data class SettingsUiState(
    val isBusy: Boolean = false,
    val message: String? = null,
    val importPreview: ImportPreviewUiState? = null,
)

data class CashbookUiState(
    val cashbooks: List<Cashbook> = emptyList(),
    val selectedCashbookId: String? = null,
)

data class RecurringTransactionInput(
    val id: String? = null,
    val name: String,
    val amount: String,
    val type: TransactionType,
    val categoryId: String?,
    val frequency: RecurringFrequency,
    val interval: Int,
    val startDate: LocalDate,
    val maxOccurrences: Int?,
    val note: String,
    val requireConfirmation: Boolean,
)

data class RecurringTransactionUiModel(
    val id: String,
    val title: String,
    val categoryId: String?,
    val categoryName: String?,
    val amount: String,
    val type: TransactionType,
    val frequency: RecurringFrequency,
    val interval: Int,
    val nextRunDate: LocalDate,
    val maxOccurrences: Int?,
    val generatedOccurrences: Int,
    val note: String?,
    val requireConfirmation: Boolean,
    val isPaused: Boolean,
    val raw: RecurringTransaction,
)

data class ImportPreviewUiState(
    val records: List<LegacyMoneyManagerRecord> = emptyList(),
    val errors: List<String> = emptyList(),
    val duplicateRows: Set<Int> = emptySet(),
    val ignoredPhotoRows: Int = 0,
    val targetCashbookId: String = "",
    val targetCashbookName: String = "",
)
