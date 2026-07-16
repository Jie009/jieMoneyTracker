package com.budgettracker.feature.addtransaction.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.core.model.AmountInputMode
import com.budgettracker.core.model.Category
import com.budgettracker.core.model.TransactionType
import com.budgettracker.core.ui.category.CategoryEditDialog
import com.budgettracker.core.ui.category.CategoryManagerScreen
import com.budgettracker.core.ui.category.CategoryUiModel
import com.budgettracker.core.ui.category.MaterialIconOptions
import com.budgettracker.core.ui.category.MaterialIconSections
import com.budgettracker.core.ui.category.asCategoryUiModel
import com.budgettracker.core.ui.category.defaultCategoryUiModels
import com.budgettracker.core.ui.category.normalizedCategoryColor
import com.budgettracker.core.ui.category.toCategoryColor
import com.budgettracker.core.ui.category.withEditableCategory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class AddTransactionSaveResult(
    val category: String,
    val note: String,
    val amount: String,
    val transactionType: TransactionType,
    val dateMillis: Long,
)

@Composable
fun AddTransactionRoute(
    modifier: Modifier = Modifier,
    transactionId: String? = null,
    prefillAmountInput: String? = null,
    prefillTransactionType: TransactionType? = null,
    prefillNote: String? = null,
    onCancel: () -> Unit = {},
    onSaved: () -> Unit = {},
    viewModel: AddTransactionViewModel = hiltViewModel(),
) {
    LaunchedEffect(transactionId, prefillAmountInput, prefillTransactionType, prefillNote) {
        val prefill = prefillAmountInput
            ?.takeIf { it.isNotBlank() }
            ?.let { amount ->
                AddTransactionPrefill(
                    amountInput = amount,
                    transactionType = prefillTransactionType ?: TransactionType.Expense,
                    note = prefillNote.orEmpty(),
                )
            }
        viewModel.load(
            transactionId = transactionId,
            prefill = prefill,
        )
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val amountInputMode by viewModel.amountInputMode.collectAsStateWithLifecycle()
    val persistedCategories by viewModel.categories.collectAsStateWithLifecycle()
    val categories = remember(persistedCategories, state.transactionType) {
        persistedCategories
            .filter { it.type == state.transactionType }
            .map { it.asCategoryUiModel() }
            .ifEmpty { defaultCategoryUiModels() }
    }
    val selectedCategoryId = state.selectedCategoryId
        ?.takeIf { selectedId -> categories.any { it.id == selectedId } }
        ?: categories.firstOrNull()?.id.orEmpty()
    var noteDraft by remember { mutableStateOf("") }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showCategoryManager by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategoryId by remember { mutableStateOf<String?>(null) }
    var categoryDraftName by remember { mutableStateOf("") }
    var categoryDraftIconName by remember { mutableStateOf(MaterialIconOptions.first().name) }
    var categoryDraftColor by remember { mutableStateOf("#64748B") }
    val screenTitle = if (transactionId == null) "Add transaction" else "Edit transaction"

    fun saveTransaction() {
        viewModel.updateSelectedCategory(selectedCategoryId)
        val normalizedAmount = state.amountInput
            .asAmountText(amountInputMode)
            .asSavedAmountText()
        if (normalizedAmount.isBlank()) return
        viewModel.updateAmountInput(normalizedAmount)
        viewModel.save(onSaved = onSaved)
    }

    fun openCategoryDialog(category: CategoryUiModel?) {
        editingCategoryId = category?.id
        categoryDraftName = category?.name.orEmpty()
        categoryDraftIconName = category?.icon?.name ?: MaterialIconOptions.first().name
        categoryDraftColor = category?.color ?: "#64748B"
        showCategoryDialog = true
    }

    fun closeCategoryDialog() {
        showCategoryDialog = false
        editingCategoryId = null
        categoryDraftName = ""
        categoryDraftIconName = MaterialIconOptions.first().name
        categoryDraftColor = "#64748B"
    }

    if (showCategoryManager) {
        CategoryManagerScreen(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onBack = { showCategoryManager = false },
            onCategoryClick = { category ->
                viewModel.updateSelectedCategory(category.id)
                showCategoryManager = false
            },
            onAddCategoryClick = { openCategoryDialog(null) },
            onCategoryEditClick = ::openCategoryDialog,
            modifier = modifier,
        )
    } else {
        AddTransactionScreen(
            title = screenTitle,
            transactionType = state.transactionType,
            amountInputMode = amountInputMode,
            amountText = state.amountInput.asAmountText(amountInputMode),
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            noteText = state.note,
            dateText = state.dateMillis.asDateLabel(),
            noteDraft = noteDraft,
            showNoteDialog = showNoteDialog,
            showDateDialog = showDateDialog,
            selectedDateMillis = state.dateMillis,
            onTransactionTypeChange = viewModel::updateType,
            onNumberClick = { value ->
                viewModel.updateAmountInput(
                    if (amountInputMode == AmountInputMode.AutoCents) {
                        state.amountInput.appendAutoCentsInput(value)
                    } else {
                        state.amountInput.appendAmountInput(value)
                    },
                )
            },
            onDecimalClick = {
                if (amountInputMode == AmountInputMode.NormalDecimal && !state.amountInput.contains('.')) {
                    viewModel.updateAmountInput(if (state.amountInput.isEmpty()) "0." else "${state.amountInput}.")
                }
            },
            onBackspaceClick = {
                viewModel.updateAmountInput(
                    if (amountInputMode == AmountInputMode.AutoCents) {
                        state.amountInput.dropLastAutoCentsInput()
                    } else {
                        state.amountInput.dropLast(1).trimEnd('.')
                    },
                )
            },
            onCategorySelected = viewModel::updateSelectedCategory,
            onManageCategoriesClick = { showCategoryManager = true },
            onNoteClick = {
                noteDraft = state.note
                showNoteDialog = true
            },
            onNoteDraftChange = { noteDraft = it },
            onNoteDialogDismiss = { showNoteDialog = false },
            onNoteSave = {
                viewModel.updateNote(noteDraft.trim())
                showNoteDialog = false
            },
            onDateClick = { showDateDialog = true },
            onDateDialogDismiss = { showDateDialog = false },
            onDateSelected = viewModel::updateDateMillis,
            onCancel = onCancel,
            onSave = ::saveTransaction,
            modifier = modifier,
        )
    }

    if (showCategoryDialog) {
        CategoryEditDialog(
            title = if (editingCategoryId == null) "Add category" else "Edit category",
            name = categoryDraftName,
            color = categoryDraftColor,
            selectedIconName = categoryDraftIconName,
            iconSections = MaterialIconSections,
            onNameChange = { categoryDraftName = it },
            onColorChange = { categoryDraftColor = it },
            onIconSelected = { categoryDraftIconName = it },
            onDismiss = ::closeCategoryDialog,
            onSave = {
                val trimmedName = categoryDraftName.trim()
                if (trimmedName.isBlank()) return@CategoryEditDialog

                val selectedIcon = MaterialIconOptions.first { it.name == categoryDraftIconName }
                val currentEditingId = editingCategoryId
                if (currentEditingId == null) {
                    viewModel.createCategory(
                        name = trimmedName,
                        icon = selectedIcon.name,
                        color = categoryDraftColor.normalizedCategoryColor(),
                        type = state.transactionType,
                    )
                } else {
                    viewModel.updateCategory(
                        id = currentEditingId,
                        name = trimmedName,
                        icon = selectedIcon.name,
                        color = categoryDraftColor.normalizedCategoryColor(),
                    )
                }
                closeCategoryDialog()
            },
        )
    }
}

@Composable
private fun AddTransactionScreen(
    title: String,
    transactionType: TransactionType,
    amountInputMode: AmountInputMode,
    amountText: String,
    categories: List<CategoryUiModel>,
    selectedCategoryId: String,
    noteText: String,
    dateText: String,
    noteDraft: String,
    showNoteDialog: Boolean,
    showDateDialog: Boolean,
    selectedDateMillis: Long,
    onTransactionTypeChange: (TransactionType) -> Unit,
    onNumberClick: (String) -> Unit,
    onDecimalClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onManageCategoriesClick: () -> Unit,
    onNoteClick: () -> Unit,
    onNoteDraftChange: (String) -> Unit,
    onNoteDialogDismiss: () -> Unit,
    onNoteSave: () -> Unit,
    onDateClick: () -> Unit,
    onDateDialogDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(top = 18.dp, bottom = 12.dp),
        ) {
            Header(
                title = title,
                onCancel = onCancel,
                onSave = onSave,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TypeSwitcher(
                selectedType = transactionType,
                onSelectedTypeChange = onTransactionTypeChange,
            )
            Spacer(modifier = Modifier.height(20.dp))
            AmountCard(amountText = amountText)
            Spacer(modifier = Modifier.weight(1f))
            CategorySection(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = onCategorySelected,
                onManageCategoriesClick = onManageCategoriesClick,
            )
            Spacer(modifier = Modifier.height(14.dp))
            NoteDateRow(
                noteText = noteText,
                dateText = dateText,
                onNoteClick = onNoteClick,
                onDateClick = onDateClick,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Keypad(
                amountInputMode = amountInputMode,
                onNumberClick = onNumberClick,
                onDecimalClick = onDecimalClick,
                onBackspaceClick = onBackspaceClick,
                onSave = onSave,
            )
        }

        if (showNoteDialog) {
            NoteDialog(
                noteDraft = noteDraft,
                onNoteDraftChange = onNoteDraftChange,
                onDismiss = onNoteDialogDismiss,
                onSave = onNoteSave,
            )
        }

        if (showDateDialog) {
            TransactionDateDialog(
                selectedDateMillis = selectedDateMillis,
                onDateSelected = onDateSelected,
                onDismiss = onDateDialogDismiss,
            )
        }
    }
}

@Composable
private fun Header(
    title: String,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        PillButton(
            text = "Cancel",
            onClick = onCancel,
        )
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            ),
        )
        PillButton(
            text = "Save",
            onClick = onSave,
        )
    }
}

@Composable
private fun TypeSwitcher(
    selectedType: TransactionType,
    onSelectedTypeChange: (TransactionType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Stroke, RoundedCornerShape(18.dp))
            .background(Panel),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        TransactionTypeSegment(
            text = "Expense",
            selected = selectedType == TransactionType.Expense,
            onClick = { onSelectedTypeChange(TransactionType.Expense) },
            modifier = Modifier.weight(1f),
        )
        TransactionTypeSegment(
            text = "Income",
            selected = selectedType == TransactionType.Income,
            onClick = { onSelectedTypeChange(TransactionType.Income) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TransactionTypeSegment(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(5.dp)
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Accent else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else MutedText,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun AmountCard(amountText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Amount",
            color = MutedText,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "RM",
                color = Color.White,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = amountText,
                color = Color.White,
                fontSize = 40.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CategorySection(
    categories: List<CategoryUiModel>,
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
    onManageCategoriesClick: () -> Unit,
) {
    val pinnedSelectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    val displayedCategories = if (pinnedSelectedCategory != null && categories.take(4).none { it.id == selectedCategoryId }) {
        listOf(pinnedSelectedCategory) + categories.filterNot { it.id == selectedCategoryId }.take(3)
    } else {
        categories.take(4)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Category",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        MenuIconButton(onClick = onManageCategoriesClick)
    }
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        displayedCategories.forEach { category ->
            CategoryChip(
                label = category.name,
                icon = category.icon.imageVector,
                color = category.color,
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MenuIconButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(ButtonPanel)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "Manage categories",
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun CategoryChip(
    label: String,
    icon: ImageVector,
    color: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColor = color.toCategoryColor()
    Column(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = if (selected) categoryColor else Stroke,
                shape = RoundedCornerShape(18.dp),
            )
            .background(if (selected) categoryColor.copy(alpha = 0.18f) else Panel)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) categoryColor else categoryColor.copy(alpha = 0.9f),
            modifier = Modifier
                .size(23.dp),
        )
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFFD6E2F2),
            fontSize = 11.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun NoteDateRow(
    noteText: String,
    dateText: String,
    onNoteClick: () -> Unit,
    onDateClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InfoField(
            label = "Note",
            value = noteText.ifBlank { "Add note" },
            onClick = onNoteClick,
            modifier = Modifier.weight(1f),
        )
        InfoField(
            label = "Date",
            value = dateText,
            onClick = onDateClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InfoField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Stroke, RoundedCornerShape(16.dp))
            .background(Panel)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = value,
            color = MutedText,
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f),
        )
    }
}

@Composable
private fun NoteDialog(
    noteDraft: String,
    onNoteDraftChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                text = "Add note",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            TextField(
                value = noteDraft,
                onValueChange = onNoteDraftChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter note") },
                singleLine = false,
                maxLines = 4,
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDateDialog(
    selectedDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis,
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let(onDateSelected)
                    onDismiss()
                },
            ) {
                Text("Save")
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
private fun Keypad(
    amountInputMode: AmountInputMode,
    onNumberClick: (String) -> Unit,
    onDecimalClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(224.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KeypadRow(
                values = listOf("1", "2", "3"),
                onValueClick = onNumberClick,
            )
            KeypadRow(
                values = listOf("4", "5", "6"),
                onValueClick = onNumberClick,
            )
            KeypadRow(
                values = listOf("7", "8", "9"),
                onValueClick = onNumberClick,
            )
            if (amountInputMode == AmountInputMode.AutoCents) {
                KeypadRow(
                    values = listOf("0", "00"),
                    weights = listOf(2f, 1f),
                    onValueClick = onNumberClick,
                )
            } else {
                KeypadRow(
                    values = listOf(".", "0", "00"),
                    onValueClick = { value ->
                        if (value == ".") onDecimalClick() else onNumberClick(value)
                    },
                )
            }
        }
        Column(
            modifier = Modifier
                .width(70.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BackspaceKey(
                onClick = onBackspaceClick,
                modifier = Modifier.fillMaxWidth(),
            )
            SaveKey(
                onSave = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

@Composable
private fun KeypadRow(
    values: List<String>,
    weights: List<Float> = values.map { 1f },
    onValueClick: (String) -> Unit,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        values.forEachIndexed { index, value ->
            if (value.isBlank()) {
                Spacer(modifier = Modifier.weight(weights.getOrElse(index) { 1f }))
            } else {
                KeypadButton(
                    text = value,
                    onClick = { onValueClick(value) },
                    modifier = Modifier.weight(weights.getOrElse(index) { 1f }),
                )
            }
        }
        trailing?.invoke(this)
    }
}

@Composable
private fun BackspaceKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(KeyPanel)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Delete",
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(KeyPanel)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun SaveKey(
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Success)
            .clickable(onClick = onSave),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Save",
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun PillButton(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = ButtonPanel,
        border = BorderStroke(0.dp, Color.Transparent),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun String.asAmountText(amountInputMode: AmountInputMode): String {
    if (amountInputMode == AmountInputMode.AutoCents) return asAutoCentsAmountText()
    if (isEmpty()) return "0.00"

    val parts = split('.', limit = 2)
    val major = parts.firstOrNull()
        ?.filter { it.isDigit() }
        ?.trimStart('0')
        ?.ifEmpty { "0" }
        ?: "0"

    if (!contains('.')) return "$major.00".withGroupedMajor()

    val minor = parts.getOrNull(1)
        ?.filter { it.isDigit() }
        .orEmpty()
        .padEnd(2, '0')
        .take(2)

    return "$major.$minor".withGroupedMajor()
}

private fun String.asAutoCentsAmountText(): String {
    val digits = filter(Char::isDigit)
        .trimStart('0')
    val minorUnits = digits.toLongOrNull() ?: 0L
    val major = minorUnits / 100
    val minor = minorUnits % 100

    return if (major == 0L && minor == 0L) {
        "0.00"
    } else {
        "$major.${minor.toString().padStart(2, '0')}".withGroupedMajor()
    }
}

private fun String.withGroupedMajor(): String {
    val parts = split('.', limit = 2)
    val major = parts.firstOrNull().orEmpty()
    val groupedMajor = major
        .filter(Char::isDigit)
        .ifBlank { "0" }
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
    val minor = parts.getOrNull(1) ?: return groupedMajor

    return "$groupedMajor.$minor"
}

private fun String.asSavedAmountText(): String {
    if (isBlank()) return ""

    val parts = split('.', limit = 2)
    val major = parts.firstOrNull()
        ?.filter { it.isDigit() }
        ?.trimStart('0')
        ?.ifEmpty { "0" }
        ?: "0"
    val minor = parts.getOrNull(1)
        ?.filter { it.isDigit() }
        .orEmpty()
        .padEnd(2, '0')
        .take(2)

    return "$major.$minor"
}

private fun String.appendAmountInput(value: String): String {
    val normalized = this
    val decimalIndex = normalized.indexOf('.')

    if (decimalIndex >= 0) {
        val decimalsUsed = normalized.length - decimalIndex - 1
        val decimalsAvailable = 2 - decimalsUsed
        return if (decimalsAvailable <= 0) {
            normalized
        } else {
            normalized + value.take(decimalsAvailable)
        }
    }

    return when {
        normalized.isEmpty() && value == "00" -> ""
        normalized.isEmpty() -> value
        normalized == "0" && value == "00" -> normalized
        normalized == "0" -> value
        normalized.length >= MaxAmountInputLength -> normalized
        else -> (normalized + value).take(MaxAmountInputLength)
    }
}

private fun String.appendAutoCentsInput(value: String): String {
    val currentDigits = filter(Char::isDigit).trimStart('0')
    val newDigits = (currentDigits + value.filter(Char::isDigit))
        .trimStart('0')
        .take(MaxAutoCentsInputDigits)
    return newDigits.asAutoCentsAmountText()
}

private fun String.dropLastAutoCentsInput(): String {
    val newDigits = filter(Char::isDigit)
        .trimStart('0')
        .dropLast(1)
    return newDigits.asAutoCentsAmountText()
}

private fun todayUtcMillis(): Long =
    LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.asDateLabel(): String {
    val selectedDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
    if (selectedDate == LocalDate.now()) return "Today"

    return selectedDate.format(DateLabelFormatter)
}

private const val MaxAmountInputLength = 9
private const val MaxAutoCentsInputDigits = 11

private val DateLabelFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

private val AppBackground = Color(0xFF08142A)
private val Panel = Color(0xFF101B37)
private val SelectedPanel = Color(0xFF1C4E8B)
private val ButtonPanel = Color(0xFF112A59)
private val KeyPanel = Color(0xFF121E3B)
private val Stroke = Color(0xFF243659)
private val Accent = Color(0xFF2F63D9)
private val AccentLight = Color(0xFF38A4F8)
private val Success = Color(0xFF12B886)
private val MutedText = Color(0xFF8FB0E8)

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun AddTransactionScreenPreview() {
    AddTransactionRoute()
}
