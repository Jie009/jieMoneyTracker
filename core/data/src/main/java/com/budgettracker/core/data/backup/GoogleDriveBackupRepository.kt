package com.budgettracker.core.data.backup

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.budgettracker.core.database.BudgetTrackerDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveBackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: BudgetTrackerDatabase,
) {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var autoSyncJob: Job? = null
    @Volatile
    private var isRestoring = false
    private val googleSignInClient by lazy {
        GoogleSignIn.getClient(context, googleSignInOptions())
    }
    private val _state = MutableStateFlow(readInitialState())

    val state: StateFlow<GoogleDriveBackupState> = _state.asStateFlow()

    init {
        observeNetwork()
        observeDatabaseChanges()
        refreshSignedInAccount()
        if (_state.value.pendingSync && _state.value.isOnline && _state.value.isConnected) {
            scope.launch { syncNow() }
        }
    }

    fun signInIntent(): Intent = googleSignInClient.signInIntent

    suspend fun handleSignInResult(data: Intent?) {
        val account = GoogleSignIn.getSignedInAccountFromIntent(data)
            .getResult(ApiException::class.java)
        connectAccount(account)
    }

    suspend fun requestSync() {
        if (!_state.value.isConnected) {
            markMessage("Connect Google Drive first.")
            return
        }
        preferences.edit().putBoolean(PendingSyncKey, true).apply()
        _state.update { it.copy(pendingSync = true) }
        syncNow()
    }

    fun disconnect() {
        googleSignInClient.signOut()
        preferences.edit()
            .remove(AccountEmailKey)
            .remove(PendingSyncKey)
            .apply()
        _state.update {
            it.copy(
                isConnected = false,
                accountEmail = null,
                isSyncing = false,
                pendingSync = false,
                message = "Google Drive disconnected.",
            )
        }
    }

    private suspend fun connectAccount(account: GoogleSignInAccount) {
        val email = account.email.orEmpty()
        preferences.edit()
            .putString(AccountEmailKey, email)
            .putBoolean(PendingSyncKey, true)
            .apply()
        _state.update {
            it.copy(
                isConnected = true,
                accountEmail = email,
                pendingSync = true,
                    message = "Google Drive connected. Checking cloud data...",
            )
        }
        syncFromCloudOrUpload(account.account)
    }

    private suspend fun syncFromCloudOrUpload(selectedAccount: Account?) {
        val account = selectedAccount ?: return
        if (!_state.value.isOnline) {
            preferences.edit().putBoolean(PendingSyncKey, true).apply()
            _state.update {
                it.copy(
                    pendingSync = true,
                    message = "No internet. Cloud data will sync when online.",
                )
            }
            return
        }

        _state.update { it.copy(isSyncing = true, message = "Checking Google Drive data...") }
        val result = runCatching {
            withContext(Dispatchers.IO) {
                val drive = buildDriveService(account)
                val cloudFileId = findBackupFileId(drive)
                if (cloudFileId == null) {
                    val backupBytes = createBackupArchive()
                    uploadBackup(drive, backupBytes, existingFileId = null)
                    SyncDirection.Uploaded
                } else {
                    val backupBytes = downloadBackup(drive, cloudFileId)
                    restoreBackupArchive(backupBytes)
                    SyncDirection.Restored
                }
            }
        }

        finishSyncResult(
            result = result,
            account = account,
            successMessage = { direction ->
                when (direction) {
                    SyncDirection.Restored -> "Cloud data restored from Google Drive."
                    SyncDirection.Uploaded -> "Cloud data created in Google Drive."
                }
            },
        )
    }

    private suspend fun syncNow(selectedAccount: Account? = currentGoogleAccount()?.account) {
        val account = selectedAccount
        if (account == null) {
            preferences.edit().putBoolean(PendingSyncKey, true).apply()
            _state.update {
                it.copy(
                    pendingSync = true,
                    isConnected = false,
                    isSyncing = false,
                    message = "Google Drive sign-in is required.",
                )
            }
            return
        }
        if (!_state.value.isOnline) {
            preferences.edit().putBoolean(PendingSyncKey, true).apply()
            _state.update {
                it.copy(
                    pendingSync = true,
                    isSyncing = false,
                    message = "No internet. Backup will sync when online.",
                )
            }
            return
        }

        _state.update { it.copy(isSyncing = true, message = "Syncing Google Drive backup...") }
        val result = runCatching {
            withContext(Dispatchers.IO) {
                val backupBytes = createBackupArchive()
                val drive = buildDriveService(account)
                uploadBackup(drive, backupBytes)
                Unit
            }
        }
        finishSyncResult(
            result = result,
            account = account,
            successMessage = { "Google Drive data synced." },
        )
    }

    private fun <T> finishSyncResult(
        result: Result<T>,
        account: Account,
        successMessage: (T) -> String,
    ) {
        result.fold(
            onSuccess = { value ->
                val syncedAt = Instant.now()
                preferences.edit()
                    .putBoolean(PendingSyncKey, false)
                    .putLong(LastSyncedAtKey, syncedAt.toEpochMilli())
                    .putString(AccountEmailKey, account.name)
                    .apply()
                _state.update {
                    it.copy(
                        isConnected = true,
                        accountEmail = account.name,
                        isSyncing = false,
                        pendingSync = false,
                        lastSyncedAt = syncedAt,
                        message = successMessage(value),
                    )
                }
            },
            onFailure = { error ->
                preferences.edit().putBoolean(PendingSyncKey, true).apply()
                _state.update {
                    it.copy(
                        isSyncing = false,
                        pendingSync = true,
                        message = error.localizedMessage ?: "Google Drive sync failed. Will retry later.",
                    )
                }
            },
        )
    }

    private suspend fun createBackupArchive(): ByteArray = withContext(Dispatchers.IO) {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

        val databaseFile = context.getDatabasePath(DatabaseName)
        check(databaseFile.exists()) { "Local database is not ready yet." }

        ByteArrayOutputStream().use { bytes ->
            ZipOutputStream(bytes).use { zip ->
                zip.putNextEntry(ZipEntry(DatabaseName))
                databaseFile.inputStream().use { input -> input.copyTo(zip) }
                zip.closeEntry()

                val prefsFile = java.io.File(
                    context.applicationInfo.dataDir,
                    "shared_prefs/$PreferencesName.xml",
                )
                if (prefsFile.exists()) {
                    zip.putNextEntry(ZipEntry("shared_prefs/$PreferencesName.xml"))
                    prefsFile.inputStream().use { input -> input.copyTo(zip) }
                    zip.closeEntry()
                }
            }
            bytes.toByteArray()
        }
    }

    private fun buildDriveService(account: Account): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA),
        ).apply {
            selectedAccount = account
        }
        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential,
        )
            .setApplicationName(ApplicationName)
            .build()
    }

    private fun uploadBackup(
        drive: Drive,
        backupBytes: ByteArray,
        existingFileId: String? = findBackupFileId(drive),
    ) {
        val metadata = File()
            .setName(BackupFileName)
            .setParents(listOf(AppDataFolder))
        val content = ByteArrayContent(BackupMimeType, backupBytes)

        if (existingFileId == null) {
            drive.files()
                .create(metadata, content)
                .setFields("id")
                .execute()
        } else {
            drive.files()
                .update(existingFileId, File().setName(BackupFileName), content)
                .setFields("id")
                .execute()
        }
    }

    private fun findBackupFileId(drive: Drive): String? =
        drive.files()
            .list()
            .setSpaces(AppDataFolder)
            .setQ("name = '$BackupFileName' and trashed = false")
            .setFields("files(id)")
            .execute()
            .files
            .firstOrNull()
            ?.id

    private fun downloadBackup(drive: Drive, fileId: String): ByteArray =
        ByteArrayOutputStream().use { output ->
            drive.files().get(fileId).executeMediaAndDownloadTo(output)
            output.toByteArray()
        }

    private fun restoreBackupArchive(backupBytes: ByteArray) {
        isRestoring = true
        try {
            database.close()

            val databaseFile = context.getDatabasePath(DatabaseName)
            databaseFile.parentFile?.mkdirs()
            databaseFile.delete()
            java.io.File("${databaseFile.path}-wal").delete()
            java.io.File("${databaseFile.path}-shm").delete()

            ZipInputStream(ByteArrayInputStream(backupBytes)).use { zip ->
                generateSequence { zip.nextEntry }.forEach { entry ->
                    when (entry.name) {
                        DatabaseName -> {
                            databaseFile.outputStream().use { output -> zip.copyTo(output) }
                        }
                        "shared_prefs/$PreferencesName.xml" -> {
                            val prefsFile = java.io.File(
                                context.applicationInfo.dataDir,
                                "shared_prefs/$PreferencesName.xml",
                            )
                            prefsFile.parentFile?.mkdirs()
                            prefsFile.outputStream().use { output -> zip.copyTo(output) }
                        }
                    }
                    zip.closeEntry()
                }
            }

            check(databaseFile.exists()) { "Cloud backup did not contain the local database." }
        } finally {
            isRestoring = false
        }
    }

    private fun refreshSignedInAccount() {
        val account = currentGoogleAccount()
        if (account != null) {
            preferences.edit().putString(AccountEmailKey, account.email.orEmpty()).apply()
        }
        _state.update {
            it.copy(
                isConnected = account != null,
                accountEmail = account?.email ?: preferences.getString(AccountEmailKey, null),
            )
        }
    }

    private fun currentGoogleAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)
            ?.takeIf { account ->
                GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_APPDATA))
            }

    private fun observeNetwork() {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _state.update { it.copy(isOnline = true) }
                if (_state.value.pendingSync && _state.value.isConnected) {
                    scope.launch { syncNow() }
                }
            }

            override fun onLost(network: Network) {
                _state.update { it.copy(isOnline = isNetworkAvailable()) }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    private fun observeDatabaseChanges() {
        database.invalidationTracker.addObserver(
            object : androidx.room.InvalidationTracker.Observer(DatabaseTables) {
                override fun onInvalidated(tables: Set<String>) {
                    if (isRestoring || !_state.value.isConnected) return

                    preferences.edit().putBoolean(PendingSyncKey, true).apply()
                    _state.update { it.copy(pendingSync = true) }
                    autoSyncJob?.cancel()
                    autoSyncJob = scope.launch {
                        delay(AutoSyncDebounceMillis)
                        syncNow()
                    }
                }
            },
        )
    }

    private fun readInitialState(): GoogleDriveBackupState {
        val lastSyncedAt = preferences.getLong(LastSyncedAtKey, 0L)
            .takeIf { it > 0L }
            ?.let(Instant::ofEpochMilli)
        return GoogleDriveBackupState(
            isConnected = false,
            accountEmail = preferences.getString(AccountEmailKey, null),
            isOnline = isNetworkAvailable(),
            pendingSync = preferences.getBoolean(PendingSyncKey, false),
            lastSyncedAt = lastSyncedAt,
        )
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun markMessage(message: String) {
        _state.update { it.copy(message = message) }
    }

    private fun googleSignInOptions(): GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

    private companion object {
        const val ApplicationName = "BudgetTracker"
        const val AppDataFolder = "appDataFolder"
        const val BackupFileName = "budget_tracker_backup.zip"
        const val BackupMimeType = "application/zip"
        const val DatabaseName = "budget_tracker.db"
        const val PreferencesName = "budget_tracker_preferences"
        const val AccountEmailKey = "google_drive_account_email"
        const val PendingSyncKey = "google_drive_pending_sync"
        const val LastSyncedAtKey = "google_drive_last_synced_at"
        const val AutoSyncDebounceMillis = 1_500L
        val DatabaseTables = arrayOf(
            "cashbooks",
            "categories",
            "transactions",
            "recurring_transactions",
            "budgets",
        )
    }
}

private enum class SyncDirection {
    Restored,
    Uploaded,
}

data class GoogleDriveBackupState(
    val isConnected: Boolean = false,
    val accountEmail: String? = null,
    val isOnline: Boolean = false,
    val isSyncing: Boolean = false,
    val pendingSync: Boolean = false,
    val lastSyncedAt: Instant? = null,
    val message: String? = null,
)
