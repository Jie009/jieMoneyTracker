package com.budgettracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.budgettracker.core.data.seed.InitialDataSeeder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var initialDataSeeder: InitialDataSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            initialDataSeeder.seedIfNeeded()
        }

        setContent {
            BudgetTrackerApp()
        }
    }
}
