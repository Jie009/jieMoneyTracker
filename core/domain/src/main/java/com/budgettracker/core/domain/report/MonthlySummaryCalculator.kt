package com.budgettracker.core.domain.report

import com.budgettracker.core.model.CurrencyCode
import com.budgettracker.core.model.Money
import com.budgettracker.core.model.Transaction
import com.budgettracker.core.model.TransactionType

data class MonthlySummary(
    val income: Money,
    val expense: Money,
    val balance: Money,
)

object MonthlySummaryCalculator {
    fun calculate(transactions: List<Transaction>, currency: CurrencyCode = CurrencyCode.MYR): MonthlySummary {
        val incomeMinor = transactions
            .filter { it.type == TransactionType.Income }
            .sumOf { it.amount.minorUnits }
        val expenseMinor = transactions
            .filter { it.type == TransactionType.Expense }
            .sumOf { it.amount.minorUnits }

        return MonthlySummary(
            income = Money(incomeMinor, currency),
            expense = Money(expenseMinor, currency),
            balance = Money(incomeMinor - expenseMinor, currency),
        )
    }
}
