package com.budgettracker.feature.addtransaction.navigation

const val ADD_TRANSACTION_ROUTE = "add_transaction"
const val ADD_TRANSACTION_ID_ARG = "transactionId"
const val ADD_TRANSACTION_ROUTE_WITH_ARGS = "$ADD_TRANSACTION_ROUTE?$ADD_TRANSACTION_ID_ARG={$ADD_TRANSACTION_ID_ARG}"

fun addTransactionRoute(transactionId: String? = null): String =
    if (transactionId == null) {
        ADD_TRANSACTION_ROUTE
    } else {
        "$ADD_TRANSACTION_ROUTE?$ADD_TRANSACTION_ID_ARG=$transactionId"
    }
