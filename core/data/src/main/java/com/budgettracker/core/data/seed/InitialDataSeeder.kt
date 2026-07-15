package com.budgettracker.core.data.seed

import com.budgettracker.core.data.repository.CashbookRepository
import com.budgettracker.core.data.repository.CategoryRepository
import com.budgettracker.core.model.Cashbook
import com.budgettracker.core.model.Category
import com.budgettracker.core.model.CurrencyCode
import com.budgettracker.core.model.TransactionType
import java.time.Instant
import javax.inject.Inject

class InitialDataSeeder @Inject constructor(
    private val cashbookRepository: CashbookRepository,
    private val categoryRepository: CategoryRepository,
) {
    suspend fun seedIfNeeded() {
        if (cashbookRepository.hasAnyCashbook()) return

        val now = Instant.now()
        val cashbookId = DEFAULT_CASHBOOK_ID

        cashbookRepository.upsertCashbook(
            Cashbook(
                id = cashbookId,
                name = "Personal",
                currency = CurrencyCode.MYR,
                color = "#2563EB",
                icon = "wallet",
                isDefault = true,
                createdAt = now,
                updatedAt = now,
            ),
        )

        categoryRepository.upsertCategories(defaultCategories(cashbookId = cashbookId, now = now))
    }

    private fun defaultCategories(cashbookId: String, now: Instant): List<Category> =
        expenseCategories(cashbookId = cashbookId, now = now) +
            incomeCategories(cashbookId = cashbookId, now = now)

    private fun expenseCategories(cashbookId: String, now: Instant): List<Category> =
        listOf(
            DefaultCategory("food", "Food", "restaurant", "#EF4444"),
            DefaultCategory("transport", "Transport", "directions_car", "#F97316"),
            DefaultCategory("petrol", "Petrol", "local_gas_station", "#EAB308"),
            DefaultCategory("parking", "Parking", "local_parking", "#84CC16"),
            DefaultCategory("shopping", "Shopping", "shopping_bag", "#EC4899"),
            DefaultCategory("subscription", "Subscription", "subscriptions", "#8B5CF6"),
            DefaultCategory("doctor", "Doctor", "medical_services", "#06B6D4"),
            DefaultCategory("other", "Other", "more_horiz", "#64748B"),
        ).mapIndexed { index, category ->
            category.asCategory(
                cashbookId = cashbookId,
                type = TransactionType.Expense,
                sortOrder = index,
                now = now,
            )
        }

    private fun incomeCategories(cashbookId: String, now: Instant): List<Category> =
        listOf(
            DefaultCategory("salary", "Salary", "payments", "#22C55E"),
            DefaultCategory("bonus", "Bonus", "redeem", "#14B8A6"),
            DefaultCategory("refund", "Refund", "undo", "#3B82F6"),
            DefaultCategory("gift", "Gift", "card_giftcard", "#A855F7"),
            DefaultCategory("other", "Other", "more_horiz", "#64748B"),
        ).mapIndexed { index, category ->
            category.asCategory(
                cashbookId = cashbookId,
                type = TransactionType.Income,
                sortOrder = index,
                now = now,
            )
        }

    private fun DefaultCategory.asCategory(
        cashbookId: String,
        type: TransactionType,
        sortOrder: Int,
        now: Instant,
    ): Category = Category(
        id = "category_default_${type.name.lowercase()}_$key",
        cashbookId = cashbookId,
        name = name,
        defaultNameKey = "category_${type.name.lowercase()}_$key",
        type = type,
        icon = icon,
        color = color,
        sortOrder = sortOrder,
        createdAt = now,
        updatedAt = now,
    )

    private data class DefaultCategory(
        val key: String,
        val name: String,
        val icon: String,
        val color: String,
    )

    private companion object {
        const val DEFAULT_CASHBOOK_ID = "cashbook_default_personal"
    }
}
