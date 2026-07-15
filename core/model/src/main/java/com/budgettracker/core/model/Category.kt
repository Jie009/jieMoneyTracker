package com.budgettracker.core.model

import java.time.Instant

data class Category(
    val id: String,
    val cashbookId: String,
    val name: String,
    val defaultNameKey: String? = null,
    val type: TransactionType,
    val icon: String,
    val color: String,
    val sortOrder: Int,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
