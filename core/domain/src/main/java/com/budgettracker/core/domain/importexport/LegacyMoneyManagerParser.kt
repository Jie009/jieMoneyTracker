package com.budgettracker.core.domain.importexport

import com.budgettracker.core.domain.money.AmountFormatter
import com.budgettracker.core.model.Money
import com.budgettracker.core.model.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.util.Locale

object LegacyMoneyManagerParser {
    private val dateFormatters = listOf(
        DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMM d, yyyy")
            .toFormatter(Locale.ENGLISH),
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("M/d/yyyy"),
        DateTimeFormatter.ofPattern("d-MM-yyyy"),
        DateTimeFormatter.ofPattern("M-d-yyyy"),
    )

    fun parseRows(rows: List<Map<String, String>>, firstDataRowNumber: Int = 2): LegacyImportPreview {
        val records = mutableListOf<LegacyMoneyManagerRecord>()
        val errors = mutableListOf<LegacyImportRowError>()

        rows.forEachIndexed { index, values ->
            val row = LegacyMoneyManagerRawRow(
                rowNumber = firstDataRowNumber + index,
                values = values,
            )
            if (row.values.values.all { it.isBlank() }) return@forEachIndexed

            parseRow(row)
                .onSuccess(records::add)
                .onFailure { error ->
                    errors += LegacyImportRowError(
                        rowNumber = row.rowNumber,
                        message = error.message ?: "Unable to parse row.",
                    )
                }
        }

        return LegacyImportPreview(records = records, errors = errors)
    }

    internal fun parseRow(row: LegacyMoneyManagerRawRow): Result<LegacyMoneyManagerRecord> =
        runCatching {
            val category = row.value("Category")
            require(category.isNotBlank()) { "Category is required." }

            val type = parseType(row.value("Type"))
            val currency = parseCurrencyCode(row.value("Currency"))
            val amount = Money(
                minorUnits = AmountFormatter.parseMinorUnits(row.value("Amount")),
                currency = currency,
            )
            val date = parseDate(row.value("Date"))
            val note = row.value("Note").ifBlank { null }
            val account = row.value("Account").ifBlank { null }
            val photos = row.value("Photos").ifBlank { null }

            LegacyMoneyManagerRecord(
                rowNumber = row.rowNumber,
                categoryName = category,
                note = note,
                amount = amount,
                type = type,
                accountName = account,
                date = date,
                photos = photos,
            )
        }

    private fun parseType(value: String): TransactionType =
        when (value.trim().lowercase()) {
            "expense", "expenses", "debit", "spent" -> TransactionType.Expense
            "income", "credit", "received" -> TransactionType.Income
            else -> error("Unsupported transaction type: $value")
        }

    private fun parseDate(value: String): LocalDate {
        require(value.isNotBlank()) { "Date is required." }

        for (formatter in dateFormatters) {
            try {
                return LocalDate.parse(value, formatter)
            } catch (_: DateTimeParseException) {
                // Try the next supported legacy date format.
            }
        }

        error("Unsupported date format: $value")
    }
}
