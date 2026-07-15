package com.budgettracker.core.domain.money

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountFormatterTest {
    @Test
    fun parseMinorUnits_supportsDecimalsAndThousands() {
        assertEquals(5690L, AmountFormatter.parseMinorUnits("56.90"))
        assertEquals(195000L, AmountFormatter.parseMinorUnits("1,950"))
        assertEquals(1200L, AmountFormatter.parseMinorUnits("12"))
    }
}
