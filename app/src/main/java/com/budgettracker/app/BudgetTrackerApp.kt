package com.budgettracker.app

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.budgettracker.app.navigation.AppNavHost

@Composable
fun BudgetTrackerApp(launchIntent: Intent? = null) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavHost(
                navController = rememberNavController(),
                launchIntent = launchIntent,
            )
        }
    }
}
