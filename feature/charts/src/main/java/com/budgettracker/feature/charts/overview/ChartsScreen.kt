package com.budgettracker.feature.charts.overview

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ChartsRoute(
    modifier: Modifier = Modifier,
) {
    ChartsScreen(
        expenseChartCategories = sampleExpenseChartCategories,
        expenseDetails = sampleExpenseDetails,
        modifier = modifier,
    )
}

@Composable
private fun ChartsScreen(
    expenseChartCategories: List<ChartCategoryUiModel>,
    expenseDetails: List<BudgetSpendingUiModel>,
    modifier: Modifier = Modifier,
) {
    var selectedMode by remember { mutableStateOf(ChartMode.Expenses) }
    var selectedMonth by remember { mutableStateOf(YearMonth.of(2026, 7)) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCategoryFilter by remember { mutableStateOf(false) }
    val monthlyCategoryBreakdown = selectedCategory?.let { category ->
        sampleCategoryMonthlyBreakdown.getValue(selectedMode).getValue(category)
    }
    val coloredMonthlyCategoryBreakdown = monthlyCategoryBreakdown?.withMonthlyColors()
    val displayedCategories = coloredMonthlyCategoryBreakdown?.toMonthlyChartCategories()
        ?: if (selectedMode == ChartMode.Expenses) {
        expenseChartCategories
    } else {
        sampleIncomeChartCategories
    }
    val monthlyDetails = if (selectedMode == ChartMode.Expenses) {
        expenseDetails
    } else {
        sampleIncomeSources
    }
    val displayedBudgets = coloredMonthlyCategoryBreakdown?.withTotalSharePercents() ?: monthlyDetails
    val filterOptions = monthlyDetails.map { it.name }
    val hasCategoryFilter = selectedCategory != null
    val categoryFilterTitle = selectedCategory?.let { "$it by Month" }
    val sectionTitle = categoryFilterTitle ?: if (selectedMode == ChartMode.Expenses) {
        "Budget Spending"
    } else {
        "Income Sources"
    }
    val totalAmount = coloredMonthlyCategoryBreakdown?.totalAmountLabel()
        ?: if (selectedMode == ChartMode.Expenses) "RM 2,438" else "RM 3,723"
    val totalLabel = selectedCategory
        ?: if (selectedMode == ChartMode.Expenses) "Total Spent" else "Total Income"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            ChartsTopBar(
                filterActive = hasCategoryFilter,
                onFilterClick = { showCategoryFilter = true },
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterPill(
                    label = "Income",
                    selected = selectedMode == ChartMode.Income,
                    onClick = {
                        selectedMode = ChartMode.Income
                        selectedCategory = null
                    },
                )
                FilterPill(
                    label = "Expenses",
                    selected = selectedMode == ChartMode.Expenses,
                    onClick = {
                        selectedMode = ChartMode.Expenses
                        selectedCategory = null
                    },
                )
            }
        }
        item {
            AnalyticsCard(
                categories = displayedCategories,
                monthLabel = selectedMonth.format(MonthFormatter),
                showMonthSwitcher = !hasCategoryFilter,
                onPreviousMonthClick = { selectedMonth = selectedMonth.minusMonths(1) },
                onNextMonthClick = { selectedMonth = selectedMonth.plusMonths(1) },
                totalLabel = totalLabel,
                totalAmount = totalAmount,
            )
        }
        item {
            SectionHeader(
                title = sectionTitle,
            )
        }
        item {
            BudgetSpendingCard(budgets = displayedBudgets)
        }
        item {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }

    if (showCategoryFilter) {
        CategoryFilterDialog(
            title = if (selectedMode == ChartMode.Expenses) {
                "Filter expense category"
            } else {
                "Filter income source"
            },
            selectedCategory = selectedCategory,
            categories = filterOptions,
            onCategorySelected = {
                selectedCategory = it
                showCategoryFilter = false
            },
            onClearFilter = {
                selectedCategory = null
                showCategoryFilter = false
            },
            onDismiss = { showCategoryFilter = false },
        )
    }
}

@Composable
private fun ChartsTopBar(
    filterActive: Boolean,
    onFilterClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Analytics",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
            ),
        )
        RoundIconButton(
            icon = Icons.Filled.FilterList,
            contentDescription = "Filter category",
            tint = if (filterActive) AccentBlue else Color.White,
            onClick = onFilterClick,
        )
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        color = if (selected) Color.White else MutedText,
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .border(
                width = 1.dp,
                color = if (selected) AccentBlue.copy(alpha = 0.5f) else BorderStrokeColor,
                shape = RoundedCornerShape(999.dp),
            )
            .background(if (selected) AccentBlue else Panel)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun AnalyticsCard(
    categories: List<ChartCategoryUiModel>,
    monthLabel: String,
    showMonthSwitcher: Boolean,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    totalLabel: String,
    totalAmount: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, AccentStroke, RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Panel,
                        Color(0xCC0F1A33),
                    ),
                ),
            )
            .padding(16.dp),
    ) {
        if (showMonthSwitcher) {
            MonthSwitcher(
                monthLabel = monthLabel,
                onPreviousMonthClick = onPreviousMonthClick,
                onNextMonthClick = onNextMonthClick,
            )
            Spacer(modifier = Modifier.height(18.dp))
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            DonutChart(
                categories = categories,
                modifier = Modifier.size(206.dp),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = totalLabel,
                    color = MutedText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = totalAmount,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.8).sp,
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            categories.forEach { category ->
                LegendRow(category = category)
            }
        }
    }
}

@Composable
private fun MonthSwitcher(
    monthLabel: String,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SmallIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Previous month",
            onClick = onPreviousMonthClick,
        )
        Text(
            text = monthLabel,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
        )
        SmallIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Next month",
            onClick = onNextMonthClick,
        )
    }
}

@Composable
private fun DonutChart(
    categories: List<ChartCategoryUiModel>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val strokeWidth = 26.dp.toPx()
        val topLeft = Offset(
            x = (size.width - diameter) / 2f,
            y = (size.height - diameter) / 2f,
        )
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f

        drawArc(
            color = Color.White.copy(alpha = 0.06f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
        )

        categories.forEach { category ->
            val sweepAngle = category.percent * 3.6f
            drawArc(
                color = category.color,
                startAngle = startAngle,
                sweepAngle = (sweepAngle - 3f).coerceAtLeast(1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun LegendRow(category: ChartCategoryUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(category.color),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = category.name,
                color = SoftText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "${category.percent.toInt()}%",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
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
    }
}

@Composable
private fun CategoryFilterDialog(
    title: String,
    selectedCategory: String?,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    onClearFilter: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Choose one category to compare it across recent months.",
                    color = MutedText,
                    fontSize = 12.sp,
                )
                categories.forEach { category ->
                    CategoryOptionRow(
                        category = category,
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClearFilter) {
                Text("All categories")
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
private fun CategoryOptionRow(
    category: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AccentBlue.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = category,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        if (selected) {
            Text(
                text = "Selected",
                color = Color(0xFFBFDBFE),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun BudgetSpendingCard(budgets: List<BudgetSpendingUiModel>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, BorderStrokeColor, RoundedCornerShape(24.dp))
            .background(Panel)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        budgets.forEach { budget ->
            BudgetRow(budget = budget)
        }
    }
}

@Composable
private fun BudgetRow(budget: BudgetSpendingUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(budget.color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = budget.icon,
                contentDescription = budget.name,
                tint = budget.color,
                modifier = Modifier.size(23.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budget.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = budget.amount,
                        color = MutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = "${budget.percent}%",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ProgressBar(
                percent = budget.percent,
                color = budget.color,
            )
        }
    }
}

@Composable
private fun ProgressBar(
    percent: Int,
    color: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.08f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                .height(7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(color),
        )
    }
}

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderStrokeColor, RoundedCornerShape(16.dp))
            .background(Panel.copy(alpha = 0.88f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun SmallIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BorderStrokeColor, RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
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

private data class ChartCategoryUiModel(
    val name: String,
    val percent: Float,
    val color: Color,
)

private data class BudgetSpendingUiModel(
    val name: String,
    val amount: String,
    val percent: Int,
    val color: Color,
    val icon: ImageVector,
)

private enum class ChartMode {
    Income,
    Expenses,
}

private fun List<BudgetSpendingUiModel>.toMonthlyChartCategories(): List<ChartCategoryUiModel> {
    val amounts = map { it.amount.toAmountValue() }
    val total = amounts.sum().takeIf { it > 0f } ?: 1f

    return mapIndexed { index, item ->
        ChartCategoryUiModel(
            name = item.name,
            percent = amounts[index] / total * 100f,
            color = item.color,
        )
    }
}

private fun List<BudgetSpendingUiModel>.totalAmountLabel(): String {
    val total = sumOf { it.amount.toAmountValue().toDouble() }
    val formattedTotal = "%,.0f".format(Locale.ENGLISH, total)

    return "RM $formattedTotal"
}

private fun List<BudgetSpendingUiModel>.withTotalSharePercents(): List<BudgetSpendingUiModel> {
    val amounts = map { it.amount.toAmountValue() }
    val total = amounts.sum().takeIf { it > 0f } ?: 1f

    return mapIndexed { index, item ->
        item.copy(percent = (amounts[index] / total * 100f).toInt().coerceIn(0, 100))
    }
}

private fun List<BudgetSpendingUiModel>.withMonthlyColors(): List<BudgetSpendingUiModel> =
    mapIndexed { index, item ->
        item.copy(color = FilteredMonthColors[index % FilteredMonthColors.size])
    }

private fun String.toAmountValue(): Float =
    filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f

private val sampleExpenseChartCategories = listOf(
    ChartCategoryUiModel("Rent", 28f, Color(0xFF3B82F6)),
    ChartCategoryUiModel("Food", 20f, Color(0xFF2DD4BF)),
    ChartCategoryUiModel("Transport", 16f, Color(0xFF7C3AED)),
    ChartCategoryUiModel("Other", 36f, Color(0xFF64748B)),
)

private val sampleIncomeChartCategories = listOf(
    ChartCategoryUiModel("Salary", 72f, Color(0xFF22C55E)),
    ChartCategoryUiModel("Freelance", 14f, Color(0xFF38BDF8)),
    ChartCategoryUiModel("Rewards", 9f, Color(0xFFA78BFA)),
    ChartCategoryUiModel("Other", 5f, Color(0xFF64748B)),
)

private val sampleExpenseDetails = listOf(
    BudgetSpendingUiModel(
        name = "Rent",
        amount = "RM 1,200",
        percent = 49,
        color = Color(0xFF93C5FD),
        icon = Icons.Filled.Home,
    ),
    BudgetSpendingUiModel(
        name = "Food",
        amount = "RM 482",
        percent = 20,
        color = Color(0xFFFDE68A),
        icon = Icons.Filled.ShoppingBag,
    ),
    BudgetSpendingUiModel(
        name = "Transport",
        amount = "RM 390",
        percent = 16,
        color = Color(0xFF86EFAC),
        icon = Icons.Filled.DirectionsCar,
    ),
    BudgetSpendingUiModel(
        name = "Subscription",
        amount = "RM 438",
        percent = 18,
        color = Color(0xFFFACC15),
        icon = Icons.Filled.Subscriptions,
    ),
    BudgetSpendingUiModel(
        name = "Healthcare",
        amount = "RM 244",
        percent = 10,
        color = Color(0xFFFB7185),
        icon = Icons.Filled.LocalHospital,
    ),
    BudgetSpendingUiModel(
        name = "Other",
        amount = "RM 195",
        percent = 8,
        color = Color(0xFF94A3B8),
        icon = Icons.Filled.Redeem,
    ),
)

private val sampleIncomeSources = listOf(
    BudgetSpendingUiModel(
        name = "Salary",
        amount = "RM 3,500",
        percent = 94,
        color = Color(0xFF86EFAC),
        icon = Icons.Filled.Payments,
    ),
    BudgetSpendingUiModel(
        name = "Freelance",
        amount = "RM 223",
        percent = 6,
        color = Color(0xFF7DD3FC),
        icon = Icons.Filled.Work,
    ),
    BudgetSpendingUiModel(
        name = "Cashback",
        amount = "RM 35",
        percent = 1,
        color = Color(0xFFC4B5FD),
        icon = Icons.Filled.Redeem,
    ),
    BudgetSpendingUiModel(
        name = "Interest",
        amount = "RM 18",
        percent = 1,
        color = Color(0xFFFDE68A),
        icon = Icons.Filled.Savings,
    ),
    BudgetSpendingUiModel(
        name = "Investment Return",
        amount = "RM 12",
        percent = 1,
        color = Color(0xFF5EEAD4),
        icon = Icons.AutoMirrored.Filled.TrendingUp,
    ),
    BudgetSpendingUiModel(
        name = "Other Income",
        amount = "RM 8",
        percent = 1,
        color = Color(0xFF94A3B8),
        icon = Icons.Filled.ShoppingBag,
    ),
)

private val sampleCategoryMonthlyBreakdown = mapOf(
    ChartMode.Expenses to mapOf(
        "Rent" to listOf(
            BudgetSpendingUiModel("July 2026", "RM 1,200", 100, Color(0xFF93C5FD), Icons.Filled.Home),
            BudgetSpendingUiModel("June 2026", "RM 1,200", 100, Color(0xFF93C5FD), Icons.Filled.Home),
            BudgetSpendingUiModel("May 2026", "RM 1,180", 98, Color(0xFF93C5FD), Icons.Filled.Home),
        ),
        "Food" to listOf(
            BudgetSpendingUiModel("July 2026", "RM 482", 100, Color(0xFFFDE68A), Icons.Filled.ShoppingBag),
            BudgetSpendingUiModel("June 2026", "RM 516", 107, Color(0xFFFDE68A), Icons.Filled.ShoppingBag),
            BudgetSpendingUiModel("May 2026", "RM 438", 91, Color(0xFFFDE68A), Icons.Filled.ShoppingBag),
        ),
        "Transport" to listOf(
            BudgetSpendingUiModel("July 2026", "RM 390", 100, Color(0xFF86EFAC), Icons.Filled.DirectionsCar),
            BudgetSpendingUiModel("June 2026", "RM 342", 88, Color(0xFF86EFAC), Icons.Filled.DirectionsCar),
            BudgetSpendingUiModel("May 2026", "RM 418", 107, Color(0xFF86EFAC), Icons.Filled.DirectionsCar),
        ),
        "Subscription" to listOf(
            BudgetSpendingUiModel("July 2026", "RM 438", 100, Color(0xFFFACC15), Icons.Filled.Subscriptions),
            BudgetSpendingUiModel("June 2026", "RM 299", 68, Color(0xFFFACC15), Icons.Filled.Subscriptions),
            BudgetSpendingUiModel("May 2026", "RM 279", 64, Color(0xFFFACC15), Icons.Filled.Subscriptions),
        ),
        "Healthcare" to listOf(
            BudgetSpendingUiModel("July 2026", "RM 244", 100, Color(0xFFFB7185), Icons.Filled.LocalHospital),
            BudgetSpendingUiModel("June 2026", "RM 86", 35, Color(0xFFFB7185), Icons.Filled.LocalHospital),
            BudgetSpendingUiModel("May 2026", "RM 132", 54, Color(0xFFFB7185), Icons.Filled.LocalHospital),
        ),
        "Other" to listOf(
            BudgetSpendingUiModel("July 2026", "RM 195", 100, Color(0xFF94A3B8), Icons.Filled.Redeem),
            BudgetSpendingUiModel("June 2026", "RM 168", 86, Color(0xFF94A3B8), Icons.Filled.Redeem),
            BudgetSpendingUiModel("May 2026", "RM 211", 108, Color(0xFF94A3B8), Icons.Filled.Redeem),
        ),
    ),
    ChartMode.Income to mapOf(
        "Salary" to listOf(
            BudgetSpendingUiModel("July 2026", "RM 3,500", 100, Color(0xFF86EFAC), Icons.Filled.Payments),
            BudgetSpendingUiModel("June 2026", "RM 3,500", 100, Color(0xFF86EFAC), Icons.Filled.Payments),
            BudgetSpendingUiModel("May 2026", "RM 3,500", 100, Color(0xFF86EFAC), Icons.Filled.Payments),
        ),
        "Freelance" to listOf(
            BudgetSpendingUiModel("July 2026", "RM 223", 100, Color(0xFF7DD3FC), Icons.Filled.Work),
            BudgetSpendingUiModel("June 2026", "RM 360", 100, Color(0xFF7DD3FC), Icons.Filled.Work),
            BudgetSpendingUiModel("May 2026", "RM 140", 63, Color(0xFF7DD3FC), Icons.Filled.Work),
        ),
        "Cashback" to listOf(
            BudgetSpendingUiModel("July 2026", "RM 35", 100, Color(0xFFC4B5FD), Icons.Filled.Redeem),
            BudgetSpendingUiModel("June 2026", "RM 24", 69, Color(0xFFC4B5FD), Icons.Filled.Redeem),
            BudgetSpendingUiModel("May 2026", "RM 42", 100, Color(0xFFC4B5FD), Icons.Filled.Redeem),
        ),
        "Interest" to listOf(
            BudgetSpendingUiModel("July 2026", "RM 18", 100, Color(0xFFFDE68A), Icons.Filled.Savings),
            BudgetSpendingUiModel("June 2026", "RM 17", 94, Color(0xFFFDE68A), Icons.Filled.Savings),
            BudgetSpendingUiModel("May 2026", "RM 16", 89, Color(0xFFFDE68A), Icons.Filled.Savings),
        ),
        "Investment Return" to listOf(
            BudgetSpendingUiModel("July 2026", "RM 12", 100, Color(0xFF5EEAD4), Icons.AutoMirrored.Filled.TrendingUp),
            BudgetSpendingUiModel("June 2026", "RM 48", 100, Color(0xFF5EEAD4), Icons.AutoMirrored.Filled.TrendingUp),
            BudgetSpendingUiModel("May 2026", "RM 0", 0, Color(0xFF5EEAD4), Icons.AutoMirrored.Filled.TrendingUp),
        ),
        "Other Income" to listOf(
            BudgetSpendingUiModel("July 2026", "RM 8", 100, Color(0xFF94A3B8), Icons.Filled.ShoppingBag),
            BudgetSpendingUiModel("June 2026", "RM 15", 100, Color(0xFF94A3B8), Icons.Filled.ShoppingBag),
            BudgetSpendingUiModel("May 2026", "RM 10", 67, Color(0xFF94A3B8), Icons.Filled.ShoppingBag),
        ),
    ),
)

private val AppBackground = Color(0xFF08142A)
private val Panel = Color(0xE0101A34)
private val BorderStrokeColor = Color(0x1AFFFFFF)
private val AccentStroke = Color(0x3893C5FD)
private val AccentBlue = Color(0xFF2563EB)
private val MutedText = Color(0xFF91A7C5)
private val SoftText = Color(0xFFD6E2F2)
private val FilteredMonthColors = listOf(
    Color(0xFF60A5FA),
    Color(0xFF2DD4BF),
    Color(0xFFA78BFA),
    Color(0xFFFBBF24),
    Color(0xFFFB7185),
    Color(0xFF94A3B8),
)
private val MonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

@Preview
@Composable
private fun ChartsScreenPreview() {
    ChartsRoute()
}
