package com.budgettracker.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(
            entity = CashbookEntity::class,
            parentColumns = ["id"],
            childColumns = ["cashbookId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["nextRunDate"]),
        Index(value = ["cashbookId", "isPaused"]),
        Index(value = ["categoryId"]),
    ],
)
data class RecurringTransactionEntity(
    @PrimaryKey val id: String,
    val cashbookId: String,
    val name: String,
    val categoryId: String?,
    val amountMinor: Long,
    val currencyCode: String,
    val type: String,
    val frequency: String,
    val interval: Int,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val maxOccurrences: Int?,
    val generatedOccurrences: Int,
    val nextRunDate: LocalDate,
    val requireConfirmation: Boolean,
    val isPaused: Boolean,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
