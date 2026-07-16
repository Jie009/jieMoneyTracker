package com.budgettracker.app.navigation

import android.content.Intent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.budgettracker.app.notification.PaymentNotificationIntents
import com.budgettracker.core.ui.BottomMenuDestination
import com.budgettracker.core.ui.BudgetBottomMenuBar
import com.budgettracker.feature.addtransaction.add.AddTransactionRoute
import com.budgettracker.feature.addtransaction.navigation.ADD_TRANSACTION_AMOUNT_ARG
import com.budgettracker.feature.addtransaction.navigation.ADD_TRANSACTION_ROUTE
import com.budgettracker.feature.addtransaction.navigation.ADD_TRANSACTION_ROUTE_WITH_ARGS
import com.budgettracker.feature.addtransaction.navigation.ADD_TRANSACTION_ID_ARG
import com.budgettracker.feature.addtransaction.navigation.ADD_TRANSACTION_NOTE_ARG
import com.budgettracker.feature.addtransaction.navigation.ADD_TRANSACTION_TYPE_ARG
import com.budgettracker.feature.addtransaction.navigation.addTransactionRoute
import com.budgettracker.feature.charts.navigation.CHARTS_ROUTE
import com.budgettracker.feature.charts.overview.ChartsRoute
import com.budgettracker.feature.home.home.HomeRoute
import com.budgettracker.feature.home.navigation.HOME_ROUTE
import com.budgettracker.feature.settings.navigation.SETTINGS_ROUTE
import com.budgettracker.feature.settings.settings.SettingsRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    launchIntent: Intent? = null,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = navBackStackEntry
        ?.destination
        ?.route
        .toBottomMenuDestination()

    LaunchedEffect(launchIntent) {
        PaymentNotificationIntents.routeFromIntent(launchIntent)?.let { route ->
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    fun navigateHome() {
        val poppedToHome = navController.popBackStack(
            route = HOME_ROUTE,
            inclusive = false,
        )
        if (!poppedToHome) {
            navController.navigate(HOME_ROUTE) {
                launchSingleTop = true
            }
        }
    }

    fun navigateTo(destination: BottomMenuDestination) {
        when (destination) {
            BottomMenuDestination.Home -> {
                navigateHome()
                return
            }
            else -> Unit
        }
        navController.navigate(
            route = destination.route,
            navOptions = navOptions {
                launchSingleTop = true
                popUpTo(HOME_ROUTE) {
                    saveState = true
                }
                restoreState = true
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
    ) {
        NavHost(
            navController = navController,
            startDestination = HOME_ROUTE,
            modifier = Modifier.weight(1f),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 220,
                        easing = FastOutSlowInEasing,
                    ),
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 160))
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 220,
                        easing = FastOutSlowInEasing,
                    ),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 160))
            },
        ) {
            composable(HOME_ROUTE) {
                HomeRoute(
                    onProfileClick = {
                        navController.navigate(SETTINGS_ROUTE) {
                            launchSingleTop = true
                        }
                    },
                    onEditTransaction = { transactionId ->
                        navController.navigate(addTransactionRoute(transactionId)) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(CHARTS_ROUTE) {
                ChartsRoute()
            }
            composable(
                route = ADD_TRANSACTION_ROUTE_WITH_ARGS,
                arguments = listOf(
                    navArgument(ADD_TRANSACTION_ID_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument(ADD_TRANSACTION_AMOUNT_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument(ADD_TRANSACTION_TYPE_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument(ADD_TRANSACTION_NOTE_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { backStackEntry ->
                AddTransactionRoute(
                    transactionId = backStackEntry.arguments?.getString(ADD_TRANSACTION_ID_ARG),
                    prefillAmountInput = backStackEntry.arguments?.getString(ADD_TRANSACTION_AMOUNT_ARG),
                    prefillTransactionType = backStackEntry.arguments
                        ?.getString(ADD_TRANSACTION_TYPE_ARG)
                        ?.toTransactionTypeOrNull(),
                    prefillNote = backStackEntry.arguments?.getString(ADD_TRANSACTION_NOTE_ARG),
                    onCancel = {
                        navigateHome()
                    },
                    onSaved = {
                        navigateHome()
                    },
                )
            }
            composable(SETTINGS_ROUTE) {
                SettingsRoute()
            }
        }

        BudgetBottomMenuBar(
            selectedDestination = selectedDestination,
            onDestinationClick = ::navigateTo,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
        )
    }
}

private val BottomMenuDestination.route: String
    get() = when (this) {
        BottomMenuDestination.Home -> HOME_ROUTE
        BottomMenuDestination.Charts -> CHARTS_ROUTE
        BottomMenuDestination.Add -> ADD_TRANSACTION_ROUTE
    }

private fun String?.toBottomMenuDestination(): BottomMenuDestination = when (this) {
    HOME_ROUTE -> BottomMenuDestination.Home
    CHARTS_ROUTE -> BottomMenuDestination.Charts
    ADD_TRANSACTION_ROUTE -> BottomMenuDestination.Add
    ADD_TRANSACTION_ROUTE_WITH_ARGS -> BottomMenuDestination.Add
    else -> BottomMenuDestination.Home
}

private fun String.toTransactionTypeOrNull() =
    runCatching { com.budgettracker.core.model.TransactionType.valueOf(this) }.getOrNull()

private val AppBackground = Color(0xFF08142A)
