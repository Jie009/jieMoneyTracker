package com.budgettracker.app.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.budgettracker.app.MainActivity
import com.budgettracker.core.model.PaymentNotificationSource
import com.budgettracker.core.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class PaymentNotificationListenerService : NotificationListenerService() {
    private val parsers = listOf(
        TngNotificationParser(),
        PublicBankNotificationParser(),
    )

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createQuickAddChannel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        runCatching {
            if (sbn.packageName == packageName) return

            val rawNotification = sbn.toRawNotification()
            val parsedNotification = parsers
                .asSequence()
                .filter { it.supports(rawNotification.packageName) }
                .mapNotNull { it.parse(rawNotification) }
                .firstOrNull()
                ?: return

            if (DuplicateNotifications.isDuplicate(parsedNotification, rawNotification)) return
            showQuickAddNotification(parsedNotification)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(ComponentName(this, PaymentNotificationListenerService::class.java))
        }
    }

    private fun showQuickAddNotification(parsedNotification: ParsedPaymentNotification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            parsedNotification.notificationId,
            Intent(this, MainActivity::class.java).apply {
                action = PaymentNotificationIntents.ACTION_QUICK_ADD
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(PaymentNotificationIntents.EXTRA_AMOUNT, parsedNotification.amountInput)
                putExtra(PaymentNotificationIntents.EXTRA_TYPE, parsedNotification.transactionType.name)
                putExtra(PaymentNotificationIntents.EXTRA_NOTE, parsedNotification.note)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = when (parsedNotification.transactionType) {
            TransactionType.Income -> "Add received money"
            TransactionType.Expense -> "Add payment"
        }
        val content = "RM ${parsedNotification.amountInput} from ${parsedNotification.source.displayName}"

        val notification = Notification.Builder(this, QuickAddChannelId)
            .setSmallIcon(android.R.drawable.ic_menu_add)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(Notification.BigTextStyle().bigText("${parsedNotification.note}\n$content"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setTimeoutAfter(QuickAddTimeoutMillis)
            .setCategory(Notification.CATEGORY_RECOMMENDATION)
            .build()

        getSystemService(NotificationManager::class.java).notify(parsedNotification.notificationId, notification)
    }
}

private interface PaymentNotificationParser {
    val source: PaymentNotificationSource
    val supportedPackageNames: Set<String>

    fun parse(input: RawPaymentNotification): ParsedPaymentNotification?

    fun supports(packageName: String): Boolean {
        val normalizedPackageName = packageName.lowercase(Locale.US)
        return supportedPackageNames.any { supportedPackageName ->
            normalizedPackageName == supportedPackageName ||
                normalizedPackageName.contains(supportedPackageName)
        }
    }
}

private class TngNotificationParser : PaymentNotificationParser {
    override val source = PaymentNotificationSource.TouchNGo
    override val supportedPackageNames = setOf(
        "my.com.tngdigital.ewallet",
        "tngdigital",
    )

    override fun parse(input: RawPaymentNotification): ParsedPaymentNotification? =
        parsePaymentNotification(source = source, input = input)
}

private class PublicBankNotificationParser : PaymentNotificationParser {
    override val source = PaymentNotificationSource.PublicBank
    override val supportedPackageNames = setOf(
        "publicbank",
        "pbengage",
    )

    override fun parse(input: RawPaymentNotification): ParsedPaymentNotification? =
        parsePaymentNotification(source = source, input = input)
}

private fun parsePaymentNotification(
    source: PaymentNotificationSource,
    input: RawPaymentNotification,
): ParsedPaymentNotification? {
    val searchableText = input.searchableText
    val amountInput = AmountRegex.find(searchableText)
        ?.groupValues
        ?.getOrNull(1)
        ?.toAmountInput()
        ?: return null
    val transactionType = searchableText.detectTransactionType(source)
    val note = listOfNotNull(
        source.displayName,
        input.title.takeIf { it.isNotBlank() },
        input.text.takeIf { it.isNotBlank() },
    ).joinToString(" · ").take(MaxNoteLength)

    return ParsedPaymentNotification(
        source = source,
        amountInput = amountInput,
        transactionType = transactionType,
        note = note,
    )
}

private fun StatusBarNotification.toRawNotification(): RawPaymentNotification {
    val extras = notification.extras
    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
    val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
    val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
    val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        ?.joinToString(separator = "\n") { it.toString() }
        .orEmpty()

    return RawPaymentNotification(
        packageName = packageName,
        title = title,
        text = text,
        subText = subText,
        bigText = bigText,
        textLines = textLines,
        postTime = postTime,
    )
}

private data class RawPaymentNotification(
    val packageName: String,
    val title: String,
    val text: String,
    val subText: String,
    val bigText: String,
    val textLines: String,
    val postTime: Long,
) {
    val searchableText: String =
        listOf(title, text, subText, bigText, textLines)
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n")
}

private data class ParsedPaymentNotification(
    val source: PaymentNotificationSource,
    val amountInput: String,
    val transactionType: TransactionType,
    val note: String,
) {
    val notificationId: Int = listOf(source.name, amountInput, transactionType.name, note)
        .joinToString("|")
        .hashCode()
}

private object DuplicateNotifications {
    private val seenAtByKey = ConcurrentHashMap<String, Long>()

    fun isDuplicate(
        parsedNotification: ParsedPaymentNotification,
        rawNotification: RawPaymentNotification,
    ): Boolean {
        val now = System.currentTimeMillis()
        seenAtByKey.entries.removeIf { (_, seenAt) -> now - seenAt > DuplicateWindowMillis }

        val key = listOf(
            rawNotification.packageName,
            parsedNotification.amountInput,
            parsedNotification.transactionType.name,
            rawNotification.searchableText.hashCode().toString(),
        ).joinToString("|")

        return seenAtByKey.putIfAbsent(key, now) != null
    }
}

private fun String.detectTransactionType(source: PaymentNotificationSource): TransactionType {
    val normalized = lowercase(Locale.US)
    val incomeKeywords = CommonIncomeKeywords + source.incomeKeywords

    if (incomeKeywords.any { it in normalized }) return TransactionType.Income
    return TransactionType.Expense
}

private fun String.toAmountInput(): String =
    BigDecimal(replace(",", ""))
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString()

private fun NotificationManager.createQuickAddChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val channel = NotificationChannel(
        QuickAddChannelId,
        "Payment quick add",
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = "Quick-add transaction drafts from payment notifications."
        setShowBadge(false)
    }
    createNotificationChannel(channel)
}

private val PaymentNotificationSource.displayName: String
    get() = when (this) {
        PaymentNotificationSource.TouchNGo -> "Touch 'n Go"
        PaymentNotificationSource.PublicBank -> "Public Bank"
    }

private val AmountRegex = Regex(
    pattern = """(?i)(?:RM|MYR)\s*([0-9]{1,3}(?:,[0-9]{3})+(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
)

private val CommonIncomeKeywords = listOf(
    "payment received",
    "received payment",
    "you have received",
    "money received",
    "received",
    "credited to",
    "credited",
    "funds credited",
    "deposit",
    "deposited",
    "refund",
    "cashback",
)

private val PaymentNotificationSource.incomeKeywords: List<String>
    get() = when (this) {
        PaymentNotificationSource.TouchNGo -> listOf(
            "customer paid",
            "customer has paid",
            "scan your qr code",
            "duitnow qr code for payment",
        )
        PaymentNotificationSource.PublicBank -> listOf(
            "credited to your account",
            "credited into your account",
            "funds credited to your account",
            "received from",
            "transferred from",
        )
    }

private const val QuickAddChannelId = "payment_quick_add"
private const val QuickAddTimeoutMillis = 15 * 60 * 1000L
private const val DuplicateWindowMillis = 5 * 60 * 1000L
private const val MaxNoteLength = 160
