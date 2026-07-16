package com.budgettracker.feature.home.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.core.domain.money.AmountFormatter
import com.budgettracker.core.model.Money
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun HomeRoute(
    onProfileClick: () -> Unit = {},
    onEditTransaction: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        monthlySummary = uiState.monthlySummary,
        transactions = uiState.transactions,
        onProfileClick = onProfileClick,
        onDeleteTransaction = viewModel::deleteTransaction,
        onEditTransaction = { transaction -> onEditTransaction(transaction.id) },
        modifier = modifier,
    )
}

@Composable
private fun HomeScreen(
    monthlySummary: MonthlySummaryUiModel,
    transactions: List<HomeTransactionUiModel>,
    onProfileClick: () -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onEditTransaction: (HomeTransactionUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMonthDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var categoryQuery by remember { mutableStateOf("") }
    var selectedTransactionId by remember { mutableStateOf<String?>(null) }
    val selectedTransaction = remember(transactions, selectedTransactionId) {
        transactions.firstOrNull { it.id == selectedTransactionId }
    }
    val visibleTransactions = remember(transactions, selectedMonth, categoryQuery) {
        val normalizedQuery = categoryQuery.trim()
        transactions
            .filter { transaction -> YearMonth.from(transaction.date) == selectedMonth }
            .filter { transaction ->
                normalizedQuery.isBlank() || transaction.title.contains(
                    other = normalizedQuery,
                    ignoreCase = true,
                )
            }
            .withVisibleDayHeaders()
    }
    val selectedMonthlySummary = remember(monthlySummary, transactions, selectedMonth) {
        monthlySummary.forMonth(
            selectedMonth = selectedMonth,
            transactions = transactions,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 18.dp),
    ) {
        Spacer(modifier = Modifier.height(18.dp))
        HomeHeader(
            onProfileClick = onProfileClick,
            onSearchClick = { showSearchDialog = true },
            onCalendarClick = { showMonthDialog = true },
        )
        Spacer(modifier = Modifier.height(14.dp))
        BalanceCard(summary = selectedMonthlySummary)
        Spacer(modifier = Modifier.height(14.dp))
        SectionHeader(
            title = filterTitle(
                selectedMonth = selectedMonth,
                categoryQuery = categoryQuery,
            ),
            action = selectedMonth.asFilterLabel(),
            onActionClick = { showMonthDialog = true },
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (visibleTransactions.isEmpty()) {
                item {
                    EmptyTransactionsMessage()
                }
            } else {
                items(visibleTransactions, key = { it.id }) { transaction ->
                    if (transaction.showDayHeader) {
                        DayHeader(
                            label = transaction.dayLabel,
                            summary = transaction.daySummary,
                        )
                    }
                    TransactionRow(
                        transaction = transaction,
                        onClick = { selectedTransactionId = transaction.id },
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    if (showMonthDialog) {
        HomeMonthFilterDialog(
            selectedMonth = selectedMonth,
            onMonthSelected = { selectedMonth = it },
            onDismiss = { showMonthDialog = false },
        )
    }

    if (showSearchDialog) {
        CategorySearchDialog(
            query = categoryQuery,
            onQuerySelected = { categoryQuery = it },
            onDismiss = { showSearchDialog = false },
        )
    }

    selectedTransaction?.let { transaction ->
        TransactionDetailsDialog(
            transaction = transaction,
            onDismiss = { selectedTransactionId = null },
            onEditClick = {
                onEditTransaction(transaction)
                selectedTransactionId = null
            },
            onDeleteClick = {
                onDeleteTransaction(transaction.id)
                selectedTransactionId = null
            },
        )
    }
}

@Composable
private fun HomeHeader(
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCalendarClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RoundIconButton(
                icon = Icons.Filled.Menu,
                contentDescription = "Open profile menu",
                onClick = onProfileClick,
            )
            Text(
                text = "Money Tracker",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                ),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RoundIconButton(
                icon = Icons.Filled.Search,
                contentDescription = "Search transactions",
                onClick = onSearchClick,
            )
            RoundIconButton(
                icon = Icons.Filled.CalendarMonth,
                contentDescription = "Open calendar",
                onClick = onCalendarClick,
            )
        }
    }
}

@Composable
private fun CategorySearchDialog(
    query: String,
    onQuerySelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var queryDraft by remember(query) { mutableStateOf(query) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                text = "Search category",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter a category name, for example Petrol or Groceries.",
                    color = MutedText,
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = queryDraft,
                    onValueChange = { queryDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Category") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onQuerySelected(queryDraft.trim())
                    onDismiss()
                },
            ) {
                Text("Search")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun BalanceCard(summary: MonthlySummaryUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, AccentStroke, RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xCC1E40AF),
                        Color(0xE60F172A),
                    ),
                ),
            )
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = summary.cashbookName,
                color = SoftText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = summary.currency,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "RM ${summary.expenses}",
            color = Color.White,
            fontSize = 34.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1.6).sp,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BalanceMetricCard(
                label = "Remaining",
                value = summary.balance.asRmAmount(),
                modifier = Modifier.weight(1f),
            )
            BalanceMetricCard(
                label = "Incomee",
                value = "RM ${summary.income}",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun String.asRmAmount(): String =
    if (startsWith("-")) {
        "-RM ${removePrefix("-")}"
    } else {
        "RM $this"
    }

private fun MonthlySummaryUiModel.forMonth(
    selectedMonth: YearMonth,
    transactions: List<HomeTransactionUiModel>,
): MonthlySummaryUiModel {
    val monthTransactions = transactions.filter { transaction ->
        YearMonth.from(transaction.date) == selectedMonth
    }
    val income = monthTransactions
        .filter { it.isIncome }
        .sumOf { it.amount.extractMinorUnits() }
    val expenses = monthTransactions
        .filterNot { it.isIncome }
        .sumOf { it.amount.extractMinorUnits() }

    return copy(
        year = selectedMonth.year.toString(),
        month = selectedMonth.month.name.take(3).lowercase().replaceFirstChar(Char::titlecase),
        expenses = expenses.formatPlainAmount(),
        income = income.formatPlainAmount(),
        balance = (income - expenses).formatPlainAmount(),
    )
}

private fun String.extractMinorUnits(): Long {
    val numeric = filter { it.isDigit() || it == '.' }
    return runCatching { AmountFormatter.parseMinorUnits(numeric) }.getOrDefault(0L)
}

private fun Long.formatPlainAmount(): String =
    AmountFormatter.formatPlainGrouped(Money(this))

@Composable
private fun BalanceMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(10.dp),
    ) {
        Text(
            text = label,
            color = MutedText,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = action,
            color = Color(0xFFBFDBFE),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(AccentBlue.copy(alpha = 0.1f))
                .clickable(onClick = onActionClick)
                .padding(horizontal = 14.dp, vertical = 9.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeMonthFilterDialog(
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedMonth.atDay(1).toUtcMillis(),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis
                        ?.toLocalDate()
                        ?.let { onMonthSelected(YearMonth.from(it)) }
                    onDismiss()
                },
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun DayHeader(
    label: String,
    summary: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label.uppercase(),
            color = MutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
        )
        Text(
            text = summary,
            color = MutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun EmptyTransactionsMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Stroke, RoundedCornerShape(18.dp))
            .background(Panel.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp, vertical = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No matching transactions",
            color = MutedText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TransactionDetailsDialog(
    transaction: HomeTransactionUiModel,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                text = transaction.title,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                DetailLine(
                    label = "Category",
                    value = transaction.title,
                )
                if (transaction.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailLine(
                        label = "Description",
                        value = transaction.note,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                DetailLine(
                    label = "Amount",
                    value = transaction.amount,
                    valueColor = if (transaction.isIncome) IncomeText else ExpenseText,
                )
                Spacer(modifier = Modifier.height(10.dp))
                DetailLine(
                    label = "Created",
                    value = transaction.createdAt.format(CreatedAtFormatter),
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDeleteClick) {
                    Text("Delete", color = ExpenseText)
                }
                TextButton(onClick = onEditClick) {
                    Text("Edit")
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
    )
}

@Composable
private fun DetailLine(
    label: String,
    value: String,
    valueColor: Color = Color.White,
) {
    Column {
        Text(
            text = label.uppercase(),
            color = MutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun TransactionRow(
    transaction: HomeTransactionUiModel,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Stroke, RoundedCornerShape(18.dp))
            .background(Panel)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(15.dp))
                .border(1.dp, AccentStroke.copy(alpha = 0.5f), RoundedCornerShape(15.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            AccentBlue.copy(alpha = 0.2f),
                            AccentGreen.copy(alpha = 0.08f),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = transaction.icon,
                contentDescription = transaction.title,
                tint = Color(0xFFBFDBFE),
                modifier = Modifier.size(23.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (transaction.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = transaction.note,
                    color = MutedText,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            text = transaction.amount,
            color = if (transaction.isIncome) IncomeText else ExpenseText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
        )
    }
}

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Stroke, RoundedCornerShape(16.dp))
            .background(Panel.copy(alpha = 0.88f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

data class MonthlySummaryUiModel(
    val year: String = "",
    val month: String = "",
    val expenses: String = "0.00",
    val income: String = "0.00",
    val balance: String = "0.00",
    val currency: String = "MYR",
    val cashbookName: String = "Personal",
)

data class HomeTransactionUiModel(
    val id: String,
    val date: LocalDate,
    val dayLabel: String,
    val daySummary: String,
    val title: String,
    val note: String,
    val amount: String,
    val createdAt: LocalDateTime,
    val icon: ImageVector,
    val isIncome: Boolean,
    val showDayHeader: Boolean,
)

val sampleHomeTransactions = listOf(
    HomeTransactionUiModel(
        id = "petrol_2026_07_15",
        date = LocalDate.of(2026, 7, 15),
        dayLabel = "Today",
        daySummary = "Expenses: RM 211.00",
        title = "Petrol",
        note = "Filled from notification",
        amount = "-RM 68.20",
        createdAt = LocalDateTime.of(2026, 7, 15, 21, 18),
        icon = Icons.Filled.LocalGasStation,
        isIncome = false,
        showDayHeader = true,
    ),
    HomeTransactionUiModel(
        id = "groceries_2026_07_15",
        date = LocalDate.of(2026, 7, 15),
        dayLabel = "Today",
        daySummary = "Expenses: RM 211.00",
        title = "Groceries",
        note = "",
        amount = "-RM 142.80",
        createdAt = LocalDateTime.of(2026, 7, 15, 19, 42),
        icon = Icons.Filled.ShoppingBag,
        isIncome = false,
        showDayHeader = false,
    ),
    HomeTransactionUiModel(
        id = "salary_2026_07_14",
        date = LocalDate.of(2026, 7, 14),
        dayLabel = "Yesterday",
        daySummary = "Income: RM 3,500.00",
        title = "Salary",
        note = "",
        amount = "+RM 3,500.00",
        createdAt = LocalDateTime.of(2026, 7, 14, 9, 5),
        icon = Icons.Filled.Payments,
        isIncome = true,
        showDayHeader = true,
    ),
    HomeTransactionUiModel(
        id = "coffee_2026_07_14",
        date = LocalDate.of(2026, 7, 14),
        dayLabel = "Yesterday",
        daySummary = "Income: RM 3,500.00",
        title = "Coffee",
        note = "Afternoon break",
        amount = "-RM 12.50",
        createdAt = LocalDateTime.of(2026, 7, 14, 15, 36),
        icon = Icons.Filled.LocalCafe,
        isIncome = false,
        showDayHeader = false,
    ),
    HomeTransactionUiModel(
        id = "rental_fee_2026_07_13",
        date = LocalDate.of(2026, 7, 13),
        dayLabel = "Mon, Jul 13",
        daySummary = "Expenses: RM 1,286.00",
        title = "Rental Fee",
        note = "Monthly apartment rent",
        amount = "-RM 1,200.00",
        createdAt = LocalDateTime.of(2026, 7, 13, 8, 12),
        icon = Icons.Filled.Home,
        isIncome = false,
        showDayHeader = true,
    ),
    HomeTransactionUiModel(
        id = "dinner_2026_07_13",
        date = LocalDate.of(2026, 7, 13),
        dayLabel = "Mon, Jul 13",
        daySummary = "Expenses: RM 1,286.00",
        title = "Dinner",
        note = "Family meal",
        amount = "-RM 86.00",
        createdAt = LocalDateTime.of(2026, 7, 13, 20, 7),
        icon = Icons.Filled.Restaurant,
        isIncome = false,
        showDayHeader = false,
    ),
    HomeTransactionUiModel(
        id = "streaming_2026_07_12",
        date = LocalDate.of(2026, 7, 12),
        dayLabel = "Sun, Jul 12",
        daySummary = "Expenses: RM 74.80",
        title = "Streaming",
        note = "Monthly subscription",
        amount = "-RM 19.90",
        createdAt = LocalDateTime.of(2026, 7, 12, 10, 30),
        icon = Icons.Filled.Subscriptions,
        isIncome = false,
        showDayHeader = true,
    ),
    HomeTransactionUiModel(
        id = "lunch_2026_07_12",
        date = LocalDate.of(2026, 7, 12),
        dayLabel = "Sun, Jul 12",
        daySummary = "Expenses: RM 74.80",
        title = "Lunch",
        note = "Noodles and drink",
        amount = "-RM 18.90",
        createdAt = LocalDateTime.of(2026, 7, 12, 12, 48),
        icon = Icons.Filled.Fastfood,
        isIncome = false,
        showDayHeader = false,
    ),
    HomeTransactionUiModel(
        id = "parking_2026_07_12",
        date = LocalDate.of(2026, 7, 12),
        dayLabel = "Sun, Jul 12",
        daySummary = "Expenses: RM 74.80",
        title = "Parking",
        note = "Mall basement",
        amount = "-RM 36.00",
        createdAt = LocalDateTime.of(2026, 7, 12, 17, 22),
        icon = Icons.Filled.DirectionsCar,
        isIncome = false,
        showDayHeader = false,
    ),
    HomeTransactionUiModel(
        id = "freelance_2026_07_10",
        date = LocalDate.of(2026, 7, 10),
        dayLabel = "Fri, Jul 10",
        daySummary = "Income: RM 223.00",
        title = "Freelance",
        note = "Small design task",
        amount = "+RM 223.00",
        createdAt = LocalDateTime.of(2026, 7, 10, 11, 15),
        icon = Icons.Filled.Payments,
        isIncome = true,
        showDayHeader = true,
    ),
    HomeTransactionUiModel(
        id = "groceries_2026_07_10",
        date = LocalDate.of(2026, 7, 10),
        dayLabel = "Fri, Jul 10",
        daySummary = "Income: RM 223.00",
        title = "Groceries",
        note = "Weekly restock",
        amount = "-RM 96.40",
        createdAt = LocalDateTime.of(2026, 7, 10, 18, 58),
        icon = Icons.Filled.ShoppingBag,
        isIncome = false,
        showDayHeader = false,
    ),
)

private fun List<HomeTransactionUiModel>.withVisibleDayHeaders(): List<HomeTransactionUiModel> =
    mapIndexed { index, transaction ->
        transaction.copy(
            showDayHeader = index == 0 || transaction.date != this[index - 1].date,
        )
    }

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun YearMonth.asFilterLabel(): String =
    format(FilterMonthFormatter)

private fun filterTitle(
    selectedMonth: YearMonth,
    categoryQuery: String,
): String {
    val normalizedQuery = categoryQuery.trim()

    return when {
        normalizedQuery.isNotBlank() -> "$normalizedQuery in ${selectedMonth.asFilterLabel()}"
        else -> "Records for ${selectedMonth.asFilterLabel()}"
    }
}

private val FilterMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
private val CreatedAtFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy hh:mm a")

private val AppBackground = Color(0xFF08142A)
private val Panel = Color(0xE0101A34)
private val Stroke = Color(0x1AFFFFFF)
private val AccentStroke = Color(0x3893C5FD)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentGreen = Color(0xFF059669)
private val MutedText = Color(0xFF91A7C5)
private val SoftText = Color(0xFFD6E2F2)
private val ExpenseText = Color(0xFFF87171)
private val IncomeText = Color(0xFF86EFAC)

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeRoute()
}
