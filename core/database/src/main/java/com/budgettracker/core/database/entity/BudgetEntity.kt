package com.budgettracker.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.YearMonth

@Entity(
    tableName = "budgets",
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
        Index(value = ["cashbookId", "month"]),
        Index(value = ["cashbookId", "categoryId", "month"], unique = true),
        Index(value = ["categoryId"]),
    ],
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val cashbookId: String,
    val categoryId: String?,
    val month: YearMonth,
    val amountMinor: Long,
    val currencyCode: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
