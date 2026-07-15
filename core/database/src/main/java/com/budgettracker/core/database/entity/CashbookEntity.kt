package com.budgettracker.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "cashbooks",
    indices = [
        Index(value = ["isDefault"]),
        Index(value = ["isArchived"]),
    ],
)
data class CashbookEntity(
    @PrimaryKey val id: String,
    val name: String,
    val currencyCode: String,
    val color: String?,
    val icon: String?,
    val isDefault: Boolean,
    val isArchived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
