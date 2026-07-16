package com.budgettracker.core.data.repository

import com.budgettracker.core.model.AmountInputMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observeAmountInputMode(): Flow<AmountInputMode>

    fun setAmountInputMode(mode: AmountInputMode)
}
