package com.budgettracker.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = CashbookEntity::class,
            parentColumns = ["id"],
            childColumns = ["cashbookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["cashbookId"]),
        Index(value = ["cashbookId", "type"]),
        Index(value = ["cashbookId", "sortOrder"]),
    ],
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val cashbookId: String,
    val name: String,
    val defaultNameKey: String?,
    val type: String,
    val icon: String,
    val color: String,
    val sortOrder: Int,
    val isArchived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
