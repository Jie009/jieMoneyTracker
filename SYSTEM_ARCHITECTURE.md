# Budget Tracker System Architecture and Folder Structure

## 1. 目标

这份文档定义 Budget Tracker Android App 的整体系统架构、folder structure、模块边界和依赖规则。

核心目标：

- 后续加功能简单。
- 模块之间 low coupling。
- 每个模块 high cohesion。
- 业务逻辑容易测试。
- UI、domain、data 分层清楚。
- 支持长期维护，不会越写越乱。

## 2. 参考架构

本项目参考以下主流 Android 架构方向：

- Android 官方 Recommended App Architecture。
- Android 官方 Modularization Guide。
- Google Now in Android 的 multi-module 架构。
- Jetpack Compose + ViewModel + StateFlow 的单向数据流。
- Room + Repository 的 local-first data layer。
- Hilt dependency injection。

主要原则：

- App 至少分成 UI layer 和 data layer。
- 复杂业务逻辑放到 domain layer。
- UI 通过 ViewModel 暴露的 state 渲染。
- Repository 是其他 layer 访问数据的唯一入口。
- Feature module 不直接依赖另一个 feature module。
- Shared code 只放到 core module。
- Navigation 传 simple id，不传复杂对象。

## 3. 架构总览

推荐采用：

- Single Activity。
- Jetpack Compose navigation。
- MVVM + Unidirectional Data Flow。
- Multi-module Gradle project。
- Local-first data source。
- Optional cloud backup。

整体分层：

```text
UI Layer
  Compose Screen
  ViewModel
  UI State
  UI Event

Domain Layer
  Use Case
  Business Rules
  Report Aggregation
  Category Suggestion
  Amount Formatting

Data Layer
  Repository Interface
  Repository Implementation
  Room DAO
  DataStore
  Import / Export
  Backup Source

Platform Layer
  Notification Listener
  WorkManager
  Google Drive API
  Android Permissions
```

## 4. Dependency Rules

依赖方向必须单向。

```text
app
  -> feature modules
  -> core modules

feature modules
  -> core modules only

core modules
  -> other core modules only when needed

core modules
  -> never depend on feature modules

feature modules
  -> should not depend on other feature impl modules
```

禁止：

- `feature-home` 直接依赖 `feature-reports` implementation。
- `core-data` 依赖任何 `feature-*`。
- `core-model` 依赖 Android UI。
- ViewModel 直接访问 DAO。
- Compose screen 直接访问 Repository。
- Navigation argument 传整个 Transaction object。

允许：

- `app` 组合所有 feature navigation。
- `feature-home` 依赖 `core-domain`。
- `feature-reports` 依赖 `core-domain`。
- `core-data` 依赖 `core-database`。
- `core-domain` 依赖 `core-model`。

## 5. Recommended Gradle Module Structure

```text
budgetTracker/
  settings.gradle.kts
  build.gradle.kts
  gradle/
  build-logic/

  app/

  core/
    model/
    common/
    database/
    datastore/
    data/
    domain/
    ui/
    design-system/
    i18n/
    notification/
    backup/
    import-export/
    testing/

  feature/
    home/
    add-transaction/
    transactions/
    reports/
    charts/
    cashbook/
    category/
    recurring/
    settings/
    profile/
    notification-integration/
    backup-restore/
    import-export/

  docs/
```

说明：

- `app` 只负责 app entry point、navigation host、theme wiring、DI wiring。
- `core` 放共享能力。
- `feature` 放具体页面和功能。
- `build-logic` 放 Gradle convention plugins，避免每个 module 重复配置。
- `docs` 放 PRD、TRD、Screen Flow、UI Spec 等文档。

## 6. App Module

路径：

```text
app/
  src/main/
    AndroidManifest.xml
    java/com/budgettracker/app/
      BudgetTrackerApplication.kt
      MainActivity.kt
      BudgetTrackerApp.kt
      navigation/
        AppNavHost.kt
        TopLevelDestination.kt
      di/
        AppModule.kt
```

职责：

- Android app entry point。
- Hilt application setup。
- Single Activity。
- Compose root。
- App-level navigation graph。
- Bottom navigation。
- App theme setup。
- Permission-level integration wiring。

不应该放：

- 业务计算。
- Room DAO。
- Repository implementation。
- Feature screen implementation。

## 7. Core Modules

## 7.1 core:model

路径：

```text
core/model/
  src/main/java/com/budgettracker/core/model/
    Cashbook.kt
    Category.kt
    Transaction.kt
    RecurringTransaction.kt
    Budget.kt
    Money.kt
    CurrencyCode.kt
    TransactionType.kt
    AmountInputMode.kt
    LanguageMode.kt
    PaymentNotificationSource.kt
```

职责：

- 纯 Kotlin domain model。
- enum/value object。
- 不依赖 Android framework。
- 可以被所有 module 使用。

规则：

- 不放 Room annotation。
- 不放 Compose。
- 不放 repository implementation。

## 7.2 core:common

路径：

```text
core/common/
  src/main/java/com/budgettracker/core/common/
    dispatcher/
      AppDispatchers.kt
      Dispatcher.kt
    result/
      AppResult.kt
    time/
      ClockProvider.kt
    util/
      DateRange.kt
```

职责：

- 通用工具。
- Coroutine dispatcher。
- Time provider。
- Result wrapper。
- Date range helper。

规则：

- 保持很小。
- 不要把业务逻辑随便塞进 common。
- 如果某段逻辑属于 finance/report/category，应该放 domain。

## 7.3 core:database

路径：

```text
core/database/
  src/main/java/com/budgettracker/core/database/
    BudgetTrackerDatabase.kt
    dao/
      CashbookDao.kt
      CategoryDao.kt
      TransactionDao.kt
      RecurringTransactionDao.kt
      BudgetDao.kt
      TagDao.kt
      CategoryLearningRuleDao.kt
      NotificationDraftDao.kt
    entity/
      CashbookEntity.kt
      CategoryEntity.kt
      TransactionEntity.kt
      RecurringTransactionEntity.kt
      BudgetEntity.kt
      TagEntity.kt
      CategoryLearningRuleEntity.kt
      NotificationDraftEntity.kt
    mapper/
      CashbookEntityMapper.kt
      TransactionEntityMapper.kt
    migration/
      DatabaseMigrations.kt
    di/
      DatabaseModule.kt
```

职责：

- Room database。
- Entity。
- DAO。
- Migration。
- Entity mapper。

规则：

- 只处理本地数据库。
- 不知道 UI。
- 不知道 Google Drive。
- 不知道 notification parser。

## 7.4 core:datastore

路径：

```text
core/datastore/
  src/main/java/com/budgettracker/core/datastore/
    AppSettingsDataSource.kt
    UserPreferences.kt
    di/
      DataStoreModule.kt
```

职责：

- 保存轻量 settings。
- language mode。
- theme mode。
- amount input mode。
- default cashbook。
- backup settings。
- notification source enable/disable。

规则：

- 不保存 transaction。
- 不保存大型数据。

## 7.5 core:data

路径：

```text
core/data/
  src/main/java/com/budgettracker/core/data/
    repository/
      CashbookRepository.kt
      CategoryRepository.kt
      TransactionRepository.kt
      RecurringTransactionRepository.kt
      ReportRepository.kt
      SettingsRepository.kt
      PaymentNotificationRepository.kt
    repository/impl/
      OfflineCashbookRepository.kt
      OfflineTransactionRepository.kt
      OfflineReportRepository.kt
    di/
      DataModule.kt
```

职责：

- Repository public API。
- Repository implementation。
- 协调 database、datastore、backup、import/export。
- 对上层隐藏 data source 细节。

规则：

- ViewModel 只能通过 repository 或 use case 访问数据。
- DAO 不暴露给 feature module。
- Repository 返回 Flow 或 suspend function。

## 7.6 core:domain

路径：

```text
core/domain/
  src/main/java/com/budgettracker/core/domain/
    amount/
      FormatAmountInputUseCase.kt
      ParseAmountInputUseCase.kt
    transaction/
      AddTransactionUseCase.kt
      UpdateTransactionUseCase.kt
      DeleteTransactionUseCase.kt
    report/
      GetMonthlySummaryUseCase.kt
      GetCategoryTrendUseCase.kt
      GetMatrixReportUseCase.kt
    recurring/
      GenerateDueRecurringTransactionsUseCase.kt
      CalculateNextRecurringDateUseCase.kt
    category/
      SuggestCategoryUseCase.kt
      UpdateCategoryLearningUseCase.kt
    importexport/
      DetectDuplicateTransactionsUseCase.kt
```

职责：

- 业务规则。
- 金额输入逻辑。
- 报表聚合。
- recurring 生成规则。
- category suggestion scoring。
- duplicate detection。

规则：

- Use case 应该小而清楚。
- 可以组合多个 repository。
- 可单元测试。
- 不依赖 Compose。
- 尽量不依赖 Android framework。

## 7.7 core:ui

路径：

```text
core/ui/
  src/main/java/com/budgettracker/core/ui/
    component/
      AppTopBar.kt
      AppBottomBar.kt
      AmountKeypad.kt
      CategoryIcon.kt
      TransactionListItem.kt
      CashbookSelector.kt
      EmptyState.kt
      ErrorState.kt
      FilterSheet.kt
    formatter/
      MoneyUiFormatter.kt
      DateUiFormatter.kt
```

职责：

- 可复用 Compose UI 组件。
- UI formatter。
- 通用 empty/error/loading state。

规则：

- 不放 screen-specific ViewModel。
- 不访问 repository。
- 不包含 feature-specific business logic。

## 7.8 core:design-system

路径：

```text
core/design-system/
  src/main/java/com/budgettracker/core/designsystem/
    theme/
      Color.kt
      Theme.kt
      Type.kt
      Shape.kt
      Spacing.kt
    icon/
      BudgetIcons.kt
```

职责：

- 颜色。
- Typography。
- Shape。
- Spacing。
- Icon set。
- Dark/light theme。

规则：

- 只放设计系统。
- 不放业务 component。

## 7.9 core:i18n

路径：

```text
core/i18n/
  src/main/
    res/
      values/strings.xml
      values-zh/strings.xml
    java/com/budgettracker/core/i18n/
      LanguageManager.kt
      LocalizedCategoryNameResolver.kt
```

职责：

- 中英文语言资源。
- app 内切换语言。
- 默认 category 的本地化显示。

规则：

- 用户自己输入的 category/note 不自动翻译。
- Built-in category 用 key 显示本地化名称。

## 7.10 core:notification

路径：

```text
core/notification/
  src/main/java/com/budgettracker/core/notification/
    listener/
      PaymentNotificationListenerService.kt
    parser/
      PaymentNotificationParser.kt
      PaymentNotificationParserRegistry.kt
      TngNotificationParser.kt
      PublicBankNotificationParser.kt
    model/
      RawNotification.kt
      ParsedPaymentNotification.kt
    quickadd/
      QuickAddNotificationBuilder.kt
      QuickAddNotificationChannel.kt
    duplicate/
      NotificationDuplicateChecker.kt
    di/
      NotificationModule.kt
```

职责：

- Android notification access。
- TNG/Public Bank parser。
- Quick-add notification。
- Notification draft。
- Duplicate detection。

规则：

- Parser 只解析 notification，不直接保存 transaction。
- 保存 draft 通过 repository。
- Quick-add notification 必须可 dismiss、非 ongoing、可 timeout。

## 7.11 core:backup

路径：

```text
core/backup/
  src/main/java/com/budgettracker/core/backup/
    model/
      BackupDocument.kt
      BackupMetadata.kt
    serializer/
      BackupJsonSerializer.kt
    encryption/
      BackupEncryptor.kt
    drive/
      GoogleDriveBackupDataSource.kt
    worker/
      BackupWorker.kt
      RestoreWorker.kt
    di/
      BackupModule.kt
```

职责：

- Backup JSON。
- Encryption。
- Google Drive upload/download。
- WorkManager backup。
- Restore。

规则：

- 不直接依赖 feature。
- Restore 通过 repository/data layer 写入数据库。
- MVP 用 replace restore，merge restore 以后再做。

## 7.12 core:import-export

路径：

```text
core/import-export/
  src/main/java/com/budgettracker/core/importexport/
    csv/
      CsvReader.kt
      CsvWriter.kt
      CsvColumnDetector.kt
      CsvTransactionParser.kt
    mapping/
      ImportColumnMapping.kt
      ImportMappingSuggester.kt
    preview/
      ImportPreviewBuilder.kt
    export/
      TransactionCsvExporter.kt
```

职责：

- CSV import。
- CSV export。
- Column mapping。
- Import preview。
- Parsing error。

规则：

- 不直接显示 UI。
- UI preview 放 feature import-export。
- Duplicate detection 调 core:domain use case。

## 7.13 core:testing

路径：

```text
core/testing/
  src/main/java/com/budgettracker/core/testing/
    repository/
      TestTransactionRepository.kt
      TestCategoryRepository.kt
    data/
      TestTransactions.kt
      TestCategories.kt
    util/
      MainDispatcherRule.kt
      TestClock.kt
```

职责：

- Shared test utilities。
- Fake repositories。
- Test data builders。
- Coroutine test rules。

## 8. Feature Modules

Feature module 原则：

- 一个 feature 负责一个业务区域。
- feature 可以包含 screen、ViewModel、UI state。
- feature 不直接访问 DAO。
- feature 不依赖其他 feature implementation。
- feature 通过 navigation route 或 simple id 与其他 feature 协作。

推荐每个 feature 目录：

```text
feature/<feature-name>/
  src/main/java/com/budgettracker/feature/<feature>/
    navigation/
      <Feature>Route.kt
      <Feature>Navigation.kt
    <screen>/
      <Screen>.kt
      <ViewModel>.kt
      <UiState>.kt
      <UiEvent>.kt
      <UiEffect>.kt
    component/
      <FeatureSpecificComponent>.kt
```

## 8.1 feature:home

职责：

- Home dashboard。
- Monthly summary。
- Recent transaction list。
- Cashbook selector entry。
- Recurring summary。

依赖：

- `core:model`
- `core:domain`
- `core:data`
- `core:ui`
- `core:design-system`
- `core:i18n`

## 8.2 feature:add-transaction

职责：

- Add expense。
- Add income。
- Amount keypad。
- Category picker。
- Pre-filled draft from notification。

依赖：

- `core:model`
- `core:domain`
- `core:data`
- `core:ui`

## 8.3 feature:transactions

职责：

- Transaction list。
- Transaction detail。
- Edit transaction。
- Duplicate transaction。
- Delete transaction。

## 8.4 feature:reports

职责：

- Monthly overview。
- Time dimension report。
- Category dimension report。
- Matrix report。
- Report filters。
- Drill-down transaction list。

## 8.5 feature:charts

职责：

- Donut chart。
- Bar chart。
- Line chart。
- Calendar heatmap。
- Chart interactions。

## 8.6 feature:cashbook

职责：

- Cashbook CRUD。
- Cashbook switcher。
- Default cashbook。
- Archive/delete cashbook。

## 8.7 feature:category

职责：

- Category CRUD。
- Category reorder。
- Icon/color selection。
- Category merge。

## 8.8 feature:recurring

职责：

- Recurring transaction list。
- Create/edit recurring transaction。
- Skip/pause/confirm due recurring item。

## 8.9 feature:settings

职责：

- Language setting。
- Amount input mode。
- Theme。
- Security。
- Notification integration entry。
- Backup settings entry。

## 8.10 feature:notification-integration

职责：

- Enable/disable notification access。
- Enable/disable TNG。
- Enable/disable Public Bank。
- Parser debug samples, development only。
- Dismissed draft history, optional。

## 8.11 feature:backup-restore

职责：

- Connect Google Drive。
- Backup now。
- Restore。
- Backup history。

## 8.12 feature:import-export

职责：

- Import CSV UI。
- Column mapping UI。
- Import preview UI。
- Export CSV UI。

## 9. Package Naming Convention

Base package:

```text
com.budgettracker
```

Examples:

```text
com.budgettracker.app
com.budgettracker.core.model
com.budgettracker.core.database
com.budgettracker.core.domain
com.budgettracker.core.notification
com.budgettracker.feature.home
com.budgettracker.feature.reports
```

Rules:

- Package name mirrors module responsibility。
- No generic `utils` package unless truly generic。
- Prefer specific names like `amount`, `report`, `recurring`, `parser`。

## 10. Navigation Architecture

Use app-level `AppNavHost` to assemble feature navigation.

Recommended pattern:

- Each feature exposes a navigation function.
- App imports feature navigation functions and registers routes.
- Feature route arguments use simple ids.
- Do not pass full objects between screens.

Example:

```kotlin
fun NavGraphBuilder.homeScreen(
    onAddTransaction: () -> Unit,
    onOpenTransaction: (transactionId: String) -> Unit,
    onOpenReports: () -> Unit
)
```

Route examples:

```text
home
add_transaction?draftId={draftId}
transaction_detail/{transactionId}
reports/time
reports/category
settings/language
notification_integration
```

## 11. State Management

Each screen should follow this structure:

```text
Screen Composable
  -> receives UiState
  -> sends UiEvent

Route Composable
  -> obtains ViewModel
  -> collects StateFlow
  -> handles effects

ViewModel
  -> exposes StateFlow<UiState>
  -> receives UiEvent
  -> calls use cases/repositories
```

Rules:

- Screen composables should be mostly stateless。
- ViewModel owns screen state。
- Reusable UI component should not own ViewModel。
- One-time navigation/snackbar uses `UiEffect`。
- Long-running data streams use `Flow`。

## 12. Data Layer Pattern

Repository is the only API for app data.

Example:

```kotlin
interface TransactionRepository {
    fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>>
    fun observeTransaction(id: String): Flow<Transaction?>
    suspend fun addTransaction(input: AddTransactionInput): String
    suspend fun updateTransaction(input: UpdateTransactionInput)
    suspend fun deleteTransaction(id: String)
}
```

Rules:

- DAO returns entities。
- Repository maps entities to domain models。
- Domain/use cases work with domain models。
- UI works with UI models or domain models converted for display。

## 13. Business Logic Placement

放在 ViewModel：

- UI event handling。
- Loading/error state。
- Calling use cases。
- Simple field validation for screen interaction。

放在 Use Case：

- Monthly total calculation。
- Balance calculation。
- Amount parsing。
- Recurring date generation。
- Category suggestion scoring。
- Duplicate detection。
- Import validation。

放在 Repository：

- Data source coordination。
- Database transaction。
- Mapping entity/domain。
- Save/read operations。

放在 DAO：

- SQL queries。
- Insert/update/delete。
- Lightweight aggregation query if needed。

## 14. Adding A New Feature

新增功能标准流程：

1. 在 PRD/TRD 补需求。
2. 判断是否需要新 feature module。
3. 如果是共享能力，放 core module。
4. 新增 domain model 或 use case。
5. 新增 repository API，如果需要数据访问。
6. 新增 database table/migration，如果需要持久化。
7. 新增 feature screen/ViewModel。
8. 在 app navigation 注册 route。
9. 写 unit test。
10. 写 UI test for critical flow。

例子：新增 Budget Limit。

```text
core:model
  Budget.kt

core:database
  BudgetEntity.kt
  BudgetDao.kt

core:data
  BudgetRepository.kt

core:domain
  GetBudgetProgressUseCase.kt

feature:budget
  BudgetListScreen.kt
  EditBudgetScreen.kt

app
  register budget navigation
```

## 15. Low Coupling Rules

必须遵守：

- Feature 不直接依赖另一个 feature 的 implementation。
- Shared UI 放 `core:ui`，不是从某个 feature 里 import。
- Shared business logic 放 `core:domain`。
- Shared data access 放 `core:data`。
- Platform-specific notification/backup 放对应 core module。
- Feature 间跳转只通过 route callback。
- 传参只传 id、date、type 等 simple value。

如果两个 feature 需要共享同一段逻辑：

- 先判断是不是业务逻辑，是就放 `core:domain`。
- 如果是 UI component，放 `core:ui`。
- 如果是 design token，放 `core:design-system`。
- 如果只是暂时相似，允许少量 duplication，等重复稳定后再抽 shared module。

## 16. Build Logic

推荐使用 convention plugins，减少 Gradle 重复配置。

路径：

```text
build-logic/
  convention/
    src/main/kotlin/
      budgettracker.android.application.gradle.kts
      budgettracker.android.library.gradle.kts
      budgettracker.android.feature.gradle.kts
      budgettracker.android.room.gradle.kts
      budgettracker.android.hilt.gradle.kts
      budgettracker.android.compose.gradle.kts
      budgettracker.kotlin.library.gradle.kts
```

用途：

- 统一 Kotlin version。
- 统一 Compose setup。
- 统一 Hilt setup。
- 统一 Room setup。
- 统一 test dependencies。
- 统一 Android SDK config。

## 17. Testing Structure

```text
core/domain/src/test/
  amount/
  report/
  recurring/
  category/

core/database/src/androidTest/
  dao/
  migration/

feature/home/src/test/
  HomeViewModelTest.kt

feature/add-transaction/src/test/
  AddTransactionViewModelTest.kt

app/src/androidTest/
  CriticalFlowTest.kt
```

Testing rules:

- Pure business logic must have unit tests。
- Amount input engine must have unit tests。
- Report totals must have unit tests。
- Recurring generation must have unit tests。
- Notification parsers must have sample-based tests。
- Database migration must be tested before release。

## 18. Documentation Folder

Current docs should eventually be moved into:

```text
docs/
  PRD.md
  TRD.md
  DEVELOPMENT_PROCESS.md
  SCREEN_FLOW.md
  SYSTEM_ARCHITECTURE.md
  UI_SPEC.md
  DATABASE_SCHEMA.md
  TEST_PLAN.md
```

For now, keeping docs at project root is acceptable while planning.

## 19. Recommended First Implementation Structure

不要一开始就创建太多空 module。建议第一阶段先建这些：

```text
app
core:model
core:common
core:database
core:datastore
core:data
core:domain
core:design-system
core:ui
core:i18n
feature:home
feature:add-transaction
feature:transactions
feature:reports
feature:settings
```

第二阶段再加：

```text
feature:charts
feature:cashbook
feature:category
feature:recurring
core:import-export
feature:import-export
```

第三阶段再加：

```text
core:notification
feature:notification-integration
core:backup
feature:backup-restore
```

这样可以避免 over-modularization，同时保留后续扩展空间。

## 20. Architecture Decision Summary

最终建议：

- 用 multi-module，但分阶段创建。
- 用 `core` + `feature` 分组。
- 用 Compose + ViewModel + StateFlow。
- 用 Room + Repository 做 local-first。
- 用 domain use case 承载财务业务逻辑。
- 用 Hilt 管理 dependency。
- 用 WorkManager 做 recurring check 和 backup。
- 用 NotificationListenerService 做 TNG/Public Bank quick add。
- 用 Android string resources 做 i18n。
- 用 convention plugins 统一 Gradle 配置。

这个结构适合从 MVP 开始，也能支撑后续加入更多银行通知、预算、receipt、Drive backup、多 cashbook、多语言和更复杂的 reports。
