# Budget Tracker Android App TRD

## 1. Technical Overview

### 1.1 Goal

Build a local-first Android budget tracking app with fast manual entry, multi-cashbook support, reports, recurring transactions, import/export, bilingual UI, optional Google Drive backup, and Touch 'n Go notification quick add.

### 1.2 Technical Principles

- Local data is the source of truth.
- All core features must work offline.
- Cloud integration should be optional.
- Financial calculations must be deterministic and testable.
- User-entered data should never be silently changed.
- Background automation should require clear user permission.
- MVP should avoid over-complicated sync and machine learning.

## 2. Recommended Stack

- Language: Kotlin
- UI: Jetpack Compose
- Architecture: MVVM with unidirectional UI state
- Navigation: Jetpack Navigation Compose
- Database: Room
- Background tasks: WorkManager
- Dependency injection: Hilt
- Serialization: Kotlinx Serialization
- Date and time: kotlinx-datetime or java.time
- Notifications: NotificationListenerService
- Cloud backup: Google Drive API
- Authentication: Google Sign-In
- Testing: JUnit, Turbine, Room testing, Compose UI tests

## 3. Project Structure

Recommended modular structure:

- `app`
- `core-model`
- `core-database`
- `core-data`
- `core-domain`
- `core-ui`
- `core-i18n`
- `core-notification`
- `feature-home`
- `feature-add-transaction`
- `feature-transactions`
- `feature-reports`
- `feature-charts`
- `feature-cashbook`
- `feature-category`
- `feature-recurring`
- `feature-import-export`
- `feature-backup`
- `feature-settings`

### 3.1 Module Responsibilities

`app`

- App entry point.
- Navigation graph.
- Theme setup.
- Dependency injection setup.

`core-model`

- Shared domain models.
- Enums such as transaction type, input mode, language mode.

`core-database`

- Room database.
- Entities.
- DAOs.
- Database migrations.

`core-data`

- Repository implementations.
- Local database coordination.
- Import/export data sources.
- Backup data source.

`core-domain`

- Use cases.
- Calculation logic.
- Report aggregation logic.
- Category suggestion logic.

`core-ui`

- Shared Compose components.
- Amount keypad.
- Category icon components.
- Empty states.
- Loading states.

`core-i18n`

- Language manager.
- Locale switching helper.
- Shared localized labels if needed.

`core-notification`

- Notification listener.
- TNG parser.
- Quick-add notification builder.

Feature modules own their screens, ViewModels, and UI state.

## 4. Architecture

### 4.1 Pattern

Use MVVM with unidirectional state:

- UI sends events to ViewModel.
- ViewModel calls use cases or repositories.
- Repository reads/writes Room.
- UI observes `StateFlow`.

### 4.2 Recommended Screen Structure

Each feature screen should have:

- `Screen`
- `Route`
- `ViewModel`
- `UiState`
- `UiEvent`
- `UiEffect`, only when one-time effects are needed

Example:

```kotlin
data class HomeUiState(
    val selectedMonth: YearMonth,
    val selectedCashbookId: CashbookId?,
    val totalIncome: Money,
    val totalExpense: Money,
    val balance: Money,
    val transactions: List<TransactionListItem>,
    val isLoading: Boolean
)
```

### 4.3 Data Flow

For normal features:

1. UI event.
2. ViewModel validates input.
3. Use case executes business rule.
4. Repository updates Room.
5. DAO emits updated Flow.
6. ViewModel maps domain model to UI state.
7. UI recomposes.

For notification quick add:

1. Android receives TNG notification.
2. Notification listener extracts raw notification text.
3. Parser creates draft transaction.
4. Duplicate checker checks existing pending or saved records.
5. App shows quick-add notification.
6. User opens pre-filled add transaction screen.
7. User confirms and saves.

## 5. Core Domain Models

### 5.1 Money

Money should be stored as integer minor units, not floating point.

Examples:

- RM 56.90 stored as `5690`.
- RM 1.00 stored as `100`.

Recommended model:

```kotlin
data class Money(
    val minorUnits: Long,
    val currency: CurrencyCode
)
```

### 5.2 Transaction Type

- Expense
- Income

### 5.3 Source Type

- Manual
- Notification
- Import
- Recurring

### 5.4 Language Mode

- System
- English
- Chinese

### 5.5 Amount Input Mode

- NormalDecimal
- AutoCents

## 6. Database Design

### 6.1 Database

Use Room as the local database.

Recommended database name:

- `budget_tracker.db`

### 6.2 Tables

Initial tables:

- `cashbooks`
- `categories`
- `transactions`
- `recurring_transactions`
- `budgets`
- `tags`
- `transaction_tags`
- `category_learning_rules`
- `import_batches`
- `app_settings`
- `backup_metadata`
- `notification_drafts`

### 6.3 CashbookEntity

Fields:

- `id: String`
- `name: String`
- `currencyCode: String`
- `color: String?`
- `icon: String?`
- `isDefault: Boolean`
- `isArchived: Boolean`
- `createdAt: Instant`
- `updatedAt: Instant`

Indexes:

- `isDefault`
- `isArchived`

### 6.4 CategoryEntity

Fields:

- `id: String`
- `cashbookId: String`
- `name: String`
- `defaultNameKey: String?`
- `type: String`
- `icon: String`
- `color: String`
- `sortOrder: Int`
- `isArchived: Boolean`
- `createdAt: Instant`
- `updatedAt: Instant`

Notes:

- Built-in categories use `defaultNameKey` for localized display.
- User-created categories use `name` as entered.
- Imported categories should not be auto-translated.

Indexes:

- `cashbookId`
- `cashbookId, type`
- `cashbookId, sortOrder`

### 6.5 TransactionEntity

Fields:

- `id: String`
- `cashbookId: String`
- `categoryId: String?`
- `amountMinor: Long`
- `currencyCode: String`
- `type: String`
- `dateTime: Instant`
- `note: String?`
- `source: String`
- `originalSourceId: String?`
- `importBatchId: String?`
- `createdAt: Instant`
- `updatedAt: Instant`

Rules:

- Expense and income should usually have `categoryId`.

Indexes:

- `cashbookId, dateTime`
- `cashbookId, categoryId, dateTime`
- `cashbookId, type, dateTime`
- `source`

### 6.6 RecurringTransactionEntity

Fields:

- `id: String`
- `cashbookId: String`
- `categoryId: String?`
- `amountMinor: Long`
- `currencyCode: String`
- `type: String`
- `frequency: String`
- `interval: Int`
- `startDate: LocalDate`
- `endDate: LocalDate?`
- `nextRunDate: LocalDate`
- `requireConfirmation: Boolean`
- `isPaused: Boolean`
- `note: String?`
- `createdAt: Instant`
- `updatedAt: Instant`

Indexes:

- `nextRunDate`
- `cashbookId, isPaused`

### 6.7 CategoryLearningRuleEntity

Fields:

- `id: String`
- `cashbookId: String`
- `timeBlock: String?`
- `dayType: String?`
- `amountMinMinor: Long?`
- `amountMaxMinor: Long?`
- `categoryId: String`
- `score: Double`
- `usageCount: Int`
- `lastUsedAt: Instant`
- `createdAt: Instant`
- `updatedAt: Instant`

Indexes:

- `cashbookId`
- `cashbookId, categoryId`

### 6.8 NotificationDraftEntity

Stores pending notification-based transaction drafts.

Fields:

- `id: String`
- `sourcePackage: String`
- `rawTitle: String?`
- `rawText: String?`
- `amountMinor: Long?`
- `currencyCode: String?`
- `eventTime: Instant`
- `suggestedCategoryId: String?`
- `status: String`
- `createdAt: Instant`

Status values:

- Pending
- Saved
- Dismissed
- Duplicate

## 7. Financial Calculation Rules

### 7.1 Monthly Summary

For a selected cashbook and month:

- Income = sum of income transactions.
- Expense = sum of expense transactions.
- Balance = income - expense.

### 7.2 All Cashbooks Summary

For all cashbooks:

- Include all active cashbooks.
- Group by currency.
- MVP can assume MYR only.
- If multi-currency is added later, reports must not merge different currencies without conversion.

### 7.3 Recurring Transactions

Recurring transactions should generate real transactions only once per occurrence.

Recommended approach:

- WorkManager checks due recurring transactions daily.
- When app opens, also run a due check.
- If `requireConfirmation = true`, create a pending confirmation item.
- If false, create transaction automatically.
- Update `nextRunDate` after generation.

## 8. Reports and Filters

### 8.1 Report Query Model

Use a shared report filter model:

```kotlin
data class ReportFilter(
    val cashbookIds: Set<String>,
    val categoryIds: Set<String> = emptySet(),
    val transactionTypes: Set<TransactionType> = emptySet(),
    val startDate: LocalDate,
    val endDate: LocalDate,
    val tagIds: Set<String> = emptySet(),
    val minAmountMinor: Long? = null,
    val maxAmountMinor: Long? = null
)
```

### 8.2 Time Dimension

Queries should support:

- Single month summary.
- Month-by-month summary.
- Year summary.
- Custom date range.
- Daily trend inside a month.

### 8.3 Category Dimension

Queries should support:

- Total for one category across multiple months.
- Monthly trend for one category.
- Monthly comparison for multiple categories.
- Drill-down to transactions for a category.

### 8.4 Matrix Report

Matrix report shape:

- Rows: months.
- Columns: categories.
- Cell value: total amount.
- Row total: monthly total.
- Column total: category total across selected period.

This can be calculated in SQL or in domain aggregation after fetching filtered transactions. For MVP, domain aggregation is acceptable if data volume is small.

## 9. Amount Input Engine

### 9.1 Normal Decimal Mode

Input string is parsed as decimal.

Rules:

- `56.90` = 5690 minor units.
- `12` = 1200 minor units.
- `12.5` = 1250 minor units.
- More than two decimal places should be blocked or rounded only with explicit rule.

### 9.2 Auto Cents Mode

Input digits are interpreted as minor units.

Rules:

- `5690` = 5690 minor units.
- `56` = 56 minor units.
- `5` = 5 minor units.
- Display should format the result as money while typing.

### 9.3 Implementation

Create a pure Kotlin engine:

```kotlin
interface AmountInputFormatter {
    fun appendDigit(current: String, digit: Char): String
    fun appendDecimal(current: String): String
    fun backspace(current: String): String
    fun toMinorUnits(input: String, mode: AmountInputMode): Long
    fun formatPreview(input: String, mode: AmountInputMode, currency: String): String
}
```

This engine must have unit tests.

## 10. Payment Notification Quick Add

### 10.1 Permission

Use Android notification access permission.

Flow:

1. User opens payment notification integration settings.
2. App explains why notification access is needed.
3. User taps enable.
4. Android system settings opens.
5. User grants notification access.
6. App verifies access.

Supported sources should be individually configurable:

- Touch 'n Go eWallet.
- Public Bank transaction notifications.
- Future banks or wallets.

### 10.2 Notification Listener

Use `NotificationListenerService`.

Responsibilities:

- Listen for posted notifications.
- Filter by enabled payment or banking package names.
- Extract title, text, subtext, big text, and timestamp.
- Route data to the correct source-specific parser.
- Store pending draft if parsed.
- Show Budget Tracker quick-add notification when confidence is high enough.

### 10.3 Parser

The parser should be rule-based for MVP.

Use a parser registry:

```kotlin
interface PaymentNotificationParser {
    val source: PaymentNotificationSource
    val supportedPackageNames: Set<String>

    fun parse(input: RawNotification): ParsedPaymentNotification?
}
```

Initial parsers:

- `TngNotificationParser`
- `PublicBankNotificationParser`

Inputs:

- Package name.
- Notification title.
- Notification text.
- Big text.
- Post time.

Outputs:

- Amount.
- Currency.
- Transaction time.
- Confidence score.

Parser should be tested with real sample notifications.

Public Bank notification wording may differ by card type, app version, SMS-style alert format, and security setting. The parser should start conservative and only show quick-add when amount detection is reliable.

### 10.4 Duplicate Detection

A notification draft is duplicate if a similar record already exists:

- Same source package.
- Same amount.
- Same raw text.
- Within a short time window.

### 10.5 Save Behavior

MVP behavior:

- Do not auto-save.
- Open pre-filled add transaction screen.
- User confirms category.
- User saves.

### 10.6 Quick-Add Notification Behavior

Budget Tracker's own quick-add notification should be optional and lightweight.

Requirements:

- It must not be an ongoing notification.
- It must not be persistent.
- It must be dismissible by swipe.
- It should auto-cancel after a configurable timeout.
- It should not require the user to mark it as read.
- It should not stay forever if the app guessed wrongly.
- Dismissing the notification should mark the draft as dismissed or leave it as ignored history.
- The app may keep a notification draft in history for debugging or recovery, but it should not block normal usage.

Recommended Android notification settings:

- `setOngoing(false)`
- `setAutoCancel(true)`
- `setTimeoutAfter(...)`
- Use a low or default importance notification channel, based on user preference.
- Provide actions:
  - Add
  - Edit
  - Dismiss

## 11. Smart Category Suggestion

### 11.1 MVP Approach

Use local rule-based scoring. Do not use cloud AI.

### 11.2 Signals

- Time block.
- Day type.
- Amount range.
- Recent usage.
- Global usage frequency.

### 11.3 Time Blocks

Suggested blocks:

- Morning: 05:00-10:59
- Lunch: 11:00-13:59
- Afternoon: 14:00-17:59
- Dinner: 18:00-21:59
- Night: 22:00-04:59

### 11.4 Scoring

Suggested weights:

- Same time block: +10
- Same day type: +8
- Similar amount range: +8
- Recent use: +10
- Global frequency: +5

The category with the highest score is shown first.

### 11.5 Learning

When user saves or changes a category:

- Update time block-category rule.
- Update amount range-category rule.
- Increase score and usage count.
- Update `lastUsedAt`.

## 12. Import and Export

### 12.1 CSV Export

Export transactions with:

- Date
- Time
- Type
- Amount
- Currency
- Cashbook
- Category
- Note
- Tags
- Source

### 12.2 CSV Import

Flow:

1. User selects file.
2. App reads first rows.
3. App detects columns.
4. User maps columns.
5. App previews parsed records.
6. App highlights errors and duplicates.
7. User confirms.
8. App imports into selected cashbook.

### 12.3 Import Parser

Parser should support:

- Different date formats.
- Positive/negative expense formats.
- Separate type column.
- Category creation if missing.

### 12.4 Duplicate Detection

Duplicate if same:

- Date.
- Amount.
- Type.
- Note.
- Cashbook.

## 13. Google Drive Backup

### 13.1 MVP Backup Strategy

Use backup and restore, not real-time sync.

### 13.2 Backup Format

Recommended:

- JSON document.
- Versioned schema.
- Encrypted before upload.

Top-level shape:

```json
{
  "schemaVersion": 1,
  "createdAt": "2026-07-12T12:00:00Z",
  "cashbooks": [],
  "categories": [],
  "transactions": [],
  "recurringTransactions": [],
  "budgets": [],
  "tags": [],
  "settings": {},
  "learningRules": []
}
```

### 13.3 Backup Scheduling

Use WorkManager.

Triggers:

- Manual backup.
- Periodic backup.
- Backup after significant changes, delayed and debounced.

Recommended constraints:

- Network connected.
- Optional Wi-Fi only setting.
- Battery not low.

### 13.4 Restore

Restore flow:

1. User selects backup.
2. App downloads backup.
3. App decrypts backup.
4. App validates schema.
5. App previews backup metadata.
6. User confirms restore.
7. App replaces or merges local data.

MVP should support replace restore. Merge restore can come later.

## 14. i18n

### 14.1 Supported Languages

- Follow system
- English
- Chinese

### 14.2 Implementation

Use Android string resources:

- `values/strings.xml`
- `values-zh/strings.xml`

Language setting should be stored in app settings.

### 14.3 Localized Content

Must localize:

- Screen titles.
- Buttons.
- Labels.
- Validation messages.
- Error messages.
- Empty states.
- Report labels.
- Filter labels.
- Notification text.
- Permission explanation text.

### 14.4 Category Names

Built-in categories:

- Store a stable `defaultNameKey`.
- Display localized name based on current language.

User-created and imported categories:

- Display the exact user-entered name.
- Do not auto-translate.

## 15. Security and Privacy

### 15.1 Data Storage

MVP:

- Store data locally in Room.
- Backup encryption is required before Google Drive upload.

Post-MVP:

- Optional SQLCipher database encryption.

### 15.2 App Lock

Support:

- PIN.
- Biometric unlock.
- Auto-lock timeout.

### 15.3 Notification Privacy

For TNG notifications:

- Explain what data is read.
- Store original notification text only if user enables it.
- Allow user to delete notification draft history.

## 16. Testing Strategy

### 16.1 Unit Tests

Required:

- Money formatting.
- Amount input modes.
- Monthly summary calculation.
- Recurring date generation.
- CSV parsing.
- Duplicate detection.
- Category scoring.
- TNG notification parser.

### 16.2 Database Tests

Required:

- DAO insert/update/delete.
- Report queries.
- Migration tests.
- Import transaction batch behavior.

### 16.3 UI Tests

Critical flows:

- Add expense.
- Add income.
- Switch cashbook.
- Change language.
- Filter report by category and date.
- Create recurring transaction.

## 17. Build Milestones

### Milestone 1: Foundation

- Create Android project.
- Add Compose.
- Add Hilt.
- Add Room.
- Add navigation.
- Add theme.
- Add i18n resource structure.

### Milestone 2: Core Data

- Implement core models.
- Implement database entities.
- Implement DAOs.
- Implement repositories.
- Seed default categories.

### Milestone 3: Manual Tracking

- Cashbook CRUD.
- Category CRUD.
- Add transaction.
- Edit transaction.
- Transaction list.
- Amount input modes.

### Milestone 4: Reports

- Monthly summary.
- Time dimension report.
- Category dimension report.
- Matrix report.
- Filters.
- Charts.

### Milestone 5: Recurring and Import

- Recurring transactions.
- CSV export.
- CSV import.
- Duplicate detection.

### Milestone 6: Automation

- TNG notification listener.
- TNG parser.
- Quick-add notification.
- Smart category suggestion.

### Milestone 7: Backup and Security

- Google Drive backup.
- Restore.
- Backup encryption.
- App lock.

## 18. Open Technical Questions

- Which Chinese variant should be the default: Simplified Chinese, Traditional Chinese, or both?
- Should each cashbook have separate default categories, or should categories be shared globally?
- Should TNG quick add support only Touch 'n Go at first, or have a generic notification parser framework for other wallets later?
- Should backup restore replace all local data or support merge in the first release?
- Should Room database encryption be included in MVP or after backup is stable?
- Which chart library should be used, or should charts be custom Compose Canvas components?
