# Budget Tracker Development Process

This document lists the recommended end-to-end development flow for the Android Budget Tracker app.

## Phase 1: Product Planning

### Goal

Define what the app should do and what should be built first.

### Main Output

- `PRD.md`

### Tasks

- Confirm target users.
- Confirm MVP features.
- Confirm post-MVP features.
- Confirm app modules.
- Confirm special features:
  - TNG notification quick add.
  - Smart category suggestion.
  - Multiple cashbooks.
  - Recurring transactions.
  - Reports by time and category.
  - English and Chinese i18n.
  - Google Drive backup.
  - Import from old app.

### Current Status

- Initial PRD has been created.
- Next step is to review and refine the PRD until feature scope is clear.

## Phase 2: Technical Requirements Document

### Goal

Turn product requirements into technical decisions and implementation details.

### Main Output

- `TRD.md`

### TRD Should Include

- Android technology stack.
- App architecture.
- Module structure.
- Database schema.
- Data flow.
- Navigation structure.
- Notification listener design.
- TNG notification parsing strategy.
- Smart category suggestion algorithm.
- Import and export design.
- Google Drive backup design.
- i18n implementation.
- Security and privacy design.
- Testing strategy.

### Recommended Technical Stack

- Kotlin
- Jetpack Compose
- Room
- WorkManager
- Hilt
- Kotlinx Serialization
- Android NotificationListenerService
- Google Drive API

## Phase 3: Information Architecture

### Goal

Define all screens and how users move between them.

### Main Output

- `SCREEN_FLOW.md`

### Tasks

- List all screens.
- Define bottom navigation.
- Define major user flows.
- Define settings structure.
- Define empty states.
- Define error states.

### Important Flows

- Add expense manually.
- Add income manually.
- TNG notification quick add.
- Create cashbook.
- Switch cashbook.
- Add recurring transaction.
- View monthly report.
- View category report across months.
- Import CSV.
- Backup and restore.
- Change language.

## Phase 4: UI / UX Design

### Goal

Design the app visually before coding too much.

### Main Output

- `UI_SPEC.md`
- Figma design, optional
- Screenshot references, optional

### Screens To Design First

- Home
- Add Transaction
- Category Picker
- Charts
- Reports
- Cashbook Switcher
- Recurring Transactions
- Settings
- Import Preview
- Backup and Restore

### UI Decisions

- Dark mode first or system theme first.
- Main color.
- Category icon style.
- Amount input keypad layout.
- Report chart style.
- Bottom navigation behavior.
- Card and list spacing.
- Chinese and English text length handling.

## Phase 5: Data Model Design

### Goal

Finalize the database before implementing features.

### Main Output

- `DATABASE_SCHEMA.md`

### Tables

- Cashbook
- Category
- Transaction
- RecurringTransaction
- Budget
- Tag
- TransactionTag
- CategoryLearningRule
- ImportBatch
- BackupMetadata

### Tasks

- Define table fields.
- Define indexes.
- Define relationships.
- Define delete behavior.
- Define migration strategy.
- Define seed data for default categories.

## Phase 6: Android Project Setup

### Goal

Create the real Android project structure.

### Main Output

- Android Studio / Gradle project

### Tasks

- Create Kotlin Android project.
- Set up Jetpack Compose.
- Set up navigation.
- Set up app theme.
- Set up module structure.
- Add Room.
- Add Hilt.
- Add Kotlinx Serialization.
- Add baseline app screens.
- Add English and Chinese string resources.

## Phase 7: MVP Implementation

### Goal

Build the first usable local-only version.

### Recommended Build Order

1. Core models.
2. Room database.
3. Repository layer.
4. Cashbook CRUD.
5. Category CRUD.
6. Add transaction.
7. Transaction list.
8. Home monthly summary.
9. Amount input modes.
10. Basic reports.
11. Basic charts.
12. Filters.
13. Recurring transactions.
14. CSV export.
15. CSV import.
16. i18n polish.

### MVP Exit Criteria

- User can create a cashbook.
- User can add expense and income.
- User can view monthly income, expenses, and balance.
- User can filter records by time and category.
- User can use both amount input modes.
- User can use the app in English and Chinese.
- User can import and export CSV.

## Phase 8: TNG Notification Feature

### Goal

Add fast expense recording from Touch 'n Go notifications.

### Main Output

- TNG notification quick-add flow

### Tasks

- Add notification access onboarding.
- Implement NotificationListenerService.
- Identify TNG package name.
- Capture sample TNG notification text.
- Build parser for amount.
- Add duplicate detection.
- Show Budget Tracker quick-add notification.
- Open pre-filled add transaction screen.
- Save transaction after user confirmation.

### Important Note

The app should not auto-save TNG transactions in the first version. It should pre-fill the record and let the user confirm.

## Phase 9: Smart Category Suggestion

### Goal

Reduce manual category selection.

### Tasks

- Track user category choices.
- Build rule-based scoring.
- Use time, day, amount, and recent usage.
- Rank category picker.
- Learn from corrections.
- Add reset learning data option.

## Phase 10: Google Drive Backup

### Goal

Protect user data and support restore on another phone.

### Tasks

- Add Google sign-in.
- Export local data to backup JSON.
- Encrypt backup.
- Upload to Google Drive.
- Show last backup time.
- Restore from selected backup.
- Keep backup history.
- Use WorkManager for automatic backup.

## Phase 11: Testing

### Goal

Make sure important finance behavior is correct.

### Test Areas

- Amount input formatting.
- Monthly total calculation.
- Balance calculation.
- Recurring transaction generation.
- CSV import parsing.
- Duplicate detection.
- Category suggestion scoring.
- Language switching.
- Google Drive backup and restore.
- TNG notification parsing.

### Recommended Tests

- Unit tests for calculations.
- Unit tests for amount input modes.
- Unit tests for import parsing.
- Unit tests for category suggestion.
- Room database tests.
- UI tests for critical flows.

## Phase 12: Beta Release

### Goal

Use the app personally and fix real-life workflow issues.

### Tasks

- Install on personal Android phone.
- Use for at least 2 to 4 weeks.
- Compare totals with old budget app.
- Test TNG notification cases.
- Test recurring transactions at month change.
- Test backup and restore.
- Collect pain points.

## Phase 13: Production Release

### Goal

Prepare a stable app release.

### Tasks

- App icon.
- App name.
- Privacy policy.
- Backup warning text.
- Permission explanation screens.
- Play Store listing, if publishing.
- Release build.
- Crash reporting, optional.

## Recommended Immediate Next Steps

1. Review `PRD.md`.
2. Write `TRD.md`.
3. Write `SCREEN_FLOW.md`.
4. Write `UI_SPEC.md`.
5. Finalize `DATABASE_SCHEMA.md`.
6. Create the Android project.
7. Build the MVP in the recommended order.
