package com.budgettracker.app.notification

import android.content.Intent
import com.budgettracker.core.model.TransactionType
import com.budgettracker.feature.addtransaction.navigation.addTransactionRoute

object PaymentNotificationIntents {
    const val ACTION_QUICK_ADD = "com.budgettracker.action.QUICK_ADD_NOTIFICATION_TRANSACTION"
    const val EXTRA_AMOUNT = "com.budgettracker.extra.AMOUNT"
    const val EXTRA_TYPE = "com.budgettracker.extra.TYPE"
    const val EXTRA_NOTE = "com.budgettracker.extra.NOTE"

    fun routeFromIntent(intent: Intent?): String? {
        if (intent?.action != ACTION_QUICK_ADD) return null

        val amount = intent.getStringExtra(EXTRA_AMOUNT)?.takeIf { it.isNotBlank() } ?: return null
        val type = intent.getStringExtra(EXTRA_TYPE)
            ?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() }
            ?: TransactionType.Expense
        val note = intent.getStringExtra(EXTRA_NOTE)

        return addTransactionRoute(
            amount = amount,
            type = type,
            note = note,
        )
    }
}
