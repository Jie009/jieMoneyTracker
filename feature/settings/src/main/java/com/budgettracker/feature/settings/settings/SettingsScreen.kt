package com.budgettracker.feature.settings.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var googleDriveConnected by remember { mutableStateOf(false) }
    var notificationReadingEnabled by remember { mutableStateOf(false) }
    var amountInputMode by remember { mutableStateOf(AmountInputMode.Decimal) }
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

    SettingsScreen(
        uiState = uiState,
        googleDriveConnected = googleDriveConnected,
        notificationReadingEnabled = notificationReadingEnabled,
        amountInputMode = amountInputMode,
        onGoogleDriveToggle = { googleDriveConnected = it },
        onNotificationReadingToggle = { notificationReadingEnabled = it },
        onAmountInputModeSelected = { amountInputMode = it },
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

@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    googleDriveConnected: Boolean,
    notificationReadingEnabled: Boolean,
    amountInputMode: AmountInputMode,
    onGoogleDriveToggle: (Boolean) -> Unit,
    onNotificationReadingToggle: (Boolean) -> Unit,
    onAmountInputModeSelected: (AmountInputMode) -> Unit,
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
                    googleDriveConnected = googleDriveConnected,
                    notificationReadingEnabled = notificationReadingEnabled,
                )
            }
            uiState.message?.let { message ->
                item {
                    StatusMessage(message = message)
                }
            }
            item {
                SettingsSection(title = "Backup") {
                    SwitchSettingRow(
                        icon = Icons.Filled.Backup,
                        title = "Google Drive backup",
                        subtitle = if (googleDriveConnected) {
                            "Connected · Encrypted backup ready"
                        } else {
                            "Not connected · Tap to link Google Drive"
                        },
                        checked = googleDriveConnected,
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
                    )
                    NavigationSettingRow(
                        icon = Icons.Filled.Repeat,
                        title = "Recurring transactions",
                        subtitle = "Rent, subscriptions, salary, and reminders",
                    )
                    NavigationSettingRow(
                        icon = Icons.Filled.AccountBalanceWallet,
                        title = "Cashbook",
                        subtitle = "Manage cashbooks and active currency",
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
                selected = selectedMode == AmountInputMode.Decimal,
                onClick = { onModeSelected(AmountInputMode.Decimal) },
                modifier = Modifier.weight(1f),
            )
            AmountInputModeChip(
                label = "No dot",
                detail = "1250",
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

private enum class AmountInputMode {
    Decimal,
    AutoCents,
}

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
        googleDriveConnected = false,
        notificationReadingEnabled = false,
        amountInputMode = AmountInputMode.Decimal,
        onGoogleDriveToggle = {},
        onNotificationReadingToggle = {},
        onAmountInputModeSelected = {},
        onImportClick = {},
        onExportClick = {},
        onConfirmImport = {},
        onDismissImport = {},
    )
}
