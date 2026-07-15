package com.budgettracker.core.domain.report

import com.budgettracker.core.model.Money
import com.budgettracker.core.model.Transaction
import com.budgettracker.core.model.TransactionSource
import com.budgettracker.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class MonthlySummaryCalculatorTest {
    @Test
    fun calculate_sumsIncomeExpenseAndBalance() {
        val now = Instant.parse("2026-07-15T00:00:00Z")
        val transactions = listOf(
            transaction("1", 10000, TransactionType.Income, now),
            transaction("2", 2590, TransactionType.Expense, now),
            transaction("3", 410, TransactionType.Expense, now),
        )

        val summary = MonthlySummaryCalculator.calculate(transactions)

        assertEquals(10000L, summary.income.minorUnits)
        assertEquals(3000L, summary.expense.minorUnits)
        assertEquals(7000L, summary.balance.minorUnits)
    }

    private fun transaction(
        id: String,
        amountMinor: Long,
        type: TransactionType,
        now: Instant,
    ): Transaction =
        Transaction(
            id = id,
            cashbookId = "cashbook",
            categoryId = null,
            amount = Money(amountMinor),
            type = type,
            dateTime = now,
            note = null,
            source = TransactionSource.Manual,
            createdAt = now,
            updatedAt = now,
        )
}
