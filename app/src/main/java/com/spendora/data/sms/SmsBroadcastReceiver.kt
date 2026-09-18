package com.spendora.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.spendora.data.database.SpendoraDatabase
import com.spendora.data.entity.SmsAuditEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val scope = CoroutineScope(Dispatchers.IO)
            val database = SpendoraDatabase.getDatabase(context)

            for (msg in messages) {
                val body = msg.displayMessageBody ?: continue
                val sender = msg.displayOriginatingAddress ?: "Unknown"
                val timestamp = msg.timestampMillis

                scope.launch {
                    val parsed = SmsEngine.parse(body, sender, timestamp)
                    if (parsed != null) {
                        // Deduplication: Avoid duplicate entries if identical amount occurs within 5 minutes
                        val windowMs = 5 * 60 * 1000L
                        val existing = database.transactionDao().findSimilarTransaction(
                            amount = parsed.amount,
                            startTime = timestamp - windowMs,
                            endTime = timestamp + windowMs
                        )
                        if (existing == null) {
                            val entity = SmsEngine.toEntity(parsed, body, timestamp)
                            database.transactionDao().insertTransaction(entity)
                        }

                        database.smsAuditDao().insertAudit(
                            SmsAuditEntity(
                                smsBody = body,
                                sender = sender,
                                timestamp = timestamp,
                                parseStatus = if (existing != null) "DUPLICATE_SKIPPED" else "PARSED_${parsed.reviewStatus.name}",
                                extractedAmount = parsed.amount
                            )
                        )
                    } else {
                        database.smsAuditDao().insertAudit(
                            SmsAuditEntity(
                                smsBody = body,
                                sender = sender,
                                timestamp = timestamp,
                                parseStatus = "SKIPPED",
                                extractedAmount = null
                            )
                        )
                    }
                }
            }
        }
    }
}
