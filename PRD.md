# Budget Tracker Android App PRD

## 1. Product Overview

### 1.1 Product Name

Working name: **Budget Tracker**

### 1.2 Product Goal

Build a private, ad-free Android budget tracking app for daily personal finance management in Malaysia. The app should make expense recording fast, support multiple cashbooks, provide clear reports, and reduce manual work by reading payment notifications such as Touch 'n Go eWallet transaction notifications.

### 1.3 Target User

- Individual users who track daily spending, income, and balances.
- Users who frequently pay with Touch 'n Go eWallet, bank cards, cash, or other wallets.
- Users who want local-first storage with optional Google Drive backup.
- Users migrating from existing budget tracker apps.

### 1.4 Core Value

- Fast expense entry.
- Smart category suggestions based on habits.
- Clear monthly reports for expenses, income, and balance.
- Multiple cashbooks for different financial contexts.
- Local data ownership with optional cloud backup.
- Import existing records from other apps.

## 2. Scope

### 2.1 MVP Scope

- Manual expense and income recording.
- Multiple cashbooks.
- Categories with icons and colors.
- Home dashboard.
- Monthly reports.
- Charts and filters.
- Recurring transactions.
- Amount input mode setting.
- English and Chinese language support.
- Local database storage.
- CSV import and export.

### 2.2 Post-MVP Scope

- Touch 'n Go notification detection and quick add.
- Smart category ranking and recommendations.
- Google Drive automatic backup and restore.
- Receipt attachment.
- Budget limits.
- Advanced analytics.
- Multi-device sync conflict handling.

## 3. Main Navigation

Recommended bottom navigation:

- **Home**
- **Charts**
- **Add**
- **Reports**
- **Profile**

Additional screens:

- Cashbook switcher
- Transaction detail
- Add or edit transaction
- Categories
- Recurring transactions
- Import and export
- Backup and restore
- Settings

## 4. Core Features

## 4.1 Home Dashboard

The Home screen shows a quick overview for the selected month and selected cashbook.

### Requirements

- Show current month.
- Show total expenses.
- Show total income.
- Show balance.
- Show today's expenses.
- Show recent transactions grouped by date.
- Support switching month.
- Support switching cashbook.
- Support "All Cashbooks" view.
- Provide quick add button.
- Show recurring transactions summary for the current month.

### Example Metrics

- Expenses: RM 3,138.34
- Income: RM 0.00
- Balance: -RM 3,138.34
- Today: RM 63.10

## 4.2 Transactions

Users can record:

- Expense
- Income

### Transaction Fields

- Amount
- Type: expense or income
- Date and time
- Cashbook
- Category
- Note
- Tags
- Source: manual, notification, import, recurring
- Attachment or receipt image

### Requirements

- Add transaction.
- Edit transaction.
- Delete transaction.
- Duplicate transaction.
- Search transaction.
- Filter by date, type, category, cashbook, amount, tag, or source.
- Group transactions by date.
- Support negative display for expenses.

## 4.3 Cashbooks

Cashbooks allow users to separate different financial records.

### Example Cashbooks

- Personal
- Family
- Business
- Travel
- Shared Expenses

### Requirements

- Create multiple cashbooks.
- Rename cashbook.
- Archive cashbook.
- Delete cashbook with confirmation.
- Set default cashbook.
- View one cashbook or all cashbooks combined.
- Each transaction must belong to one cashbook.
- Each cashbook can have its own categories, recurring transactions, and reports.

## 4.4 Categories

Categories help classify transactions.

### Example Expense Categories

- Food
- Transport
- Petrol
- Parking
- Rental fee
- Phone
- Subscription
- Shopping
- Doctor
- Entertainment
- Daily
- Other

### Example Income Categories

- Salary
- Bonus
- Refund
- Gift
- Other

### Requirements

- Add, edit, delete, and reorder categories.
- Set icon and color.
- Separate expense and income categories.
- Allow category ranking based on usage habits.
- Show frequently used categories first.
- Support category merge when importing old data.

## 4.5 Amount Input Modes

Users can choose how amounts are entered.

### Mode A: Normal Decimal Input

The user manually enters the decimal point.

Examples:

- Input `56.90` means `RM 56.90`.
- Input `12` means `RM 12.00`.
- Input `12.5` means `RM 12.50`.

### Mode B: Auto Cents Input

The app treats the last two digits as cents.

Examples:

- Input `5690` means `RM 56.90`.
- Input `56` means `RM 0.56`.
- Input `5` means `RM 0.05`.
- Input `100000` means `RM 1,000.00`.

### Requirements

- User can switch input mode in settings.
- The selected mode applies to the add transaction screen.
- The amount preview updates immediately while typing.
- Backspace should update the formatted amount correctly.
- The setting should be app-wide for MVP.

## 4.6 Recurring Transactions

Recurring transactions are fixed or repeated expenses and income.

### Examples

- Rental fee
- Phone bill
- Insurance
- Netflix
- Spotify
- Salary
- Loan repayment

### Requirements

- Create recurring expense or income.
- Set frequency:
  - Daily
  - Weekly
  - Monthly
  - Yearly
  - Custom interval
- Set start date.
- Set optional end date.
- Set category, cashbook, amount, and note.
- Auto-generate transaction when due.
- Option to require confirmation before adding.
- Skip one occurrence.
- Pause recurring transaction.
- Show monthly recurring total on Home.
- Show upcoming recurring transactions.

## 4.7 Reports

Reports provide financial summaries by month, category, and cashbook.

### Report Types

- Monthly overview
- Expense report
- Income report
- Balance report
- Category report
- Cashbook report

### Monthly Overview

For each month, show:

- Total income
- Total expenses
- Balance
- Expense by category
- Income by category
- Daily expense trend
- Recurring expense total
- Top categories

### Analysis Dimensions

Reports should support two main ways of viewing data:

- **Time dimension**: start from a time range, then see all expenses, income, balance, and category breakdown inside that period.
- **Category dimension**: start from one category, then see its total spending across multiple months, trend by month, and related transactions.

### Time Dimension Examples

- View all expenses for July 2026.
- View income, expenses, and balance for each month in 2026.
- Compare monthly spending from January to December.
- View one cashbook's monthly expenses.
- View all cashbooks combined for a selected month.

### Category Dimension Examples

- Select Food and view total Food spending from January to July.
- Select Rental fee and view monthly totals for the whole year.
- Select Petrol and compare spending across multiple months.
- Select one category and drill down into all matching transactions.
- Select multiple categories and compare their monthly trends.

### Matrix View

The report should support a table-like summary with time and category together:

- Rows: months.
- Columns: categories.
- Cells: total amount for that category in that month.
- Row total: total expense or income for the month.
- Column total: total amount for that category across the selected period.

Example:

| Month | Food | Petrol | Rental fee | Total |
| --- | ---: | ---: | ---: | ---: |
| Jan 2026 | RM 320.00 | RM 180.00 | RM 1,950.00 | RM 2,450.00 |
| Feb 2026 | RM 280.00 | RM 220.00 | RM 1,950.00 | RM 2,480.00 |
| Total | RM 600.00 | RM 400.00 | RM 3,900.00 | RM 4,930.00 |

### Filters

Reports should support filters by:

- Date range
- Month
- Year
- Cashbook
- Category
- Transaction type
- Tag
- Amount range

## 4.8 Charts

Charts help users understand spending patterns.

### Chart Types

- Donut chart for category percentage.
- Bar chart for monthly income and expenses.
- Line chart for spending trend.
- Calendar heatmap for daily spending.

### Requirements

- Support week, month, and year views.
- Support category breakdown.
- Support cashbook filter.
- Tapping a chart segment should show related transactions.

## 4.9 Payment Notification Quick Add

The app should reduce manual entry for supported payment and banking transaction notifications.

### Initial Supported Sources

- Touch 'n Go eWallet
- Public Bank transaction notifications

### User Flow

1. User pays with a supported payment app or card.
2. The payment or banking app sends a transaction notification.
3. Budget Tracker reads the notification through Android notification access.
4. Budget Tracker extracts amount and time.
5. Budget Tracker shows its own quick-add notification.
6. User taps "Add" to save the transaction.
7. User can tap "Edit" to review or change category before saving.

### Requirements

- Ask user to enable notification access.
- Detect TNG transaction notifications.
- Detect Public Bank transaction notifications.
- Parse amount.
- Parse transaction time if available.
- Pre-fill transaction form.
- Suggest category.
- Avoid duplicate transactions.
- Store original notification text for debugging, if user allows.
- Allow the user to enable or disable each notification source separately.
- Quick-add notifications should not be persistent or ongoing.
- Quick-add notifications should be dismissible by the user.
- Quick-add notifications should auto-expire after a short time if ignored.
- Ignoring or dismissing a quick-add notification should not block the app.
- Dismissed drafts can be stored as optional history, but they should not require user action.

### Important Limitation

Android apps cannot freely modify another app's notification UI. The expected implementation is to read supported payment notifications and show Budget Tracker's own optional quick-add notification or shortcut.

## 4.10 Smart Category Suggestion

The app should learn user habits and recommend categories.

### Signals

- Time of day
- Day of week
- Amount range
- Cashbook
- Recent selected categories
- Historical category for similar transactions

### Example

If the user often records transactions between 12:00 PM and 2:00 PM with amounts between RM 8 and RM 20 as Food, the app should suggest Food first for similar future transactions.

### Requirements

- Recommend category when adding transaction.
- Rank categories based on score.
- Show top categories first.
- Learn from manual corrections.
- Work fully offline.
- Allow user to reset learning data.

### Suggested Scoring Model

- Same time block: medium weight.
- Same day type: medium weight.
- Same amount range: medium weight.
- Recent use: medium weight.
- Global frequency: low weight.

## 4.11 Budget Limits

Users can set spending limits.

### Requirements

- Set monthly budget per category.
- Set monthly total budget per cashbook.
- Show remaining budget.
- Alert when near limit.
- Alert when over budget.
- Include budget usage in reports.

## 4.12 Tags

Tags provide flexible labels beyond categories.

### Example Tags

- work
- family
- trip
- claimable
- tax
- emergency

### Requirements

- Add multiple tags to one transaction.
- Filter transactions by tag.
- Include tags in export and import.

## 4.13 Import and Export

The app should allow migration from existing budget tracker apps.

### Export Formats

- CSV
- JSON backup
- SQLite backup, optional

### Import Formats

- CSV
- JSON backup
- Excel, optional

### Import Flow

1. User selects import file.
2. App detects columns.
3. User maps columns to fields.
4. App previews records.
5. App detects possible duplicates.
6. User confirms import.
7. App imports records into selected cashbook.
8. App shows import summary.

### Field Mapping

Common fields:

- Date
- Time
- Amount
- Type
- Category
- Note
- Tags

### Duplicate Detection

Possible duplicate if these match:

- Same date
- Same amount
- Same type
- Same note
- Same cashbook

## 4.14 Google Drive Backup and Restore

Google Drive support should start as backup and restore, not real-time multi-device sync.

### Requirements

- User signs in with Google.
- App creates encrypted backup.
- Upload backup when network is available.
- Restore backup on new device.
- Show last backup time.
- Allow manual backup.
- Allow manual restore.
- Keep several backup versions.

### Backup Content

- Transactions
- Categories
- Cashbooks
- Recurring transactions
- Budgets
- Tags
- Settings
- Learning data

### Security

- Backup should be encrypted before upload.
- User financial data should not be stored as plain text in Drive.

## 4.15 Security and Privacy

### Requirements

- App lock with PIN.
- Biometric unlock.
- Auto-lock after inactivity.
- Local database encryption, optional for MVP.
- Encrypted cloud backup.
- User can delete all local data.
- User can export data before deleting.
- Notification text storage should be optional.

## 5. Data Model Draft

## 5.1 Cashbook

- id
- name
- currency
- color
- icon
- isDefault
- isArchived
- createdAt
- updatedAt

## 5.2 Category

- id
- cashbookId
- name
- type
- icon
- color
- sortOrder
- isArchived
- createdAt
- updatedAt

## 5.3 Transaction

- id
- cashbookId
- categoryId
- amount
- type
- dateTime
- note
- source
- originalSourceId
- createdAt
- updatedAt

## 5.4 RecurringTransaction

- id
- cashbookId
- categoryId
- amount
- type
- frequency
- startDate
- endDate
- nextRunDate
- requireConfirmation
- isPaused
- note
- createdAt
- updatedAt

## 5.5 Budget

- id
- cashbookId
- categoryId
- periodType
- amount
- startDate
- endDate
- createdAt
- updatedAt

## 5.6 CategoryLearningRule

- id
- cashbookId
- timeBlock
- dayType
- amountMin
- amountMax
- categoryId
- score
- lastUsedAt
- createdAt
- updatedAt

## 5.7 ImportBatch

- id
- cashbookId
- fileName
- sourceApp
- importedCount
- skippedCount
- createdAt

## 6. Settings

### General

- Currency, default MYR.
- Language: follow system, English, Chinese.
- Theme: light, dark, system.
- Default cashbook.

### Language and i18n

The app should support English and Chinese from MVP.

### Requirements

- User can switch language in settings.
- Supported language options:
  - Follow system
  - English
  - Chinese
- Language change should apply to all screens without losing user data.
- All app UI text should use localization resources.
- System notifications from Budget Tracker should be localized.
- Report labels, filter labels, settings labels, empty states, validation messages, and error messages should be localized.
- Default built-in category names should have English and Chinese display names.
- User-created category names should stay as entered by the user.
- Exported transaction data should preserve user-entered category names and notes.
- Imported categories should not be auto-translated unless the user chooses to merge or rename them.

### Amount Input

- Normal decimal input.
- Auto cents input.

### Notification Integration

- Enable notification access.
- Enable TNG quick add.
- Store original notification text.

### Backup

- Google Drive sign in.
- Auto backup on Wi-Fi only.
- Manual backup.
- Manual restore.

### Security

- PIN lock.
- Biometric unlock.
- Auto-lock timeout.

## 7. Technical Architecture

### Recommended Stack

- Language: Kotlin
- UI: Jetpack Compose
- Architecture: MVVM or MVI
- Database: Room
- Background jobs: WorkManager
- Notifications: NotificationListenerService
- Cloud backup: Google Drive API
- Dependency injection: Hilt
- Serialization: Kotlinx Serialization
- Localization: Android string resources or Compose-compatible resource wrapper

### Suggested Modules

- app
- core-database
- core-model
- core-i18n
- core-ui
- feature-home
- feature-add-transaction
- feature-reports
- feature-charts
- feature-cashbook
- feature-category
- feature-recurring
- feature-import-export
- feature-backup
- feature-notification
- feature-settings

## 8. Development Milestones

## Milestone 1: Foundation

- Android project setup.
- Compose navigation.
- Room database.
- Core data models.
- Basic theme.
- Cashbook CRUD.
- Category CRUD.

## Milestone 2: Manual Tracking

- Add expense.
- Add income.
- Edit and delete transaction.
- Home dashboard.
- Transaction list.
- Amount input modes.

## Milestone 3: Reports and Charts

- Monthly income, expense, balance report.
- Category breakdown.
- Cashbook report.
- Filters.
- Basic charts.

## Milestone 4: Recurring Transactions

- Recurring transaction setup.
- Auto-generate due transactions.
- Upcoming recurring list.
- Skip, pause, and confirm recurring items.

## Milestone 5: Import and Export

- CSV export.
- CSV import.
- Field mapping.
- Import preview.
- Duplicate detection.

## Milestone 6: TNG Notification Quick Add

- Notification access onboarding.
- TNG notification parser.
- Quick-add notification.
- Pre-filled transaction screen.
- Duplicate prevention.

## Milestone 7: Smart Category Suggestion

- Category ranking.
- Time-based suggestion.
- Recent usage ranking.
- Learning from corrections.

## Milestone 8: Backup and Restore

- Google sign-in.
- Encrypted JSON backup.
- Google Drive upload.
- Restore from Drive.
- Backup history.

## 9. Open Questions

- Should cashbook categories be fully separate, or should users be able to share global categories across cashbooks?
- Should Google Drive backup use appDataFolder or a user-visible folder?
- Should TNG notification import auto-save transactions, or always require user confirmation?
- Should recurring transactions be generated automatically at midnight, or only when the user opens the app?
- Should the app support multiple currencies in MVP?
- Should receipt images be included in Google Drive backup by default?

## 10. Success Criteria

- User can record a transaction in under 5 seconds.
- User can view monthly expenses, income, and balance clearly.
- User can separate records using multiple cashbooks.
- User can import old records from CSV.
- User can restore data from backup.
- TNG transactions can be converted into pre-filled records with minimal manual typing.
- Category suggestions become more accurate as the user uses the app.
