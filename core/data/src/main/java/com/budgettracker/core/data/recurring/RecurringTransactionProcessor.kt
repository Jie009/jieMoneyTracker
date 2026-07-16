package com.budgettracker.core.data.recurring

import com.budgettracker.core.data.repository.RecurringTransactionRepository
import com.budgettracker.core.data.repository.TransactionRepository
import com.budgettracker.core.model.RecurringFrequency
import com.budgettracker.core.model.RecurringTransaction
import com.budgettracker.core.model.Transaction
import com.budgettracker.core.model.TransactionSource
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class RecurringTransactionProcessor @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend fun processDueRecurringTransactions(today: LocalDate = LocalDate.now()) {
        val dueRecurringTransactions = recurringTransactionRepository.getDueRecurringTransactions(today)
        dueRecurringTransactions.forEach { recurringTransaction ->
            processRecurringTransaction(
                recurringTransaction = recurringTransaction,
                today = today,
            )
        }
    }

    private suspend fun processRecurringTransaction(
        recurringTransaction: RecurringTransaction,
        today: LocalDate,
    ) {
        var current = recurringTransaction
        while (!current.isPaused && current.nextRunDate <= today && current.canGenerateMore()) {
            val runDate = current.nextRunDate
            transactionRepository.upsertTransaction(current.asTransaction(runDate))

            val generatedOccurrences = current.generatedOccurrences + 1
            val nextRunDate = current.nextRunDate.plusFrequency(
                frequency = current.frequency,
                interval = current.interval,
            )
            current = current.copy(
                generatedOccurrences = generatedOccurrences,
                nextRunDate = nextRunDate,
                isPaused = current.maxOccurrences?.let { generatedOccurrences >= it } ?: false,
                updatedAt = Instant.now(),
            )
        }
        if (current != recurringTransaction) {
            recurringTransactionRepository.upsertRecurringTransaction(current)
        }
    }

    private fun RecurringTransaction.canGenerateMore(): Boolean =
        maxOccurrences?.let { generatedOccurrences < it } ?: true

    private fun RecurringTransaction.asTransaction(runDate: LocalDate): Transaction {
        val now = Instant.now()
        val transactionId = "transaction_recurring_${id}_$runDate"
        return Transaction(
            id = transactionId,
            cashbookId = cashbookId,
            categoryId = categoryId,
            amount = amount,
            type = type,
            dateTime = runDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            note = note ?: name,
            source = TransactionSource.Recurring,
            originalSourceId = "${id}_$runDate",
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun LocalDate.plusFrequency(
        frequency: RecurringFrequency,
        interval: Int,
    ): LocalDate {
        val safeInterval = interval.coerceAtLeast(1).toLong()
        return when (frequency) {
            RecurringFrequency.Daily -> plusDays(safeInterval)
            RecurringFrequency.Weekly -> plusWeeks(safeInterval)
            RecurringFrequency.Monthly -> plusMonths(safeInterval)
            RecurringFrequency.Yearly -> plusYears(safeInterval)
        }
    }
}
