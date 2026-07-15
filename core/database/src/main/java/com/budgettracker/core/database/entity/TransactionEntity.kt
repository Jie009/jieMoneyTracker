package com.budgettracker.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "transactions",
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
        Index(value = ["cashbookId", "dateTime"]),
        Index(value = ["cashbookId", "categoryId", "dateTime"]),
        Index(value = ["cashbookId", "type", "dateTime"]),
        Index(value = ["categoryId"]),
        Index(value = ["source"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val cashbookId: String,
    val categoryId: String?,
    val amountMinor: Long,
    val currencyCode: String,
    val type: String,
    val dateTime: Instant,
    val note: String?,
    val source: String,
    val originalSourceId: String?,
    val importBatchId: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
