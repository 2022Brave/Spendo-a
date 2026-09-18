package com.spendora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpendoraExpenseRed
import com.example.ui.theme.SpendoraIncomeGreen
import com.example.ui.theme.SpendoraPurplePrimary
import com.example.ui.theme.SpendoraSavingsPurple
import com.spendora.data.entity.TransactionEntity
import com.spendora.data.model.TransactionSource
import com.spendora.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: TransactionEntity,
    categoryName: String,
    accountName: String? = null,
    onDismiss: () -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateTimeFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = dateTimeFormat.format(Date(transaction.timestamp))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Type Pill
            val (typeColor, typeLabel) = when (transaction.type) {
                TransactionType.EXPENSE -> Pair(SpendoraExpenseRed, "Expense")
                TransactionType.INCOME -> Pair(SpendoraIncomeGreen, "Income")
                TransactionType.TRANSFER -> Pair(Color(0xFF38BDF8), "Internal Transfer")
                TransactionType.REFUND -> Pair(SpendoraIncomeGreen, "Refund")
                TransactionType.CASH_WITHDRAWAL -> Pair(SpendoraSavingsPurple, "ATM Cash Withdrawal")
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(typeColor.copy(alpha = 0.15f))
                    .border(1.dp, typeColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = typeColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large Formatted Amount
            val prefix = when (transaction.type) {
                TransactionType.EXPENSE, TransactionType.CASH_WITHDRAWAL -> "-"
                TransactionType.INCOME, TransactionType.REFUND -> "+"
                TransactionType.TRANSFER -> ""
            }
            Text(
                text = prefix + formatRupee(transaction.amount),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.INCOME || transaction.type == TransactionType.REFUND)
                    SpendoraIncomeGreen
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Detail Items Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Merchant / Payee
                transaction.merchantName?.takeIf { it.isNotBlank() }?.let { merchant ->
                    DetailRow(
                        icon = Icons.Default.Store,
                        label = "Merchant / Payee",
                        value = merchant
                    )
                }

                // Category
                DetailRow(
                    icon = Icons.Default.Category,
                    label = "Category",
                    value = categoryName.ifEmpty { "General" }
                )

                // Account
                val displayAccount = accountName ?: transaction.accountId
                if (!displayAccount.isNullOrBlank()) {
                    DetailRow(
                        icon = Icons.Default.AccountBalance,
                        label = "Account",
                        value = displayAccount
                    )
                }

                // Date & Time
                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Date & Time",
                    value = formattedDate
                )

                // Source
                val sourceLabel = when (transaction.source) {
                    TransactionSource.SMS_LIVE -> "Bank SMS (Live on-device)"
                    TransactionSource.MANUAL -> "Manual Entry"
                    TransactionSource.SMS_IMPORT -> "Historical SMS Import"
                }
                DetailRow(
                    icon = Icons.Default.Description,
                    label = "Source",
                    value = sourceLabel
                )

                // Note
                transaction.note?.takeIf { it.isNotBlank() }?.let { note ->
                    DetailRow(
                        icon = Icons.Default.Description,
                        label = "Note",
                        value = note
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions: Delete & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDelete(transaction)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SpendoraExpenseRed
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SpendoraExpenseRed.copy(alpha = 0.5f))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = " Delete",
                        style = MaterialTheme.typography.labelLarge,
                        color = SpendoraExpenseRed
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpendoraPurplePrimary
                    )
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SpendoraPurplePrimary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
