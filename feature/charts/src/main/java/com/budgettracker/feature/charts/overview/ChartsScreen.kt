package com.budgettracker.feature.charts.overview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.budgettracker.core.domain.money.AmountFormatter
import com.budgettracker.core.model.Category
import com.budgettracker.core.model.Money
import com.budgettracker.core.model.Transaction
import com.budgettracker.core.model.TransactionType
import com.budgettracker.core.ui.category.asCategoryIcon
import com.budgettracker.core.ui.category.toCategoryColor
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

@Composable
fun ChartsRoute(
    modifier: Modifier = Modifier,
    viewModel: ChartsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChartsScreen(
        uiState = uiState,
        modifier = modifier,
    )
}

@Composable
private fun ChartsScreen(
    uiState: ChartsUiState,
    modifier: Modifier = Modifier,
) {
    var selectedMode by remember { mutableStateOf(ChartMode.Expenses) }
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCategoryFilter by remember { mutableStateOf(false) }
    val transactions = uiState.transactions
    val categories = uiState.categories
    val monthlyDetails by remember(transactions, categories, selectedMode, selectedMonth) {
        derivedStateOf {
            ChartsUiState(
                transactions = transactions,
                categories = categories,
            ).toMonthlyDetails(
                selectedMode = selectedMode,
                selectedMonth = selectedMonth,
            )
        }
    }
    val monthlyCategoryBreakdown by remember(transactions, categories, monthlyDetails, selectedMode, selectedMonth, selectedCategory) {
        derivedStateOf {
            selectedCategory?.let { category ->
                ChartsUiState(
                    transactions = transactions,
                    categories = categories,
                ).toCategoryMonthlyBreakdown(
                    selectedMode = selectedMode,
                    selectedMonth = selectedMonth,
                    selectedCategory = category,
                    monthlyDetails = monthlyDetails,
                )
            }
        }
    }
    val coloredMonthlyCategoryBreakdown = monthlyCategoryBreakdown?.withMonthlyColors()
    val displayedCategories = coloredMonthlyCategoryBreakdown?.toMonthlyChartCategories()
        ?: monthlyDetails.toTopMonthlyChartCategories()
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
        ?: monthlyDetails.totalAmountLabel()
    val totalLabel = selectedCategory
        ?: if (selectedMode == ChartMode.Expenses) "Total Spent" else "Total Income"
    val chartAnimationKey = ChartAnimationKey(
        selectedMode = selectedMode,
        selectedMonth = selectedMonth,
        selectedCategory = selectedCategory,
    )

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
                chartAnimationKey = chartAnimationKey,
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
            BudgetSpendingCard(
                budgets = displayedBudgets,
            )
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
    chartAnimationKey: ChartAnimationKey,
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
                animationKey = chartAnimationKey,
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
        if (categories.isEmpty()) {
            Text(
                text = "No data for this month",
                color = MutedText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                categories.forEach { category ->
                    LegendRow(category = category)
                }
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
    animationKey: ChartAnimationKey,
    modifier: Modifier = Modifier,
) {
    val revealProgress = remember { Animatable(0f) }
    LaunchedEffect(animationKey) {
        revealProgress.snapTo(0f)
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val strokeWidth = 26.dp.toPx()
        val topLeft = Offset(
            x = (size.width - diameter) / 2f,
            y = (size.height - diameter) / 2f,
        )
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f
        var remainingRevealAngle = 360f * revealProgress.value

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
            val visibleSweepAngle = min(sweepAngle, remainingRevealAngle).coerceAtLeast(0f)
            val gapAngle = if (sweepAngle >= 6f) 3f else 0f
            val drawnSweepAngle = (visibleSweepAngle - gapAngle).coerceAtLeast(0f)
            if (drawnSweepAngle > 0f) {
                drawArc(
                    color = category.color,
                    startAngle = startAngle,
                    sweepAngle = drawnSweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                )
            }
            startAngle += sweepAngle
            remainingRevealAngle -= sweepAngle
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
private fun BudgetSpendingCard(
    budgets: List<BudgetSpendingUiModel>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, BorderStrokeColor, RoundedCornerShape(24.dp))
            .background(Panel)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (budgets.isEmpty()) {
            Text(
                text = "No transactions found for this selection.",
                color = MutedText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(4.dp),
            )
        } else {
            budgets.forEach { budget ->
                BudgetRow(
                    budget = budget,
                )
            }
        }
    }
}

@Composable
private fun BudgetRow(
    budget: BudgetSpendingUiModel,
) {
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
    val fillProgress by animateFloatAsState(
        targetValue = percent / 100f,
        animationSpec = tween(
            durationMillis = 700,
            easing = FastOutSlowInEasing,
        ),
        label = "BudgetProgress",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.08f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fillProgress.coerceIn(0f, 1f))
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

@Immutable
private data class ChartCategoryUiModel(
    val name: String,
    val percent: Float,
    val color: Color,
)

@Immutable
private data class ChartAnimationKey(
    val selectedMode: ChartMode,
    val selectedMonth: YearMonth,
    val selectedCategory: String?,
)

@Immutable
private data class BudgetSpendingUiModel(
    val name: String,
    val amount: String,
    val minorUnits: Long,
    val percent: Int,
    val color: Color,
    val icon: ImageVector,
    val categoryIds: Set<String?>,
)

private enum class ChartMode {
    Income,
    Expenses,
}

private val ChartMode.transactionType: TransactionType
    get() = when (this) {
        ChartMode.Income -> TransactionType.Income
        ChartMode.Expenses -> TransactionType.Expense
    }

private fun ChartsUiState.toMonthlyDetails(
    selectedMode: ChartMode,
    selectedMonth: YearMonth,
): List<BudgetSpendingUiModel> {
    val categoryById = categories.associateBy { it.id }
    val selectedType = selectedMode.transactionType

    return transactions
        .filter { transaction ->
            transaction.type == selectedType && transaction.dateTime.toYearMonth() == selectedMonth
        }
        .groupBy { it.categoryId }
        .map { (categoryId, transactions) ->
            val category = categoryId?.let(categoryById::get)
            transactions.toBudgetSpendingUiModel(
                category = category,
                categoryIds = setOf(categoryId),
            )
        }
        .sortedByDescending { it.minorUnits }
        .withTotalSharePercents()
}

private fun ChartsUiState.toCategoryMonthlyBreakdown(
    selectedMode: ChartMode,
    selectedMonth: YearMonth,
    selectedCategory: String,
    monthlyDetails: List<BudgetSpendingUiModel> = toMonthlyDetails(
        selectedMode = selectedMode,
        selectedMonth = selectedMonth,
    ),
): List<BudgetSpendingUiModel> {
    val selectedType = selectedMode.transactionType
    val category = categories.firstOrNull { category ->
        category.type == selectedType && category.name == selectedCategory
    }
    val selectedCategoryIds = monthlyDetails.firstOrNull { it.name == selectedCategory }?.categoryIds ?: setOf(category?.id)

    return (0L..2L).map { monthsAgo ->
        val month = selectedMonth.minusMonths(monthsAgo)
        transactions
            .filter { transaction ->
                transaction.type == selectedType &&
                    transaction.dateTime.toYearMonth() == month &&
                    transaction.categoryId in selectedCategoryIds
            }
            .toBudgetSpendingUiModel(
                category = category,
                name = month.format(MonthFormatter),
                categoryIds = selectedCategoryIds,
            )
    }.withTotalSharePercents()
}

private fun List<Transaction>.toBudgetSpendingUiModel(
    category: Category?,
    name: String = category?.name ?: "Uncategorized",
    categoryIds: Set<String?>,
): BudgetSpendingUiModel {
    val minorUnits = sumOf { it.amount.minorUnits }

    return BudgetSpendingUiModel(
        name = name,
        amount = minorUnits.toMoneyLabel(),
        minorUnits = minorUnits,
        percent = 0,
        color = category?.color?.toCategoryColor() ?: Color(0xFF64748B),
        icon = category?.icon?.asCategoryIcon() ?: Icons.Filled.Category,
        categoryIds = categoryIds,
    )
}

private fun List<BudgetSpendingUiModel>.groupOverflowCategories(): List<BudgetSpendingUiModel> {
    if (size <= MaxChartCategories) return this

    val visibleCategories = take(MaxChartCategories - 1)
    val overflowCategories = drop(MaxChartCategories - 1)
    val overflowMinorUnits = overflowCategories.sumOf { it.minorUnits }
    val otherCategory = BudgetSpendingUiModel(
        name = "Other",
        amount = overflowMinorUnits.toMoneyLabel(),
        minorUnits = overflowMinorUnits,
        percent = 0,
        color = Color(0xFF94A3B8),
        icon = Icons.Filled.Category,
        categoryIds = overflowCategories.flatMap { it.categoryIds }.toSet(),
    )

    return visibleCategories + otherCategory
}

private fun List<BudgetSpendingUiModel>.toMonthlyChartCategories(): List<ChartCategoryUiModel> {
    val total = sumOf { it.minorUnits }.takeIf { it > 0L } ?: 1L

    return map { item ->
        ChartCategoryUiModel(
            name = item.name,
            percent = item.minorUnits.toFloat() / total * 100f,
            color = item.color,
        )
    }
}

private fun List<BudgetSpendingUiModel>.toTopMonthlyChartCategories(): List<ChartCategoryUiModel> =
    groupOverflowCategories().toMonthlyChartCategories()

private fun List<BudgetSpendingUiModel>.totalAmountLabel(): String {
    val total = sumOf { it.minorUnits }
    return total.toMoneyLabel()
}

private fun List<BudgetSpendingUiModel>.withTotalSharePercents(): List<BudgetSpendingUiModel> {
    val total = sumOf { it.minorUnits }.takeIf { it > 0L } ?: 1L

    return map { item ->
        item.copy(percent = (item.minorUnits.toFloat() / total * 100f).toInt().coerceIn(0, 100))
    }
}

private fun List<BudgetSpendingUiModel>.withMonthlyColors(): List<BudgetSpendingUiModel> =
    mapIndexed { index, item ->
        item.copy(color = FilteredMonthColors[index % FilteredMonthColors.size])
    }

private fun Long.toMoneyLabel(): String =
    "RM ${AmountFormatter.formatPlainGrouped(Money(this))}"

private fun java.time.Instant.toYearMonth(): YearMonth =
    YearMonth.from(atZone(ZoneId.systemDefault()).toLocalDate())

private val AppBackground = Color(0xFF08142A)
private val Panel = Color(0xE0101A34)
private val BorderStrokeColor = Color(0x1AFFFFFF)
private val AccentStroke = Color(0x3893C5FD)
private val AccentBlue = Color(0xFF2563EB)
private val MutedText = Color(0xFF91A7C5)
private val SoftText = Color(0xFFD6E2F2)
private const val MaxChartCategories = 5
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
    ChartsScreen(uiState = ChartsUiState(isLoading = false))
}
