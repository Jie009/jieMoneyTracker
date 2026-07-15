# Budget Tracker Screen Flow

## 1. Navigation Overview

The app uses a bottom navigation layout for daily use.

Bottom tabs:

- Home
- Charts
- Add
- Reports
- Profile

Global entry points:

- Cashbook switcher
- Search
- Settings
- Notification quick add
- Import and export
- Backup and restore

## 2. First Launch Flow

### 2.1 New User Onboarding

Flow:

1. Splash
2. Language selection
3. Welcome
4. Create first cashbook
5. Confirm default categories
6. Choose amount input mode
7. Optional: enable app lock
8. Optional: enable Google Drive backup
9. Home

### 2.2 Returning User

Flow:

1. Splash
2. App lock, if enabled
3. Home

### 2.3 Restore From Backup

Flow:

1. Splash
2. Language selection
3. Welcome
4. Restore from Google Drive
5. Select backup
6. Backup preview
7. Confirm restore
8. Home

## 3. Home Tab

### 3.1 Home Screen

Purpose:

- Show monthly summary and recent transactions.

Main UI:

- Top app bar:
  - Menu
  - App title
  - Search
  - Calendar / month picker
- Cashbook selector
- Month selector
- Summary:
  - Expenses
  - Income
  - Balance
- Recurring transaction summary
- Recent transaction list grouped by date
- Floating add button

Actions:

- Change month.
- Change cashbook.
- Search transaction.
- Open transaction detail.
- Open recurring transactions.
- Tap add button.

### 3.2 Cashbook Switcher

Entry:

- Home top area
- Reports filter
- Settings

Flow:

1. User taps current cashbook.
2. Bottom sheet opens.
3. User selects:
   - One cashbook
   - All cashbooks
   - Manage cashbooks
4. App updates current scope.

## 4. Add Tab

### 4.1 Add Transaction Screen

Purpose:

- Add expense or income quickly.

Main UI:

- Top bar:
  - Cancel
  - Title
  - Save
- Type switcher:
  - Expense
  - Income
- Category picker grid
- Amount display
- Note input
- Date selector
- Tags, optional
- Custom keypad

Actions:

- Select type.
- Select category.
- Enter amount.
- Add note.
- Change date.
- Save transaction.

### 4.2 Expense Flow

Flow:

1. Tap Add.
2. Select Expense.
3. Select category.
4. Enter amount.
5. Add note, optional.
6. Save.
7. Return to previous screen or Home.

### 4.3 Income Flow

Flow:

1. Tap Add.
2. Select Income.
3. Select income category.
4. Enter amount.
5. Add note, optional.
6. Save.

### 4.4 Amount Input Mode Behavior

Normal decimal mode:

- User enters decimal point manually.
- Example: `56.90` means RM 56.90.

Auto cents mode:

- Last two digits are cents.
- Example: `5690` means RM 56.90.

The add screen should show a real-time formatted preview.

## 5. Transaction Detail Flow

### 5.1 Transaction Detail Screen

Entry:

- Home transaction list
- Reports drill-down
- Search results
- Notification draft after save

Main UI:

- Amount
- Type
- Category
- Cashbook
- Date and time
- Note
- Tags
- Source
- Receipt, if any

Actions:

- Edit.
- Duplicate.
- Delete.
- View source import batch, if imported.

### 5.2 Edit Transaction Flow

Flow:

1. Open transaction detail.
2. Tap Edit.
3. Update fields.
4. Save.
5. Summary and reports update automatically.

## 6. Charts Tab

### 6.1 Charts Screen

Purpose:

- Visualize spending and income.

Main UI:

- Period selector:
  - Week
  - Month
  - Year
  - Custom
- Cashbook filter
- Chart carousel or tabs:
  - Category donut
  - Monthly bar chart
  - Daily trend line
  - Calendar heatmap
- Category breakdown list

Actions:

- Change period.
- Tap category segment.
- Open filtered transaction list.
- Switch between expense and income.

## 7. Reports Tab

### 7.1 Reports Home

Purpose:

- Provide detailed financial analysis.

Report modes:

- Overview
- Time Dimension
- Category Dimension
- Matrix
- Cashbook

### 7.2 Monthly Overview Flow

Flow:

1. Open Reports.
2. Select Overview.
3. Select month or year.
4. Select cashbook or all cashbooks.
5. View:
   - Income
   - Expenses
   - Balance
   - Category breakdown
   - Recurring total

### 7.3 Time Dimension Flow

Purpose:

- Start from time, then analyze all spending or income inside that period.

Flow:

1. Open Reports.
2. Select Time Dimension.
3. Choose date range:
   - Single month
   - Multiple months
   - Whole year
   - Custom range
4. Choose transaction type:
   - Expense
   - Income
   - Both
5. View totals and breakdown.
6. Tap category to drill down.

Examples:

- View all expenses in July 2026.
- Compare each month in 2026.
- View total income, expense, and balance from January to July.

### 7.4 Category Dimension Flow

Purpose:

- Start from category, then analyze that category across time.

Flow:

1. Open Reports.
2. Select Category Dimension.
3. Select one or multiple categories.
4. Choose date range.
5. View:
   - Total amount.
   - Monthly trend.
   - Average per month.
   - Highest month.
   - Related transactions.

Examples:

- Food total spending from January to July.
- Petrol monthly spending trend.
- Rental fee total for the whole year.

### 7.5 Matrix Report Flow

Purpose:

- Compare month and category together.

Flow:

1. Open Reports.
2. Select Matrix.
3. Choose date range.
4. Choose transaction type.
5. Choose categories, optional.
6. View matrix:
   - Rows: months.
   - Columns: categories.
   - Cells: totals.
   - Row totals: monthly totals.
   - Column totals: category totals.
7. Tap any cell to view related transactions.

## 8. Profile Tab

### 8.1 Profile Screen

Main sections:

- Cashbooks
- Categories
- Recurring transactions
- Import and export
- Backup and restore
- Security
- Settings
- About

## 9. Settings Flow

### 9.1 Settings Screen

Sections:

- General
- Language
- Amount input
- Theme
- Notification integration
- Backup
- Security
- Data management

### 9.2 Language Flow

Flow:

1. Open Profile.
2. Open Settings.
3. Open Language.
4. Select:
   - Follow system
   - English
   - Chinese
5. App applies language.
6. User returns to previous screen.

### 9.3 Amount Input Mode Flow

Flow:

1. Open Settings.
2. Open Amount Input.
3. Select:
   - Normal decimal input
   - Auto cents input
4. App saves setting.
5. Add Transaction screen uses selected mode.

## 10. Cashbook Management Flow

### 10.1 Cashbooks Screen

Entry:

- Profile
- Cashbook switcher
- Onboarding

Actions:

- Create cashbook.
- Rename cashbook.
- Archive cashbook.
- Delete cashbook.
- Set default cashbook.

### 10.2 Create Cashbook Flow

Flow:

1. Open Cashbooks.
2. Tap Create.
3. Enter name.
4. Select currency.
5. Select icon or color.
6. Confirm.
7. Optional: create default categories.

## 11. Recurring Transactions Flow

### 11.1 Recurring List Screen

Main UI:

- Monthly recurring total.
- Upcoming recurring transactions.
- Active recurring transactions.
- Paused recurring transactions.

Actions:

- Create recurring transaction.
- Edit.
- Pause.
- Skip next occurrence.
- Delete.

### 11.2 Create Recurring Transaction Flow

Flow:

1. Open Recurring Transactions.
2. Tap Add.
3. Select type.
4. Enter amount.
5. Select category.
6. Set frequency.
7. Set start date.
8. Set end date, optional.
9. Choose auto-save or require confirmation.
10. Save.

### 11.3 Recurring Confirmation Flow

Flow:

1. Recurring item is due.
2. App shows pending confirmation.
3. User reviews.
4. User confirms, edits, skips, or pauses.

## 12. Payment Notification Quick Add Flow

### 12.1 Enable Payment Notification Integration

Flow:

1. Open Settings.
2. Open Notification Integration.
3. Enable supported sources:
   - Touch 'n Go eWallet
   - Public Bank
4. App explains notification access.
5. User opens Android notification access settings.
6. User grants permission.
7. App verifies permission.
8. Enabled payment integrations become active.

### 12.2 Quick Add From Payment Notification

Flow:

1. User pays with TNG, Public Bank card, or another supported source.
2. Payment or banking app sends notification.
3. Budget Tracker detects notification.
4. App parses amount.
5. App suggests category.
6. App shows a dismissible quick-add notification.
7. User taps Add or Edit.
8. App opens pre-filled Add Transaction screen.
9. User confirms.
10. Transaction is saved.

### 12.3 Ignored or Wrong Notification Flow

Purpose:

- The quick-add notification should not become something the user must clear manually.

Flow:

1. App shows quick-add notification.
2. User ignores it, or Android timeout expires.
3. Notification disappears automatically.
4. Draft is marked as ignored or dismissed.
5. App does not force the user to review it.

User can also swipe away the quick-add notification at any time.

### 12.4 Duplicate Notification Flow

Flow:

1. App detects possible duplicate.
2. App marks notification draft as duplicate.
3. App does not show another quick-add notification.
4. User can review duplicate drafts from settings, optional.

## 13. Import and Export Flow

### 13.1 CSV Import Flow

Flow:

1. Open Profile.
2. Open Import and Export.
3. Select Import CSV.
4. Choose file.
5. Select target cashbook.
6. Map columns.
7. Preview records.
8. Review errors and duplicates.
9. Confirm import.
10. View import summary.

### 13.2 CSV Export Flow

Flow:

1. Open Import and Export.
2. Select Export CSV.
3. Choose cashbook or all cashbooks.
4. Choose date range.
5. Choose fields.
6. Export file.

## 14. Backup and Restore Flow

### 14.1 Enable Google Drive Backup

Flow:

1. Open Backup and Restore.
2. Tap Connect Google Drive.
3. Sign in with Google.
4. Choose backup settings:
   - Auto backup
   - Wi-Fi only
   - Backup frequency
5. Run first backup.

### 14.2 Manual Backup Flow

Flow:

1. Open Backup and Restore.
2. Tap Backup Now.
3. App exports encrypted backup.
4. App uploads to Drive.
5. App shows success and backup time.

### 14.3 Restore Flow

Flow:

1. Open Backup and Restore.
2. Tap Restore.
3. Select backup.
4. View backup preview.
5. Confirm restore.
6. App restores data.
7. App returns to Home.

## 15. Search and Filter Flow

### 15.1 Search Flow

Entry:

- Home top bar
- Reports drill-down

Flow:

1. Tap Search.
2. Enter keyword.
3. App searches note, category, and tags.
4. User opens transaction detail.

### 15.2 Filter Flow

Filters:

- Date range
- Month
- Year
- Cashbook
- Category
- Transaction type
- Tag
- Amount range

Flow:

1. Open filter sheet.
2. Select filters.
3. Apply.
4. Result list and report update.
5. User can clear filters.

## 16. Error and Empty States

### 16.1 Empty States

- No transactions this month.
- No categories.
- No cashbooks.
- No recurring transactions.
- No import file selected.
- No backup found.
- No report data for selected filters.

### 16.2 Error States

- Invalid amount.
- Missing category.
- Import file cannot be read.
- Import date format invalid.
- Google Drive sign-in failed.
- Backup upload failed.
- Restore failed.
- Notification access not enabled.

## 17. Navigation Routes Draft

Suggested route names:

- `home`
- `charts`
- `add_transaction`
- `edit_transaction/{transactionId}`
- `transaction_detail/{transactionId}`
- `reports`
- `reports/time`
- `reports/category`
- `reports/matrix`
- `profile`
- `settings`
- `settings/language`
- `settings/amount-input`
- `cashbooks`
- `categories`
- `recurring`
- `import_export`
- `backup_restore`
- `notification_integration`

## 18. Next UI Design Step

After this screen flow is approved, create `UI_SPEC.md` with:

- Design style.
- Color palette.
- Typography.
- Component list.
- Screen-by-screen layout.
- Chinese and English text examples.
- Empty and error state designs.
