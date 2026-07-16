package com.budgettracker.feature.addtransaction.navigation

import android.net.Uri
import com.budgettracker.core.model.TransactionType

const val ADD_TRANSACTION_ROUTE = "add_transaction"
const val ADD_TRANSACTION_ID_ARG = "transactionId"
const val ADD_TRANSACTION_AMOUNT_ARG = "amount"
const val ADD_TRANSACTION_TYPE_ARG = "type"
const val ADD_TRANSACTION_NOTE_ARG = "note"
const val ADD_TRANSACTION_ROUTE_WITH_ARGS =
    "$ADD_TRANSACTION_ROUTE?$ADD_TRANSACTION_ID_ARG={$ADD_TRANSACTION_ID_ARG}" +
        "&$ADD_TRANSACTION_AMOUNT_ARG={$ADD_TRANSACTION_AMOUNT_ARG}" +
        "&$ADD_TRANSACTION_TYPE_ARG={$ADD_TRANSACTION_TYPE_ARG}" +
        "&$ADD_TRANSACTION_NOTE_ARG={$ADD_TRANSACTION_NOTE_ARG}"

fun addTransactionRoute(transactionId: String? = null): String =
    if (transactionId == null) {
        ADD_TRANSACTION_ROUTE
    } else {
        "$ADD_TRANSACTION_ROUTE?$ADD_TRANSACTION_ID_ARG=$transactionId"
    }

fun addTransactionRoute(
    amount: String,
    type: TransactionType,
    note: String? = null,
): String {
    val encodedAmount = Uri.encode(amount)
    val encodedType = Uri.encode(type.name)
    val encodedNote = Uri.encode(note.orEmpty())

    return "$ADD_TRANSACTION_ROUTE?$ADD_TRANSACTION_AMOUNT_ARG=$encodedAmount" +
        "&$ADD_TRANSACTION_TYPE_ARG=$encodedType" +
        "&$ADD_TRANSACTION_NOTE_ARG=$encodedNote"
}
