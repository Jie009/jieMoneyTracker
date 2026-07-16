package com.budgettracker.feature.settings.settings

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.budgettracker.core.data.backup.GoogleDriveBackupState
import com.budgettracker.core.model.AmountInputMode
import com.budgettracker.core.model.Category
import com.budgettracker.core.model.Cashbook
import com.budgettracker.core.model.RecurringFrequency
import com.budgettracker.core.model.TransactionType
import com.budgettracker.core.ui.category.CategoryEditDialog
import com.budgettracker.core.ui.category.CategoryManagerScreen
import com.budgettracker.core.ui.category.MaterialIconOptions
import com.budgettracker.core.ui.category.MaterialIconSections
import com.budgettracker.core.ui.category.asCategoryUiModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val amountInputMode by viewModel.amountInputMode.collectAsStateWithLifecycle()
    val googleDriveBackupState by viewModel.googleDriveBackupState.collectAsStateWithLifecycle()
    val cashbookUiState by viewModel.cashbookUiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val recurringTransactions by viewModel.recurringTransactions.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var notificationReadingEnabled by remember { mutableStateOf(context.isPaymentNotificationAccessEnabled()) }
    var screenMode by remember { mutableStateOf(SettingsScreenMode.Main) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        context.openNotificationListenerSettings()
    }
    val googleDriveSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        viewModel.handleGoogleDriveSignInResult(result.data)
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(viewModel::previewImport)
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        uri?.let(viewModel::exportCsv)
    }
    DisposableEffect(context, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationReadingEnabled = context.isPaymentNotificationAccessEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (screenMode) {
        SettingsScreenMode.Main -> {
            SettingsScreen(
                uiState = uiState,
                googleDriveBackupState = googleDriveBackupState,
                notificationReadingEnabled = notificationReadingEnabled,
                amountInputMode = amountInputMode,
                onGoogleDriveToggle = { enabled ->
                    if (enabled) {
                        if (googleDriveBackupState.isConnected) {
                            viewModel.syncGoogleDriveBackup()
                        } else {
                            googleDriveSignInLauncher.launch(viewModel.googleDriveSignInIntent())
                        }
                    } else {
                        viewModel.disconnectGoogleDrive()
                    }
                },
                onNotificationReadingToggle = { enabled ->
                    if (enabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            context.openNotificationListenerSettings()
                        }
                    } else {
                        context.openNotificationListenerSettings()
                    }
                },
                onAmountInputModeSelected = viewModel::updateAmountInputMode,
                onCategorySettingsClick = { screenMode = SettingsScreenMode.Categories },
                onRecurringTransactionsClick = { screenMode = SettingsScreenMode.RecurringTransactions },
                onCashbookSettingsClick = { screenMode = SettingsScreenMode.Cashbooks },
                onImportClick = {
                    importLauncher.launch(
                        arrayOf(
                            "text/csv",
                            "application/vnd.ms-excel",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/octet-stream",
                        ),
                    )
                },
                onExportClick = {
                    exportLauncher.launch("budget_tracker_money_manager.csv")
                },
                onConfirmImport = viewModel::confirmImport,
                onDismissImport = viewModel::clearImportPreview,
                modifier = modifier,
            )
        }

        SettingsScreenMode.Cashbooks -> {
            CashbookSettingsScreen(
                cashbooks = cashbookUiState.cashbooks,
                selectedCashbookId = cashbookUiState.selectedCashbookId,
                onBack = { screenMode = SettingsScreenMode.Main },
                onSelectCashbook = viewModel::selectCashbook,
                onCreateCashbook = viewModel::createCashbook,
                modifier = modifier,
            )
        }

        SettingsScreenMode.Categories -> {
            CategorySettingsScreen(
                categories = categories,
                onBack = { screenMode = SettingsScreenMode.Main },
                onSaveCategory = viewModel::updateCategory,
                onArchiveCategory = viewModel::archiveCategory,
                modifier = modifier,
            )
        }

        SettingsScreenMode.RecurringTransactions -> {
            RecurringTransactionsScreen(
                categories = categories,
                recurringTransactions = recurringTransactions,
                onBack = { screenMode = SettingsScreenMode.Main },
                onSaveRecurringTransaction = viewModel::saveRecurringTransaction,
                onPausedChange = viewModel::setRecurringPaused,
                onDelete = viewModel::deleteRecurringTransaction,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    googleDriveBackupState: GoogleDriveBackupState,
    notificationReadingEnabled: Boolean,
    amountInputMode: AmountInputMode,
    onGoogleDriveToggle: (Boolean) -> Unit,
    onNotificationReadingToggle: (Boolean) -> Unit,
    onAmountInputModeSelected: (AmountInputMode) -> Unit,
    onCategorySettingsClick: () -> Unit,
    onRecurringTransactionsClick: () -> Unit,
    onCashbookSettingsClick: () -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onConfirmImport: () -> Unit,
    onDismissImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .statusBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SettingsHeader()
            }
            item {
                SettingsHero(
                    googleDriveConnected = googleDriveBackupState.isConnected,
                    notificationReadingEnabled = notificationReadingEnabled,
                )
            }
            val statusMessage = uiState.message ?: googleDriveBackupState.message
            statusMessage?.let { message ->
                item {
                    StatusMessage(message = message)
                }
            }
            item {
                SettingsSection(title = "Backup") {
                    SwitchSettingRow(
                        icon = Icons.Filled.Backup,
                        title = "Google Drive backup",
                        subtitle = googleDriveBackupState.backupSubtitle(),
                        checked = googleDriveBackupState.isConnected,
                        onCheckedChange = onGoogleDriveToggle,
                    )
                }
            }
            item {
                SettingsSection(title = "Manage") {
                    NavigationSettingRow(
                        icon = Icons.Filled.Category,
                        title = "Category setting",
                        subtitle = "Edit income and expense categories",
                        onClick = onCategorySettingsClick,
                    )
                    NavigationSettingRow(
                        icon = Icons.Filled.Repeat,
                        title = "Recurring transactions",
                        subtitle = "Rent, subscriptions, salary, and reminders",
                        onClick = onRecurringTransactionsClick,
                    )
                    NavigationSettingRow(
                        icon = Icons.Filled.AccountBalanceWallet,
                        title = "Cashbook",
                        subtitle = "Manage cashbooks and active currency",
                        onClick = onCashbookSettingsClick,
                    )
                }
            }
            item {
                SettingsSection(title = "Data") {
                    NavigationSettingRow(
                        icon = Icons.Filled.UploadFile,
                        title = "Export data",
                        subtitle = "Export Money Manager compatible CSV",
                        onClick = onExportClick,
                    )
                    NavigationSettingRow(
                        icon = Icons.Filled.Download,
                        title = "Import data",
                        subtitle = "Import CSV/XLSX and review duplicates before saving",
                        onClick = onImportClick,
                    )
                }
            }
            item {
                SettingsSection(title = "Input") {
                    AmountInputModeSetting(
                        selectedMode = amountInputMode,
                        onModeSelected = onAmountInputModeSelected,
                    )
                }
            }
            item {
                SettingsSection(title = "Automation") {
                    SwitchSettingRow(
                        icon = Icons.Filled.Notifications,
                        title = "Read payment notifications",
                        subtitle = if (notificationReadingEnabled) {
                            "Enabled · Create quick-add drafts from supported apps"
                        } else {
                            "Off · Enable notification access for TNG and banks"
                        },
                        checked = notificationReadingEnabled,
                        onCheckedChange = onNotificationReadingToggle,
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    uiState.importPreview?.let { preview ->
        ImportPreviewDialog(
            preview = preview,
            isBusy = uiState.isBusy,
            onDismiss = onDismissImport,
            onConfirm = onConfirmImport,
        )
    }
}

@Composable
private fun SettingsHeader() {
    Column {
        Text(
            text = "Settings",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
            ),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "App, data, backup, and automation controls",
            color = MutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SettingsHero(
    googleDriveConnected: Boolean,
    notificationReadingEnabled: Boolean,
) {
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
        Text(
            text = "No personal profile",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "This area is for storage, cashbook, import/export, input, and notification settings.",
            color = SoftText,
            fontSize = 12.sp,
            lineHeight = 17.sp,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusPill(
                label = if (googleDriveConnected) "Drive linked" else "Drive not linked",
                active = googleDriveConnected,
                modifier = Modifier.weight(1f),
            )
            StatusPill(
                label = if (notificationReadingEnabled) "Notification on" else "Notification off",
                active = notificationReadingEnabled,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatusMessage(message: String) {
    Text(
        text = message,
        color = Color(0xFFBFDBFE),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, AccentStroke, RoundedCornerShape(18.dp))
            .background(AccentBlue.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    )
}

@Composable
private fun ImportPreviewDialog(
    preview: ImportPreviewUiState,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val importableCount = preview.records.size - preview.duplicateRows.size
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                text = "Import preview",
                fontWeight = FontWeight.Black,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Target cashbook: ${preview.targetCashbookName}",
                    color = SoftText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Rows parsed: ${preview.records.size}",
                    color = SoftText,
                    fontSize = 13.sp,
                )
                Text(
                    text = "Possible duplicates skipped: ${preview.duplicateRows.size}",
                    color = SoftText,
                    fontSize = 13.sp,
                )
                Text(
                    text = "Errors: ${preview.errors.size}",
                    color = if (preview.errors.isEmpty()) SoftText else Color(0xFFFCA5A5),
                    fontSize = 13.sp,
                )
                if (preview.ignoredPhotoRows > 0) {
                    Text(
                        text = "Photos ignored for ${preview.ignoredPhotoRows} rows in this version.",
                        color = Color(0xFFFDE68A),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
                preview.errors.firstOrNull()?.let { firstError ->
                    Text(
                        text = firstError,
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
                Text(
                    text = "$importableCount transactions will be imported after confirmation.",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isBusy && importableCount > 0,
                onClick = onConfirm,
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isBusy,
                onClick = onDismiss,
            ) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun StatusPill(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        color = if (active) Color(0xFF86EFAC) else MutedText,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
    )
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        Text(
            text = title.uppercase(),
            color = MutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, Stroke, RoundedCornerShape(22.dp))
                .background(Panel),
            content = content,
        )
    }
}

@Composable
private fun NavigationSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
) {
    SettingRowShell(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = Modifier.clickable(onClick = onClick),
        trailing = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(22.dp),
            )
        },
    )
}

@Composable
private fun SwitchSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingRowShell(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = Modifier.clickable { onCheckedChange(!checked) },
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}

@Composable
private fun AmountInputModeSetting(
    selectedMode: AmountInputMode,
    onModeSelected: (AmountInputMode) -> Unit,
) {
    Column(modifier = Modifier.padding(14.dp)) {
        SettingTitleBlock(
            icon = Icons.Filled.Payments,
            title = "Amount input type",
            subtitle = "Choose whether numbers use a decimal point or auto cents.",
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AmountInputModeChip(
                label = "Decimal",
                detail = "12.50",
                selected = selectedMode == AmountInputMode.NormalDecimal,
                onClick = { onModeSelected(AmountInputMode.NormalDecimal) },
                modifier = Modifier.weight(1f),
            )
            AmountInputModeChip(
                label = "No dot",
                detail = "1950 -> 19.50",
                selected = selectedMode == AmountInputMode.AutoCents,
                onClick = { onModeSelected(AmountInputMode.AutoCents) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AmountInputModeChip(
    label: String,
    detail: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = if (selected) AccentBlue else Stroke,
                shape = RoundedCornerShape(18.dp),
            )
            .background(if (selected) AccentBlue.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = detail,
            color = if (selected) Color(0xFFBFDBFE) else MutedText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SettingRowShell(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingTitleBlock(
            icon = icon,
            title = title,
            subtitle = subtitle,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.size(12.dp))
        trailing()
    }
}

@Composable
private fun SettingTitleBlock(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon = icon)
        Spacer(modifier = Modifier.size(12.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = MutedText,
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .border(1.dp, AccentStroke.copy(alpha = 0.6f), CircleShape)
            .background(AccentBlue.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFBFDBFE),
            modifier = Modifier.size(21.dp),
        )
    }
}

private enum class SettingsScreenMode {
    Main,
    Cashbooks,
    Categories,
    RecurringTransactions,
}

@Composable
private fun CashbookSettingsScreen(
    cashbooks: List<Cashbook>,
    selectedCashbookId: String?,
    onBack: () -> Unit,
    onSelectCashbook: (String) -> Unit,
    onCreateCashbook: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 18.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(18.dp))
            SettingsSubPageHeader(
                title = "Cashbook",
                subtitle = "Switch between isolated books or create a new one.",
                onBack = onBack,
            )
            Spacer(modifier = Modifier.height(14.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (cashbooks.isEmpty()) {
                    item {
                        EmptyPanel(message = "No cashbooks yet. Add one to start tracking.")
                    }
                } else {
                    items(cashbooks, key = { it.id }) { cashbook ->
                        CashbookRow(
                            cashbook = cashbook,
                            selected = cashbook.id == selectedCashbookId,
                            onClick = { onSelectCashbook(cashbook.id) },
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(86.dp))
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp)
                .size(58.dp)
                .clip(CircleShape)
                .background(AccentBlue)
                .clickable { showAddDialog = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add cashbook",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }

    if (showAddDialog) {
        AddCashbookDialog(
            onDismiss = { showAddDialog = false },
            onCreate = { name ->
                onCreateCashbook(name)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun CashbookRow(
    cashbook: Cashbook,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = if (selected) AccentBlue else Stroke,
                shape = RoundedCornerShape(20.dp),
            )
            .background(if (selected) AccentBlue.copy(alpha = 0.16f) else Panel)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon = Icons.Filled.AccountBalanceWallet)
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cashbook.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${cashbook.currency.name} · ${if (cashbook.isDefault) "Default cashbook" else "Separate data space"}",
                color = MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        StatusPill(
            label = if (selected) "Current" else "Select",
            active = selected,
        )
    }
}

@Composable
private fun AddCashbookDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    val trimmedName = name.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                text = "Add cashbook",
                fontWeight = FontWeight.Black,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Each cashbook has its own categories, transactions, imports, and exports.",
                    color = SoftText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Cashbook name") },
                    placeholder = { Text("Family, Business, Trip...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = trimmedName.isNotBlank(),
                onClick = { onCreate(trimmedName) },
            ) {
                Text("Create")
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
private fun CategorySettingsScreen(
    categories: List<Category>,
    onBack: () -> Unit,
    onSaveCategory: (Category, String, String, String) -> Unit,
    onArchiveCategory: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedType by remember { mutableStateOf(TransactionType.Expense) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryNameDraft by remember { mutableStateOf("") }
    var categoryIconDraft by remember { mutableStateOf(MaterialIconOptions.first().name) }
    var categoryColorDraft by remember { mutableStateOf("#64748B") }
    val visibleCategories = remember(categories, selectedType) {
        categories.filter { it.type == selectedType }
    }
    val visibleCategoryModels = remember(visibleCategories) {
        visibleCategories.map { it.asCategoryUiModel() }
    }

    fun openEditor(category: Category) {
        editingCategory = category
        categoryNameDraft = category.name
        categoryColorDraft = category.color
        categoryIconDraft = MaterialIconOptions
            .firstOrNull { it.name == category.icon }
            ?.name
            ?: MaterialIconOptions.first().name
    }

    fun closeEditor() {
        editingCategory = null
        categoryNameDraft = ""
        categoryIconDraft = MaterialIconOptions.first().name
        categoryColorDraft = "#64748B"
    }

    CategoryManagerScreen(
        categories = visibleCategoryModels,
        selectedCategoryId = null,
        onBack = onBack,
        onCategoryClick = {},
        onCategoryEditClick = { categoryUiModel ->
            visibleCategories.firstOrNull { it.id == categoryUiModel.id }?.let(::openEditor)
        },
        title = "Category",
        helperText = "Tap to edit",
        modifier = modifier,
        topContent = {
            TypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it },
            )
            if (visibleCategoryModels.isEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                EmptyPanel(message = "No ${selectedType.name.lowercase()} categories yet.")
            }
        },
    )

    editingCategory?.let { category ->
        CategoryEditDialog(
            title = "Edit category",
            name = categoryNameDraft,
            color = categoryColorDraft,
            selectedIconName = categoryIconDraft,
            iconSections = MaterialIconSections,
            onNameChange = { categoryNameDraft = it },
            onColorChange = { categoryColorDraft = it },
            onIconSelected = { categoryIconDraft = it },
            onDismiss = ::closeEditor,
            onSave = {
                onSaveCategory(category, categoryNameDraft, categoryIconDraft, categoryColorDraft)
                closeEditor()
            },
            archiveButton = {
                TextButton(
                    onClick = {
                        onArchiveCategory(category)
                        closeEditor()
                    },
                ) {
                    Text("Hide")
                }
            },
        )
    }
}

@Composable
private fun RecurringTransactionsScreen(
    categories: List<Category>,
    recurringTransactions: List<RecurringTransactionUiModel>,
    onBack: () -> Unit,
    onSaveRecurringTransaction: (RecurringTransactionInput) -> Unit,
    onPausedChange: (RecurringTransactionUiModel, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRecurringTransaction by remember { mutableStateOf<RecurringTransactionUiModel?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 18.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(18.dp))
            SettingsSubPageHeader(
                title = "Recurring transactions",
                subtitle = "Fixed income or fixed deductions with a scheduled date.",
                onBack = onBack,
            )
            Spacer(modifier = Modifier.height(14.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (recurringTransactions.isEmpty()) {
                    item {
                        EmptyPanel(message = "No recurring transactions yet. Add rent, subscriptions, salary, or reminders.")
                    }
                } else {
                    items(recurringTransactions, key = { it.id }) { recurringTransaction ->
                        RecurringTransactionRow(
                            recurringTransaction = recurringTransaction,
                            onPausedChange = { paused -> onPausedChange(recurringTransaction, paused) },
                            onEdit = { editingRecurringTransaction = recurringTransaction },
                            onDelete = { onDelete(recurringTransaction.id) },
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(86.dp))
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp)
                .size(58.dp)
                .clip(CircleShape)
                .background(AccentBlue)
                .clickable { showAddDialog = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add recurring transaction",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }

    if (showAddDialog || editingRecurringTransaction != null) {
        RecurringTransactionDialog(
            categories = categories,
            recurringTransaction = editingRecurringTransaction,
            onDismiss = {
                showAddDialog = false
                editingRecurringTransaction = null
            },
            onSave = { input ->
                onSaveRecurringTransaction(input)
                showAddDialog = false
                editingRecurringTransaction = null
            },
        )
    }
}

@Composable
private fun SettingsSubPageHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, Stroke, RoundedCornerShape(18.dp))
                .background(Panel)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = MutedText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Stroke, RoundedCornerShape(18.dp))
            .background(Panel)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TransactionType.entries.forEach { type ->
            TypeChip(
                label = type.name,
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AccentBlue else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else MutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun RecurringTransactionRow(
    recurringTransaction: RecurringTransactionUiModel,
    onPausedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Stroke, RoundedCornerShape(20.dp))
            .background(Panel)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = Icons.Filled.Repeat)
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recurringTransaction.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = recurringTransaction.note ?: "${recurringTransaction.repeatLabel()} · next ${recurringTransaction.nextRunDate.format(DateFormatter)}",
                    color = MutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "${if (recurringTransaction.type == TransactionType.Expense) "-" else "+"}RM ${recurringTransaction.amount}",
                color = if (recurringTransaction.type == TransactionType.Expense) Color(0xFFFCA5A5) else Color(0xFF86EFAC),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatusPill(
                label = if (recurringTransaction.isPaused) "Paused" else "Active",
                active = !recurringTransaction.isPaused,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = !recurringTransaction.isPaused,
                onCheckedChange = { checked -> onPausedChange(!checked) },
            )
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit recurring transaction",
                tint = Color(0xFFBFDBFE),
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onEdit),
            )
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete recurring transaction",
                tint = Color(0xFFFCA5A5),
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onDelete),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringTransactionDialog(
    categories: List<Category>,
    recurringTransaction: RecurringTransactionUiModel? = null,
    onDismiss: () -> Unit,
    onSave: (RecurringTransactionInput) -> Unit,
) {
    var name by remember(recurringTransaction?.id) { mutableStateOf(recurringTransaction?.title.orEmpty()) }
    var type by remember(recurringTransaction?.id) {
        mutableStateOf(recurringTransaction?.type ?: TransactionType.Expense)
    }
    var amount by remember(recurringTransaction?.id) { mutableStateOf(recurringTransaction?.amount.orEmpty()) }
    var note by remember(recurringTransaction?.id) { mutableStateOf(recurringTransaction?.note.orEmpty()) }
    var repeatOption by remember(recurringTransaction?.id) {
        mutableStateOf(recurringTransaction?.repeatOption() ?: RecurringRepeatOption.Monthly)
    }
    var transactionCountMode by remember(recurringTransaction?.id) {
        mutableStateOf(
            if (recurringTransaction?.maxOccurrences == null) {
                TransactionCountMode.Infinite
            } else {
                TransactionCountMode.Custom
            },
        )
    }
    var transactionCountInput by remember(recurringTransaction?.id) {
        mutableStateOf(recurringTransaction?.maxOccurrences?.toString().orEmpty())
    }
    var requireConfirmation by remember(recurringTransaction?.id) {
        mutableStateOf(recurringTransaction?.requireConfirmation ?: false)
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var startDate by remember(recurringTransaction?.id) {
        mutableStateOf(recurringTransaction?.raw?.startDate ?: LocalDate.now())
    }
    var selectedCategoryId by remember(recurringTransaction?.id) {
        mutableStateOf(recurringTransaction?.categoryId)
    }
    val filteredCategories = remember(categories, type) {
        categories.filter { it.type == type }
    }
    val resolvedCategoryId = selectedCategoryId
        ?.takeIf { selectedId -> filteredCategories.any { it.id == selectedId } }
        ?: filteredCategories.firstOrNull()?.id

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                text = if (recurringTransaction == null) "Add recurring transaction" else "Edit recurring transaction",
                fontWeight = FontWeight.Black,
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 520.dp),
            ) {
                item {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Transaction name") },
                        placeholder = { Text("Rent, salary, subscription...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    TypeSelector(
                        selectedType = type,
                        onTypeSelected = {
                            type = it
                            selectedCategoryId = null
                        },
                    )
                }
                item {
                    TextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        placeholder = { Text("1200.00") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    Text(
                        text = "Category",
                        color = MutedText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryChoiceGrid(
                        categories = filteredCategories,
                        selectedCategoryId = resolvedCategoryId,
                        onCategorySelected = { selectedCategoryId = it },
                    )
                }
                item {
                    RepeatOptionSelector(
                        selectedOption = repeatOption,
                        onOptionSelected = { repeatOption = it },
                    )
                }
                item {
                    SettingRowShell(
                        icon = Icons.Filled.CalendarMonth,
                        title = "Start date",
                        subtitle = startDate.format(DateFormatter),
                        modifier = Modifier.clickable { showDatePicker = true },
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MutedText,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                    )
                }
                item {
                    TransactionCountSelector(
                        selectedMode = transactionCountMode,
                        countInput = transactionCountInput,
                        onModeSelected = { transactionCountMode = it },
                        onCountInputChange = { transactionCountInput = it.filter(Char::isDigit).take(3) },
                    )
                }
                item {
                    SettingRowShell(
                        icon = Icons.Filled.Notifications,
                        title = "Require confirmation",
                        subtitle = if (requireConfirmation) {
                            "Ask before creating each transaction"
                        } else {
                            "Create transactions automatically"
                        },
                        modifier = Modifier.clickable { requireConfirmation = !requireConfirmation },
                        trailing = {
                            Switch(
                                checked = requireConfirmation,
                                onCheckedChange = { requireConfirmation = it },
                            )
                        },
                    )
                }
                item {
                    TextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note") },
                        placeholder = { Text("Optional note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            val maxOccurrences = when (transactionCountMode) {
                TransactionCountMode.Infinite -> null
                TransactionCountMode.Custom -> transactionCountInput.toIntOrNull()
            }
            TextButton(
                enabled = name.isNotBlank() &&
                    amount.isNotBlank() &&
                    (transactionCountMode == TransactionCountMode.Infinite || (maxOccurrences ?: 0) > 0),
                onClick = {
                    onSave(
                        RecurringTransactionInput(
                            id = recurringTransaction?.id,
                            name = name,
                            amount = amount,
                            type = type,
                            categoryId = resolvedCategoryId,
                            frequency = repeatOption.frequency,
                            interval = repeatOption.interval,
                            startDate = startDate,
                            maxOccurrences = maxOccurrences,
                            note = note,
                            requireConfirmation = requireConfirmation,
                        ),
                    )
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
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.toUtcMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = millis.toUtcLocalDate()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CategoryChoiceGrid(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String) -> Unit,
) {
    if (categories.isEmpty()) {
        EmptyPanel(message = "No categories for this type.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(2).forEach { rowCategories ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowCategories.forEach { category ->
                    SelectableTextChip(
                        label = category.name,
                        selected = category.id == selectedCategoryId,
                        onClick = { onCategorySelected(category.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowCategories.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RepeatOptionSelector(
    selectedOption: RecurringRepeatOption,
    onOptionSelected: (RecurringRepeatOption) -> Unit,
) {
    Column {
        Text(
            text = "Transaction frequency",
            color = MutedText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RecurringRepeatOption.entries.chunked(2).forEach { rowOptions ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowOptions.forEach { option ->
                        SelectableTextChip(
                            label = option.label,
                            selected = option == selectedOption,
                            onClick = { onOptionSelected(option) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowOptions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCountSelector(
    selectedMode: TransactionCountMode,
    countInput: String,
    onModeSelected: (TransactionCountMode) -> Unit,
    onCountInputChange: (String) -> Unit,
) {
    Column {
        Text(
            text = "Number of transactions",
            color = MutedText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectableTextChip(
                label = "Unlimited",
                selected = selectedMode == TransactionCountMode.Infinite,
                onClick = { onModeSelected(TransactionCountMode.Infinite) },
                modifier = Modifier.weight(1f),
            )
            SelectableTextChip(
                label = "Custom",
                selected = selectedMode == TransactionCountMode.Custom,
                onClick = { onModeSelected(TransactionCountMode.Custom) },
                modifier = Modifier.weight(1f),
            )
        }
        if (selectedMode == TransactionCountMode.Custom) {
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = countInput,
                onValueChange = onCountInputChange,
                label = { Text("Times") },
                placeholder = { Text("12") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SelectableTextChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = if (selected) AccentBlue else Stroke,
                shape = RoundedCornerShape(14.dp),
            )
            .background(if (selected) AccentBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EmptyPanel(message: String) {
    Text(
        text = message,
        color = MutedText,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Stroke, RoundedCornerShape(18.dp))
            .background(Panel)
            .padding(14.dp),
    )
}

private fun RecurringTransactionUiModel.repeatLabel(): String {
    val option = RecurringRepeatOption.entries.firstOrNull { repeatOption ->
        repeatOption.frequency == frequency && repeatOption.interval == interval
    }
    val countLabel = maxOccurrences?.let { "$it times" } ?: "unlimited"
    return "${option?.label ?: "${frequency.name} x$interval"} · $generatedOccurrences/$countLabel"
}

private fun RecurringTransactionUiModel.repeatOption(): RecurringRepeatOption =
    RecurringRepeatOption.entries.firstOrNull { repeatOption ->
        repeatOption.frequency == frequency && repeatOption.interval == interval
    } ?: RecurringRepeatOption.Monthly

private enum class TransactionCountMode {
    Infinite,
    Custom,
}

private enum class RecurringRepeatOption(
    val label: String,
    val frequency: RecurringFrequency,
    val interval: Int,
) {
    Daily("Every day", RecurringFrequency.Daily, 1),
    Weekly("Every week", RecurringFrequency.Weekly, 1),
    BiWeekly("Every 2 weeks", RecurringFrequency.Weekly, 2),
    Monthly("Every month", RecurringFrequency.Monthly, 1),
    EveryTwoMonths("Every 2 months", RecurringFrequency.Monthly, 2),
    EveryThreeMonths("Every 3 months", RecurringFrequency.Monthly, 3),
    EveryFourMonths("Every 4 months", RecurringFrequency.Monthly, 4),
    EverySixMonths("Every 6 months", RecurringFrequency.Monthly, 6),
    Yearly("Every year", RecurringFrequency.Yearly, 1),
}

private fun String.toCategoryColor(): Color {
    val normalized = trim()
        .removePrefix("#")
        .takeIf { it.length == 6 || it.length == 8 }
        ?.takeIf { hex -> hex.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' } }
        ?: return AccentBlue

    val argb = if (normalized.length == 6) "FF$normalized" else normalized
    return Color(argb.toLong(16))
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate =
    java.time.Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun GoogleDriveBackupState.backupSubtitle(): String = when {
    isSyncing -> "Syncing backup to Google Drive..."
    isConnected && pendingSync && !isOnline -> "Connected · Offline, will sync when online"
    isConnected && pendingSync -> "Connected · Waiting to sync"
    isConnected && lastSyncedAt != null -> {
        val formatted = lastSyncedAt
            ?.atZone(ZoneId.systemDefault())
            ?.format(DriveSyncFormatter)
        "Connected · Last synced $formatted"
    }
    isConnected -> accountEmail?.let { "Connected as $it · Tap to sync now" }
        ?: "Connected · Tap to sync now"
    else -> "Not connected · Tap to link Google Drive"
}

private fun Context.isPaymentNotificationAccessEnabled(): Boolean {
    val enabledListeners = Settings.Secure.getString(
        contentResolver,
        "enabled_notification_listeners",
    ).orEmpty()

    return enabledListeners
        .split(':')
        .mapNotNull(ComponentName::unflattenFromString)
        .any { it.packageName == packageName }
}

private fun Context.openNotificationListenerSettings() {
    startActivity(
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}

private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
private val DriveSyncFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")

private val AppBackground = Color(0xFF08142A)
private val Panel = Color(0xE0101A34)
private val Stroke = Color(0x1AFFFFFF)
private val AccentStroke = Color(0x3893C5FD)
private val AccentBlue = Color(0xFF3B82F6)
private val MutedText = Color(0xFF91A7C5)
private val SoftText = Color(0xFFD6E2F2)

@Preview
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        uiState = SettingsUiState(),
        googleDriveBackupState = GoogleDriveBackupState(),
        notificationReadingEnabled = false,
        amountInputMode = AmountInputMode.NormalDecimal,
        onGoogleDriveToggle = {},
        onNotificationReadingToggle = {},
        onAmountInputModeSelected = {},
        onCategorySettingsClick = {},
        onRecurringTransactionsClick = {},
        onCashbookSettingsClick = {},
        onImportClick = {},
        onExportClick = {},
        onConfirmImport = {},
        onDismissImport = {},
    )
}
