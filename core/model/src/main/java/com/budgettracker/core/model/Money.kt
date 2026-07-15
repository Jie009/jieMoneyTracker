package com.budgettracker.core.model

data class Money(
    val minorUnits: Long,
    val currency: CurrencyCode = CurrencyCode.MYR,
)
