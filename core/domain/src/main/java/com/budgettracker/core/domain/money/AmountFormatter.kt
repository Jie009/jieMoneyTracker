package com.budgettracker.core.domain.money

import com.budgettracker.core.model.CurrencyCode
import com.budgettracker.core.model.Money
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object AmountFormatter {
    fun parseMinorUnits(input: String): Long {
        val normalized = input
            .trim()
            .replace(",", "")
            .removePrefix("+")

        require(normalized.isNotBlank()) { "Amount is required." }

        return BigDecimal(normalized)
            .setScale(2, RoundingMode.UNNECESSARY)
            .movePointRight(2)
            .longValueExact()
    }

    fun formatPlain(money: Money): String =
        BigDecimal(money.minorUnits)
            .movePointLeft(2)
            .setScale(2)
            .toPlainString()

    fun formatDisplay(money: Money, signed: Boolean = false, isIncome: Boolean = false): String {
        val amount = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(BigDecimal(money.minorUnits).movePointLeft(2))
        val sign = when {
            !signed -> ""
            isIncome -> "+"
            else -> "-"
        }

        return "$sign${money.currency.name} $amount"
    }

    fun money(input: String, currency: CurrencyCode = CurrencyCode.MYR): Money =
        Money(
            minorUnits = parseMinorUnits(input),
            currency = currency,
        )
}
