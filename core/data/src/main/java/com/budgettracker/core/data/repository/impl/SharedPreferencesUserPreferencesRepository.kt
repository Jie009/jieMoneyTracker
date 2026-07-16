package com.budgettracker.core.data.repository.impl

import android.content.Context
import com.budgettracker.core.data.repository.UserPreferencesRepository
import com.budgettracker.core.model.AmountInputMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesUserPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context,
) : UserPreferencesRepository {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
    private val amountInputMode = MutableStateFlow(readAmountInputMode())

    override fun observeAmountInputMode(): StateFlow<AmountInputMode> = amountInputMode

    override fun setAmountInputMode(mode: AmountInputMode) {
        preferences.edit()
            .putString(AmountInputModeKey, mode.name)
            .apply()
        amountInputMode.value = mode
    }

    private fun readAmountInputMode(): AmountInputMode {
        val storedValue = preferences.getString(AmountInputModeKey, null)
        return AmountInputMode.entries.firstOrNull { it.name == storedValue }
            ?: AmountInputMode.NormalDecimal
    }

    private companion object {
        const val PreferencesName = "budget_tracker_preferences"
        const val AmountInputModeKey = "amount_input_mode"
    }
}
