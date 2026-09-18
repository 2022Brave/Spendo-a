package com.spendora.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.SpendoraPurplePrimary
import com.spendora.data.database.SpendoraDatabase
import com.spendora.data.entity.TransactionEntity
import com.spendora.data.model.TransactionSource
import com.spendora.data.sms.SmsEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HistoricalImportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var scannedCount by remember { mutableStateOf(0) }
    var importedCount by remember { mutableStateOf(0) }
    var scanCompleted by remember { mutableStateOf(false) }

    fun runSmsImport() {
        isScanning = true
        scanCompleted = false
        scope.launch(Dispatchers.IO) {
            val db = SpendoraDatabase.getDatabase(context)
            val cr = context.contentResolver
            val uri = Telephony.Sms.Inbox.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms.Inbox.BODY,
                Telephony.Sms.Inbox.ADDRESS,
                Telephony.Sms.Inbox.DATE
            )

            // Scan last 60 days
            val sixtyDaysAgo = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000)
            val selection = "${Telephony.Sms.Inbox.DATE} >= ?"
            val selectionArgs = arrayOf(sixtyDaysAgo.toString())

            val entities = mutableListOf<TransactionEntity>()
            var totalMessages = 0

            try {
                cr.query(uri, projection, selection, selectionArgs, "${Telephony.Sms.Inbox.DATE} DESC")?.use { cursor ->
                    val bodyIndex = cursor.getColumnIndex(Telephony.Sms.Inbox.BODY)
                    val addressIndex = cursor.getColumnIndex(Telephony.Sms.Inbox.ADDRESS)
                    val dateIndex = cursor.getColumnIndex(Telephony.Sms.Inbox.DATE)

                    while (cursor.moveToNext()) {
                        totalMessages++
                        val body = cursor.getString(bodyIndex) ?: continue
                        val address = cursor.getString(addressIndex) ?: "Unknown"
                        val timestamp = cursor.getLong(dateIndex)

                        val parsed = SmsEngine.parse(body, address, timestamp)
                        if (parsed != null) {
                            // Check if transaction with same amount within 5 min window already in DB or current batch
                            val windowMs = 5 * 60 * 1000L
                            val existingInDb = db.transactionDao().findSimilarTransaction(
                                amount = parsed.amount,
                                startTime = timestamp - windowMs,
                                endTime = timestamp + windowMs
                            )
                            val alreadyInBatch = entities.any { 
                                it.amount == parsed.amount && kotlin.math.abs(it.timestamp - timestamp) <= windowMs 
                            }
                            if (existingInDb == null && !alreadyInBatch) {
                                val entity = SmsEngine.toEntity(parsed, body, timestamp).copy(
                                    source = TransactionSource.SMS_IMPORT
                                )
                                entities.add(entity)
                            }
                        }
                    }
                }

                if (entities.isNotEmpty()) {
                    db.transactionDao().insertTransactions(entities)
                }
            } catch (e: Exception) {
                // Log or handle safely
            }

            withContext(Dispatchers.Main) {
                scannedCount = totalMessages
                importedCount = entities.size
                isScanning = false
                scanCompleted = true
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            runSmsImport()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Historical SMS Scanner",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // On-device privacy banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, SpendoraPurplePrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Offline",
                        tint = SpendoraPurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "100% On-Device SMS Parser",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Spendora inspects your bank debit and credit SMS notifications locally. OTPs, personal chats, and marketing messages are automatically ignored and never parsed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (scanCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Scan Finished!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Scanned $scannedCount messages. Imported $importedCount financial transactions into your database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    runSmsImport()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_SMS)
                }
            },
            enabled = !isScanning,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpendoraPurplePrimary,
                contentColor = Color.White
            )
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text("Scanning Inbox Locally...")
            } else {
                Icon(imageVector = Icons.Default.HistoryEdu, contentDescription = "Scan")
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Scan Past 60 Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
