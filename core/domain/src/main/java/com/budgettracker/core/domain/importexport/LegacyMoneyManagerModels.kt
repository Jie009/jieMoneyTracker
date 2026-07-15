package com.budgettracker.core.domain.importexport

import com.budgettracker.core.model.CurrencyCode
import com.budgettracker.core.model.Money
import com.budgettracker.core.model.TransactionType
import java.time.LocalDate

data class LegacyMoneyManagerRecord(
    val rowNumber: Int,
    val categoryName: String,
    val note: String?,
    val amount: Money,
    val type: TransactionType,
    val accountName: String?,
    val date: LocalDate,
    val photos: String?,
)

data class LegacyImportPreview(
    val records: List<LegacyMoneyManagerRecord>,
    val errors: List<LegacyImportRowError>,
)

data class LegacyImportRowError(
    val rowNumber: Int,
    val message: String,
)

data class LegacyImportDuplicateKey(
    val cashbookId: String,
    val date: LocalDate,
    val amountMinor: Long,
    val type: TransactionType,
    val note: String,
)

internal data class LegacyMoneyManagerRawRow(
    val rowNumber: Int,
    val values: Map<String, String>,
) {
    fun value(header: String): String = values[header].orEmpty().trim()
}

internal val SupportedLegacyHeaders = listOf(
    "Category",
    "Note",
    "Amount",
    "Currency",
    "Type",
    "Account",
    "Date",
    "Photos",
)

internal fun parseCurrencyCode(value: String): CurrencyCode =
    if (value.isBlank()) {
        CurrencyCode.MYR
    } else {
        CurrencyCode.valueOf(value.uppercase())
    }
