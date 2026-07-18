package com.budgettracker.core.domain.importexport

import com.budgettracker.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LegacyMoneyManagerParserTest {
    @Test
    fun parseRows_supportsMoneyManagerExportShape() {
        val preview = LegacyMoneyManagerParser.parseRows(
            rows = listOf(
                mapOf(
                    "Category" to "Rental fee",
                    "Note" to "1.5 mth deposit + 750",
                    "Amount" to "1,950",
                    "Currency" to "MYR",
                    "Type" to "Expenses",
                    "Account" to "",
                    "Date" to "Jul 2, 2026",
                    "Photos" to "",
                ),
            ),
        )

        assertTrue(preview.errors.isEmpty())
        assertEquals(1, preview.records.size)
        assertEquals("Rental fee", preview.records.single().categoryName)
        assertEquals(195000L, preview.records.single().amount.minorUnits)
        assertEquals(TransactionType.Expense, preview.records.single().type)
        assertEquals(LocalDate.of(2026, 7, 2), preview.records.single().date)
    }

    @Test
    fun csvReader_parsesQuotedNotes() {
        val preview = LegacyMoneyManagerCsvReader.read(
            """
            Category,Note,Amount,Currency,Type,Account,Date,Photos
            Food,"lunch, drink",13.90,MYR,Expenses,,"Jul 14, 2026",
            """.trimIndent(),
        )

        assertTrue(preview.errors.isEmpty())
        assertEquals("lunch, drink", preview.records.single().note)
    }

    @Test
    fun csvWriter_outputsImportableCsv() {
        val csv = LegacyMoneyManagerCsvWriter.write(
            rows = listOf(
                listOf("Category", "Note", "Amount", "Currency", "Type", "Account", "Date", "Photos"),
                listOf(
                    "Uncategorized",
                    "parking, mall",
                    "3.50",
                    "MYR",
                    "Expenses",
                    "Daily",
                    "Jul 18, 2026",
                    "",
                ),
            ),
        )

        val preview = LegacyMoneyManagerCsvReader.read(csv)

        assertTrue(preview.errors.isEmpty())
        assertEquals(1, preview.records.size)
        assertEquals("Uncategorized", preview.records.single().categoryName)
        assertEquals("parking, mall", preview.records.single().note)
        assertEquals(350L, preview.records.single().amount.minorUnits)
        assertEquals(TransactionType.Expense, preview.records.single().type)
        assertEquals(LocalDate.of(2026, 7, 18), preview.records.single().date)
    }
}
