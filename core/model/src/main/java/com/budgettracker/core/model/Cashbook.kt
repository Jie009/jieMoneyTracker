package com.budgettracker.core.model

import java.time.Instant

data class Cashbook(
    val id: String,
    val name: String,
    val currency: CurrencyCode = CurrencyCode.MYR,
    val color: String? = null,
    val icon: String? = null,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
